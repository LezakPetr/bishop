package bishop.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bishop.base.Move;
import bishop.base.Position;

public class BookRecord {

	private final Position position;
	private final List<BookMove> moveList;
	
	public BookRecord() {
		position = new Position();
		moveList = new ArrayList<BookMove>();
	}

	public Position getPosition() {
		return position;
	}
	
	public BookMove findMove (final Move move) {
		for (BookMove bookMove: moveList) {
			if (bookMove.getMove().equals(move)) {
				return bookMove;
			}
		}
		
		return null;
	}
	
	public int getMoveCount() {
		return moveList.size();
	}
	
	public BookMove getMoveAt (final int index) {
		return moveList.get(index);
	}
	
	public void addMove (final BookMove bookMove) {
		if (findMove(bookMove.getMove()) == null)
			moveList.add(bookMove);
	}
	
	/**
	 * Returns random good move (without bad annotations).
	 * @param random random number generator
	 * @return move
	 */
	public BookMove getRandomMove(final Random random) {
		final List<BookMove> goodMoveList = new ArrayList<BookMove>();
		
		for (BookMove move: moveList) {
			if (move.getAnnotation().isGoodMove()) {
				goodMoveList.add(move);
			}
		}
		
		final int moveCount = goodMoveList.size();
		
		if (moveCount <= 0)
			return null;
		
		final int index = random.nextInt(moveCount);
		
		return this.getMoveAt(index);
	}
	
	public void assign(final BookRecord orig) {
		this.position.assign(orig.position);
		
		this.moveList.clear();
		this.moveList.addAll(orig.moveList);
	}
	
}
