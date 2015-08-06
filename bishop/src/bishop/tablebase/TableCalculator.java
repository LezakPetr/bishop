package bishop.tablebase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import parallel.ParallelUtils;

import bishop.base.Color;
import bishop.base.Fen;
import bishop.base.LegalMoveFinder;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.PositionValidator;

public class TableCalculator {
	
	private final MaterialHash[] materialHashArray;
	private final ExecutorService executor;
	private final int threadCount;
	private final BothColorPositionResultSource<PersistentTable> bothTables;
	private final TableSwitch resultSource;
	
	private BitArray prevPositionsToCheck;
	private BitArray nextPositionsToCheck;

	
	public TableCalculator(final MaterialHash[] materialHashArray, final ExecutorService executor, final int threadCount) {
		this.materialHashArray = new MaterialHash[Color.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			this.materialHashArray[color] = materialHashArray[color].copy();
		
		this.executor = executor;
		this.threadCount = threadCount;
		
		this.bothTables = new BothColorPositionResultSource<PersistentTable>();
		
		this.resultSource = new TableSwitch();
	}
	
	public void addSubTable(final MaterialHash materialHash, final ITableRead subTable) {
		resultSource.addTable(materialHash, subTable);
	}
	
	public void calculate() throws Exception {
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final TableDefinition tableDefinition = new TableDefinition(TableWriter.VERSION, materialHashArray[onTurn]);
			final MaterialHash materialHash = tableDefinition.getMaterialHash();
			final PersistentTable table = new PersistentTable(tableDefinition, "/tmp/" + tableDefinition.getMaterialHash().toString());
			
			bothTables.setBaseSource(onTurn, table);
			resultSource.addTable(materialHash, table);
		}
		
		try {
			generateTableBase();
		}
		finally {
			for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
				final MaterialHash materialHash = bothTables.getBaseSource(onTurn).getDefinition().getMaterialHash();
				
				resultSource.removeSource(materialHash);
			}
		}
		
