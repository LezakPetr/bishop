package bishop.engine;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.Square;

public class RuleOfSquare {
	
	private static final long[][][] RULE_OF_SQUARE_TABLE = initializeRuleOfSquareTable();
	
	private static long[][][] initializeRuleOfSquareTable() {
		final long[][][] result = new long[Color.LAST][Color.LAST][Square.LAST];
		
		for (int pawnColor = Color.FIRST; pawnColor < Color.LAST; pawnColor++) {
			for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
				for (int pawnSquare = Square.FIRST; pawnSquare < Square.LAST; pawnSquare++) {
					long board = calculateRuleOfSquareItem(pawnColor, onTurn, pawnSquare);
					
					result[pawnColor][onTurn][pawnSquare] = board;
				}
			}
		}
		
		return result;
	}

	private static long calculateRuleOfSquareItem(final int pawnColor, final int onTurn, final int pawnSquare) {
		final int promotionSquare = BoardConstants.getPawnPromotionSquare(pawnColor, pawnSquare);
		final int pawnDistance = BoardConstants.getPawnPromotionDistance(pawnColor, pawnSquare);
		long board = 0;
		
		for (int defenderKingSquare = Square.FIRST; defenderKingSquare < Square.LAST; defenderKingSquare++) {
			int kingDistance = BoardConstants.getKingSquareDistance(defenderKingSquare, promotionSquare);
			
			if (onTurn != pawnColor)
				kingDistance--;
			
			if (kingDistance <= pawnDistance)
				board |= BitBoard.getSquareMask(defenderKingSquare);
		}
		return board;
	}
	
	public static long getKingDefendingSquares (final int pawnColor, final int onTurn, final int pawnSquare) {
		return RULE_OF_SQUARE_TABLE[pawnColor][onTurn][pawnSquare];
	}
	
}
