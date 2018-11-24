package bishop.engine;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.PieceType;
import bishop.base.Position;

public class DrawChecker {
	
	public static final int LIVE_POSITION = 0;
	public static final int THEORETICAL_DRAW = 1;
	public static final int DEAD_POSITION = 2;
	
	
	public static final int evaluatePosition (final Position position) {
		// Check alone mating pieces
		final IMaterialHashRead materialHash = position.getMaterialHash();
		
		if (materialHash.hasQueenRookOrPawn())
			return LIVE_POSITION;
		
		// Now we have a position with just light figures
		final long allKnightMask = position.getBothColorPiecesMask(PieceType.KNIGHT);
		final long allBishopMask = position.getBothColorPiecesMask(PieceType.BISHOP);
		
		int effectiveCount = BitBoard.getSquareCount(allKnightMask);
		
		if ((allBishopMask & BoardConstants.WHITE_SQUARE_MASK) != 0)
			effectiveCount++;
		
		if ((allBishopMask & BoardConstants.BLACK_SQUARE_MASK) != 0)
			effectiveCount++;
		
		if (effectiveCount <= 1)
			return DEAD_POSITION;
		
		return LIVE_POSITION;
	}
	
	/**
	 * Checks if one player can normally win the position.
	 * @param position position to check
	 * @return true if one player can theoretically force mate, false if not
	 */
	public static boolean isDraw (final Position position) {
		final int evaluation = evaluatePosition(position);
		
		return evaluation != LIVE_POSITION;
	}

	/**
	 * Checks if there is dead position.
	 * @param position position to check
	 * @return true if no player can be mated by any sequence of legal moves, false not
	 */
	public static boolean isDeadPosition (final Position position) {
		final int evaluation = evaluatePosition(position);
		
		return evaluation == DEAD_POSITION;
	}
	
	public static boolean hasMatingMaterial (final IMaterialHashRead materialHash, final int matingColor) {
		if (materialHash.hasQueenRookOrPawnOnSide(matingColor))
			return true;
		
		// Two bishops
		final int bishopCount = materialHash.getPieceCount(matingColor, PieceType.BISHOP);
		
		if (bishopCount >= 2)
			return true;
		
		final int knightCount = materialHash.getPieceCount(matingColor, PieceType.KNIGHT);
		
		// Bishop and knight
		if (bishopCount > 0 && knightCount > 0)
			return true;
		
		// Three light figures
		if (bishopCount + knightCount >= 3)
			return true;
		
		return false;
	}

}