		printData();
	}
	
	private void printData() {
		final PersistentTable table = bothTables.getBaseSource(Color.WHITE);
		long winCount = 0;
		long loseCount = 0;
		long drawCount = 0;
		long illegalCount = 0;
		
		int longestWinDepth = -1;
		final Position longestWinPosition = new Position(); 
		
		for (ITableIterator it = table.getIterator(); it.isValid(); it.next()) {
			final int result = it.getResult();
			
			if (result == TableResult.ILLEGAL)
				illegalCount++;
			else {
				if (TableResult.isWin(result)) {
					winCount++;
					
					final int depth = TableResult.getWinDepth(result);
					
					if (depth > longestWinDepth) {
						longestWinDepth = depth;
						it.fillPosition(longestWinPosition);
					}
				}
				else {
					if (TableResult.isLose(result))
						loseCount++;
					else
						drawCount++;
				}
			}
		}
		
		System.out.println("Win: " + winCount);
		System.out.println("Lose: " + loseCount);
		System.out.println("Draw: " + drawCount);
		System.out.println("Illegal: " + illegalCount);
		
		if (longestWinDepth >= 0) {
			final Fen fen = new Fen();
			fen.setPosition(longestWinPosition);
			
			final PrintWriter writer = new PrintWriter(System.out);
			fen.writeFen(writer);
			
			writer.flush();
			System.out.println();
		}
	}

	public BothColorPositionResultSource getTable() {
		final BothColorPositionResultSource<ITable> copy = new BothColorPositionResultSource<>();
		
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++)
			copy.setBaseSource(onTurn, bothTables.getBaseSource(onTurn));
		
		return bothTables;
	}
	
	private static void initializeBlocks(final PersistentTable table) throws FileNotFoundException, IOException {
		final LegalMoveFinder moveFinder = new LegalMoveFinder();
		
		final Position position = new Position();
		final PositionValidator validator = new PositionValidator();
		validator.setPosition(position);
		
		while (true) {
			try (
				final OutputFileTableIterator it = table.getOutputBlock()
			) {
				if (it == null)
					break;
		
				while (it.isValid()) {
					it.fillPosition(position);
					
					final boolean isValid = table.getDefinition().hasSameCountOfPieces(position) && validator.checkPosition();
					final int result;
					
					if (isValid) {
						if (moveFinder.existsLegalMove(position)) {
							result = TableResult.DRAW;
						}
						else {
							if (position.isCheck())
								result = TableResult.MATE;
							else
								result = TableResult.DRAW;						
						}
					}
					else
						result = TableResult.ILLEGAL;
					
					it.setResult(result);
					it.next();
				}
			}
		}
	}
	
	private void initializeTable() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final PersistentTable table = bothTables.getBaseSource(onTurn);
			
			table.clear();
			table.switchToModeWrite();
			
			ParallelUtils.runParallel(executor, threadCount, new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					initializeBlocks(table);
					
					return null;
				}
			});			
			
			table.moveOutputToInput();
		}
	}
	
	private void generateTableBase() throws Exception {
		initializeTable();
		
		boolean firstIteration = true;
		final List<CalculationTaskProcessor> processorList = new ArrayList<CalculationTaskProcessor>();
		
		for (int i = 0; i < threadCount; i++) {
			processorList.add (new CalculationTaskProcessor(resultSource));
		}
		
		long changeCount;
		
		do {
			changeCount = 0;
			
			for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
				final PersistentTable ownTable = bothTables.getBaseSource(onTurn);
				final PersistentTable oppositeTable = bothTables.getBaseSource(Color.getOppositeColor(onTurn));
				final TableDefinition oppositeTableDefinition = oppositeTable.getDefinition();
				
				final long itemCount = oppositeTableDefinition.getTableIndexCount();
				nextPositionsToCheck = new BitArray(itemCount);
				
				// Initialize
				ownTable.switchToModeWrite();
				oppositeTable.switchToModeRead();
				
				for (int i = 0; i < threadCount; i++) {
					final CalculationTaskProcessor processor = processorList.get(i);
					processor.initialize(firstIteration, oppositeTableDefinition, prevPositionsToCheck, ownTable);
				}
				
				// Execute
				final List<Future<Object>> futureList = executor.invokeAll(processorList);
				
				for (Future<Object> future: futureList) {
					future.get();
				}
				
				// Aggregate
				for (CalculationTaskProcessor processor: processorList) {
					changeCount += processor.getChangeCount();
					nextPositionsToCheck.assignOr (processor.getNextPositionsToCheck());
				}
				
				ownTable.moveOutputToInput();
				prevPositionsToCheck = nextPositionsToCheck;
			}
			
			System.out.println ("Change count " + changeCount);
			
			firstIteration = false;
		} while (changeCount > 0);
	}

	public List<MaterialHash> getNeededSubtables() {
		final List<MaterialHash> neededSubtables = new LinkedList<MaterialHash>();
		
		// Captures
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final MaterialHash parentHash = materialHashArray[onTurn].copy();
			parentHash.swapOnTurn();
			
			addCapturesToSubtables(neededSubtables, parentHash);
		}
		
		// Promotion
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final int pieceCount = materialHashArray[onTurn].getPieceCount(onTurn, PieceType.PAWN);
				
			if (pieceCount > 0) {
				for (int promotionPieceType = PieceType.PROMOTION_FIGURE_FIRST; promotionPieceType < PieceType.PROMOTION_FIGURE_LAST; promotionPieceType++) {
					final MaterialHash subMaterialHash = materialHashArray[onTurn].copy();
					subMaterialHash.removePiece(onTurn, PieceType.PAWN);
					subMaterialHash.addPiece(onTurn, promotionPieceType);
					subMaterialHash.swapOnTurn();
					
					addMaterialToSubtables(neededSubtables, subMaterialHash);
					addCapturesToSubtables(neededSubtables, subMaterialHash);
				}
			}
		}
		
		return neededSubtables;
	}

	private static void addCapturesToSubtables(final List<MaterialHash> neededSubtables, final MaterialHash parentHash) {
		final int capturedColor = parentHash.getOnTurn();
		
		for (int capturedPieceType = PieceType.VARIABLE_FIRST; capturedPieceType < PieceType.VARIABLE_LAST; capturedPieceType++) {
			final int pieceCount = parentHash.getPieceCount(capturedColor, capturedPieceType);
			
			if (pieceCount > 0) {
				final MaterialHash subMaterialHash = parentHash.copy();
				subMaterialHash.removePiece(capturedColor, capturedPieceType);
									
				addMaterialToSubtables(neededSubtables, subMaterialHash);
			}
		}
	}

	private static void addMaterialToSubtables(final List<MaterialHash> neededSubtables, final MaterialHash subMaterialHash) {
		final MaterialHash oppositeHash = subMaterialHash.getOpposite();
		
		if (subMaterialHash.isGreater (oppositeHash))
			neededSubtables.add(subMaterialHash);
		else
			neededSubtables.add(oppositeHash);
	}

}
