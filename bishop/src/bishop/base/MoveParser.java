package bishop.base;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MoveParser {

	private IMoveWalker walker = new IMoveWalker() {
		public boolean processMove (final Move move) {
			final Move moveCopy = new Move();
			moveCopy.assign(move);
			
			moveList.add(moveCopy);
			
			return true;
		}
	};
	
	private LegalMoveGenerator generator;
	private List<Move> moveList;
	
	public MoveParser() {
		generator = new LegalMoveGenerator();
		generator.setWalker(walker);
		
		moveList = new LinkedList<Move>();
	}
	
	/**
	 * Generates all moves in given position.
	 * @param position position
	 */
	public void initPosition (final Position position) {
		moveList.clear();
		
		generator.setPosition(position);
		generator.generateMoves();
	}
	
	/**
	 * Removes all moves with different type of moving piece.
	 * @param movingPieceType type of moving piece
	 */
	public void filterByMovingPieceType (final int movingPieceType) {
		for (Iterator<Move> it = moveList.iterator(); it.hasNext(); ) {
			final Move move = it.next();
			
			if (move.getMovingPieceType() != movingPieceType)
				it.remove();
		}
	}
	
	/**
	 * Removes all moves with different begin square.
	 * @param square begin square
	 */
	public void filterByBeginSquare (final int square) {
		for (Iterator<Move> it = moveList.iterator(); it.hasNext(); ) {
			final Move move = it.next();
			
			if (move.getBeginSquare() != square)
				it.remove();
		}
	}

	/**
	 * Removes all moves with different begin file.
	 * @param file begin file
	 */
	public void filterByBeginFile (final int file) {
		for (Iterator<Move> it = moveList.iterator(); it.hasNext(); ) {
			final Move move = it.next();
			
			if (Square.getFile(move.getBeginSquare()) != file)
				it.remove();
		}
	}

	/**
	 * Removes all moves with different begin rank.
	 * @param rank begin rank
	 */
	public void filterByBeginRank (final int rank) {
		for (Iterator<Move> it = moveList.iterator(); it.hasNext(); ) {
			final Move move = it.next();
			
			if (Square.getRank(move.getBeginSquare()) != rank)
				it.remove();
		}
	}

	/**
	 * Removes all moves with different target square.
	 * @param square target square
	 */
	public void filterByTargetSquare (final int square) {
		for (Iterator<Move> it = moveList.iterator(); it.hasNext(); ) {
			final Move move = it.next();
			
			if (move.getTargetSquare() != square)
				it.remove();
		}
	}

	/**
	 * Removes all moves with different type of promotion piece.
	 * @param promotionPieceType type of promotion piece
	 */
	public void filterByPromotionPieceType (final int promotionPieceType) {
		for (Iterator<Move> it = moveList.iterator(); it.hasNext(); ) {
			final Move move = it.next();
			
			if (move.getPromotionPieceType() != promotionPieceType)
				it.remove();
		}
	}
	
	public void filterByMoveType(final int moveType) {
		for (Iterator<Move> it = moveList.iterator(); it.hasNext(); ) {
			final Move move = it.next();
			
			if (move.getMoveType() != moveType)
				it.remove();
		}
	}
	
	/**
	 * Returns generated and later filtered list of moves.
	 * @return list of moves
	 */
	public List<Move> getMoveList() {
		return moveList;
	}

}
