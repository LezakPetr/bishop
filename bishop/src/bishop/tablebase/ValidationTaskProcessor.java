package bishop.tablebase;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

import bishop.base.Fen;
import bishop.base.Position;
import bishop.base.PositionValidator;
import bishop.base.PseudoLegalMoveGenerator;


public class ValidationTaskProcessor implements Callable<Boolean> {
	
	private final Position position;
	private final PositionValidator positionValidator;
	private final PseudoLegalMoveGenerator moveGenerator;
	private final ForwardTableWalker moveWalker;
	
	private TableDefinition tableDefinition;
	private ITableIteratorRead iterator; 
	private long count;
	
	
	public ValidationTaskProcessor(final TableSwitch resultSource) {
		this.position = new Position();
		this.moveGenerator = new PseudoLegalMoveGenerator();
		this.moveWalker = new ForwardTableWalker();
		
		moveWalker.setPosition(position);
		moveWalker.setResultSource(resultSource);
		
		moveGenerator.setPosition(position);
		moveGenerator.setWalker(moveWalker);
		
		positionValidator = new PositionValidator();
		positionValidator.setPosition(position);
	}
	
	public void initialize(final TableDefinition tableDefinition, final ITableIteratorRead beginIter, final long count) {
		this.tableDefinition = tableDefinition;
		this.iterator = beginIter.copy();
		this.count = count;
	}

	@Override
	public Boolean call() throws Exception {
		while (count > 0) {
			final int realResult = iterator.getResult();
			final int expectedResult;
			
			iterator.fillPosition(position);
			
			final boolean isValid = tableDefinition.hasSameCountOfPieces(position) && positionValidator.checkPosition();
			
			if (isValid) {
				moveWalker.clear();
				moveGenerator.generateMoves();
				
				// Mate or stalemate
				if (moveWalker.isMoveFound())
					expectedResult = moveWalker.getResult();
				else {
					if (position.isCheck())
						expectedResult = TableResult.MATE;
					else
						expectedResult = TableResult.DRAW;
				}
			}
			else {
				expectedResult = TableResult.ILLEGAL;
			}
			
			if (realResult != expectedResult) {
				final Fen fen = new Fen();
				fen.getPosition().assign(position);
				
				final PrintWriter writer = new PrintWriter(System.err);
				fen.writeFen(writer);
				writer.flush();
				
				System.err.println("Calculated: " + realResult + ", expected: " + expectedResult);
				
				return false;
			}

			iterator.next();
			count--;
		}
		
		return true;
	}

}
