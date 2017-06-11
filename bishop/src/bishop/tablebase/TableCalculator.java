package bishop.tablebase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import parallel.Parallel;
import bishop.base.Color;
import bishop.base.Fen;
import bishop.base.LegalMoveFinder;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.PositionValidator;

public class TableCalculator {
	
	private final MaterialHash[] materialHashArray;
	private final Parallel parallel;
	private final BothColorPositionResultSource<IStagedTable> bothTables;
	private final TableSwitch resultSource;
	
	private BitArray prevPositionsToCheck;
	private BitArray nextPositionsToCheck;
	
	private boolean usePersistentTable = false;
	private boolean useCompressedTable = false;

	
	public TableCalculator(final MaterialHash[] materialHashArray, final Parallel parallel) {
		this.materialHashArray = new MaterialHash[Color.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			this.materialHashArray[color] = materialHashArray[color].copy();
		
		this.parallel = parallel;		
		this.bothTables = new BothColorPositionResultSource<IStagedTable>();
		
		this.resultSource = new TableSwitch();
	}
	
	public void addSubTable(final MaterialHash materialHash, final ITableRead subTable) {
		resultSource.addTable(materialHash, subTable);
	}
	
	public void calculate() throws Exception {
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final TableDefinition tableDefinition = new TableDefinition(TableWriter.VERSION, materialHashArray[onTurn]);
			final MaterialHash materialHash = tableDefinition.getMaterialHash();
			final IStagedTable table = createStagedTable(tableDefinition);
			
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
	}

	private IStagedTable createStagedTable(final TableDefinition tableDefinition) {
		if (usePersistentTable)
			return new PersistentStagedTable(tableDefinition, "/tmp/" + tableDefinition.getMaterialHash().toString());
		else
			return new MemoryStagedTable(tableDefinition, useCompressedTable);
	}
	
	private void printData() {
		final IStagedTable table = bothTables.getBaseSource(Color.WHITE);
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

	public void assignTablesTo(final BothColorPositionResultSource<? super IStagedTable> result) {
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++)
			result.setBaseSource(onTurn, bothTables.getBaseSource(onTurn));
	}
	
	private static void initializeBlocks(final IStagedTable table) throws FileNotFoundException, IOException {
		final LegalMoveFinder moveFinder = new LegalMoveFinder();
		
		final Position position = new Position(true);
		final PositionValidator validator = new PositionValidator();
		validator.setPosition(position);
		
		while (true) {
			try (
				final IClosableTableIterator it = table.getOutputPage()
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
			final IStagedTable table = bothTables.getBaseSource(onTurn);
			
			table.clear();
			table.switchToModeWrite();
			
			parallel.runParallel(new Callable<Throwable>() {
				@Override
				public Throwable call() throws Exception {
					initializeBlocks(table);
					
					return null;
				}
			});			
		}
	}

	private void generateTableBase() throws Exception {
		initializeTable();
		
		boolean firstIteration = true;
		final List<CalculationTaskProcessor> processorList = new ArrayList<CalculationTaskProcessor>();
		
		for (int i = 0; i < parallel.getThreadCount(); i++) {
			processorList.add (new CalculationTaskProcessor(resultSource));
		}
		
		long changeCount;
		
		do {
			changeCount = 0;
			
			for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
				final IStagedTable ownTable = bothTables.getBaseSource(onTurn);
				final IStagedTable oppositeTable = bothTables.getBaseSource(Color.getOppositeColor(onTurn));
				final TableDefinition oppositeTableDefinition = oppositeTable.getDefinition();
				
				final long itemCount = oppositeTableDefinition.getTableIndexCount();
				nextPositionsToCheck = new BitArray(itemCount);
				
				// Initialize
				ownTable.switchToModeWrite();
				oppositeTable.switchToModeRead(parallel);
				
				for (int i = 0; i < processorList.size(); i++) {
					final CalculationTaskProcessor processor = processorList.get(i);
					processor.initialize(firstIteration, oppositeTableDefinition, prevPositionsToCheck, ownTable);
				}
				
				// Execute
				parallel.invokeAll (processorList);
				
				// Aggregate
				for (CalculationTaskProcessor processor: processorList) {
					changeCount += processor.getChangeCount();
					nextPositionsToCheck.assignOr (processor.getNextPositionsToCheck());
				}
				
				prevPositionsToCheck = nextPositionsToCheck;
			}
			
			System.out.println ("Change count " + changeCount);
			
			firstIteration = false;
		} while (changeCount > 0);
	}

	public Set<MaterialHash> getNeededSubtables() {
		final Set<MaterialHash> neededSubtables = new HashSet<>();
		
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

	private static void addCapturesToSubtables(final Set<MaterialHash> neededSubtables, final MaterialHash parentHash) {
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

	private static void addMaterialToSubtables(final Set<MaterialHash> neededSubtables, final MaterialHash subMaterialHash) {
		final MaterialHash oppositeHash = subMaterialHash.getOpposite();
		final boolean addDirect;

		if (subMaterialHash.isBalancedExceptFor(PieceType.NONE))
			addDirect = (subMaterialHash.getOnTurn() == Color.WHITE);
		else
			addDirect = subMaterialHash.isGreater (oppositeHash);
		
		if (addDirect)
			neededSubtables.add(subMaterialHash);
		else
			neededSubtables.add(oppositeHash);
	}

	public void setUsePersistentTable (final boolean use) {
		this.usePersistentTable = use;
	}
	
	public void setUseCompressedTable (final boolean use) {
		this.useCompressedTable = use;
	}

}
