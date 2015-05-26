package bishop.tablebase;

import bishop.base.IMoveWalker;
import bishop.base.Move;
import bishop.base.Position;

public class ForwardTableWalker implements IMoveWalker {
	
	private Position position;
	private IPositionResultSource resultSource;
	
	private boolean moveFound;
	private int result;
	
	
	public ForwardTableWalker() {
	}
	
	public void setPosition (final Position position) {
		this.position = position;
	}
	
	public void setResultSource (final IPositionResultSource resultSource) {
		this.resultSource = resultSource;
	}
	
	public boolean isMoveFound() {
		return moveFound;
	}
	
	public int getResult() {
		return result;
	}
	
	public void clear() {
		result = TableResult.MATE;
		moveFound = false;
	}
	
	@Override
	public boolean processMove(final Move move) {
		position.makeMove(move);
		
		final int oppositeResult = resultSource.getPositionResult(position);
		final int currentResult = TableResult.getOpposite(oppositeResult);
		
		if (currentResult != TableResult.ILLEGAL) {
			moveFound = true;
			
			if (currentResult > result)
				result = currentResult;
		}
		
		position.undoMove(move);
		
		return true;
	}

}
