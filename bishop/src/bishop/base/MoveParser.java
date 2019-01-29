package bishop.base;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MoveParser {

	private final IMoveWalker walker = new IMoveWalker() {
		public boolean processMove (final Move move) {
			final Move moveCopy = new Move();
			moveCopy.assign(move);
			
			moveList.add(moveCopy);
			
			return true;
		}
	};
	
	private final LegalMoveGenerator generator;
	private final List<Move> moveList;
	
	public MoveParser() {
		generator = new LegalMoveGenerator();
		generator.setWalker(walker);
		
		moveList = new LinkedList<>();
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
		moveList.removeIf(move -> move.getMovingPieceType() != movingPieceType);
	}
	
	/**
	 * Removes all moves with different begin square.
	 * @param square begin square
	 */
	public void filterByBeginSquare (final int square) {
		moveList.removeIf(move -> move.getBeginSquare() != square);
	}

	/**
	 * Removes all moves with different begin file.
	 * @param file begin file
	 */
	public void filterByBeginFile (final int file) {
		moveList.removeIf(move -> Square.getFile(move.getBeginSquare()) != file);
	}

	/**
	 * Removes all moves with different begin rank.
	 * @param rank begin rank
	 */
	public void filterByBeginRank (final int rank) {
		moveList.removeIf(move -> Square.getRank(move.getBeginSquare()) != rank);
	}

	/**
	 * Removes all moves with different target square.
	 * @param square target square
	 */
	public void filterByTargetSquare (final int square) {
		moveList.removeIf(move -> move.getTargetSquare() != square);
	}

	/**
	 * Removes all moves with different type of promotion piece.
	 * @param promotionPieceType type of promotion piece
	 */
	public void filterByPromotionPieceType (final int promotionPieceType) {
		moveList.removeIf(move -> move.getPromotionPieceType() != promotionPieceType);
	}
	
	public void filterByMoveType(final int moveType) {
		moveList.removeIf(move -> move.getMoveType() != moveType);
	}
	
	/**
	 * Returns generated and later filtered list of moves.
	 * @return list of moves
	 */
	public List<Move> getMoveList() {
		return moveList;
	}

}
