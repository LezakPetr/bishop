package bishop.tablebase;

import java.util.concurrent.Callable;

import bishop.base.Position;
import bishop.base.PseudoLegalMoveGenerator;
import bishop.base.ReverseMoveGenerator;

public class CalculationTaskProcessor implements Callable<Object> {

	private boolean firstIteration;
	private BitArray prevPositionsToCheck;
	private BitArray nextPositionsToCheck;

	private final Position position;
	private final PseudoLegalMoveGenerator moveGenerator;
	private final ForwardTableWalker moveWalker;
	
	private final ReverseMoveGenerator reverseMoveGenerator;
	private final ReverseTableWalker reverseMoveWalker;
	
	private long changeCount;
	private ITableIterator iterator;
	private long count;	
	
	public CalculationTaskProcessor(final TableSwitch resultSource) {
		this.position = new Position();
		
		this.moveWalker = new ForwardTableWalker();
		this.moveGenerator = new PseudoLegalMoveGenerator();
		this.moveWalker.setPosition(position);
		this.moveWalker.setResultSource(resultSource);
		
		this.moveGenerator.setWalker(moveWalker);
		this.moveGenerator.setPosition(position);
		
		this.reverseMoveWalker = new ReverseTableWalker();
		this.reverseMoveWalker.setPosition(position);
		
		this.reverseMoveGenerator = new ReverseMoveGenerator();
		
		this.reverseMoveGenerator.setWalker(reverseMoveWalker);
		this.reverseMoveGenerator.setPosition(position);
	}
	
	public void initialize(final boolean firstIteration, final TableDefinition oppositeTableDefinition, final BitArray prevPositionsToCheck, final ITableIterator beginIter, final long count) {
		this.firstIteration = firstIteration;
		this.prevPositionsToCheck = prevPositionsToCheck;
		this.iterator = beginIter.copy();
		this.count = count;
		
		final long itemCount = oppositeTableDefinition.getTableIndexCount();
				
		this.nextPositionsToCheck = new BitArray(itemCount);
		this.reverseMoveWalker.setNextPositionsToCheck (nextPositionsToCheck);
		this.changeCount = 0;
		
		this.reverseMoveWalker.setTableDefinition(oppositeTableDefinition);
	}

	public BitArray getPrevPositionsToCheck() {
		return prevPositionsToCheck;
	}

	public void setPrevPositionsToCheck(final BitArray prevPositionsToCheck) {
		this.prevPositionsToCheck = prevPositionsToCheck;
	}

	public BitArray getNextPositionsToCheck() {
		return nextPositionsToCheck;
	}

	public long getChangeCount() {
		return changeCount;
	}

	@Override
	public Object call() throws Exception {
		try {
			while (count > 0) {
				if (firstIteration || prevPositionsToCheck.getAt(iterator.getTableIndex())) {
					final int oldResult = iterator.getResult();
					
					if (oldResult != TableResult.ILLEGAL) {
						iterator.fillPosition(position);
						
						moveWalker.clear();
						moveGenerator.generateMoves();
						
						final int result = moveWalker.getResult();
						
						if (result != oldResult && moveWalker.isMoveFound()) {
							changeCount++;
							
							iterator.setResult (result);
							reverseMoveGenerator.generateMoves();
						}
					}
				}
				
				iterator.next();
				count--;
			}
			
			return null;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}
}
