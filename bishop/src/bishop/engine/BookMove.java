package bishop.engine;

import bishop.base.Move;

public final class BookMove {

	private Move move;
	private byte relativeMoveRepetition;
	private byte targetPositionBalance;
	
	public BookMove() {
		move = null;
	}

	public Move getMove() {
		return move;
	}

	public void setMove(final Move move) {
		this.move = move;
	}

	public BookMove copy() {
		final BookMove move = new BookMove();
		move.relativeMoveRepetition = this.relativeMoveRepetition;
		move.targetPositionBalance = this.targetPositionBalance;
		move.move = this.move.copy();
		
		return move;
	}
	
	public int getRelativeMoveRepetition() {
		return relativeMoveRepetition;
	}

	public void setRelativeMoveRepetition(final int relativaMoveRepetition) {
		this.relativeMoveRepetition = (byte) relativaMoveRepetition;
	}

	public int getTargetPositionBalance() {
		return targetPositionBalance;
	}

	public void setTargetPositionBalance(final int targetPositionBalance) {
		this.targetPositionBalance = (byte) targetPositionBalance;
	}
	
	@Override
	public boolean equals (final Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		
		final BookMove that = (BookMove) obj;
		
		return this.move.equals(that.move)
				&& this.relativeMoveRepetition == that.relativeMoveRepetition
				&& this.targetPositionBalance == that.targetPositionBalance;
	}
	
	@Override
	public int hashCode() {
		int hash = move.hashCode();
		hash = 31 * hash + relativeMoveRepetition;
		hash = 31 * hash + targetPositionBalance;
		
		return hash;
	}
	
	@Override
	public String toString() {
		return move.toString() + " " + relativeMoveRepetition + " " + targetPositionBalance;
	}
	
}
