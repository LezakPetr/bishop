package bishop.engine;

import java.util.Arrays;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public class AttackCalculator {

	public static final int MIN_ATTACK_EVALUATION = 0;
	public static final int MAX_REASONABLE_ATTACK_EVALUATION = 300;

	private static final int QUEEN_ATTACK_COEFF = 2;
	private static final int ROOK_ATTACK_COEFF = 2;
	private static final int BISHOP_ATTACK_COEFF = 1;
	private static final int KNIGHT_ATTACK_COEFF = 1;
	private static final int PAWN_ATTACK_COEFF = 2;
	private static final int TOTAL_ATTACK_COEFF = 1;

	private final long[] directlyAttackedSquares = new long[Color.LAST];   // Squares attacked by some piece
	private final int[] mobility = new int[PieceType.LAST];
	private final int[] attackEvaluation = new int[Color.LAST];   // Attack evaluation for given color, always positive

	public void calculate(final Position position, final MobilityCalculator mobilityCalculator) {
		calculateMobility(position, mobilityCalculator);
		calculateAttackEvaluation(position, mobilityCalculator);
	}
	
	private void calculateMobility(final Position position, final MobilityCalculator mobilityCalculator) {
		Arrays.fill(mobility, 0);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);

			final long pawnsMask = position.getPiecesMask(oppositeColor, PieceType.PAWN);
			final long oppositePawnAttacks = BoardConstants.getPawnsAttackedSquares(oppositeColor, pawnsMask);
			final long freeSquares = ~(oppositePawnAttacks | position.getColorOccupancy(color));

			// Bishop
			final long bishopAttackedSquares = mobilityCalculator.getBishopAttackedSquares(color);
			mobility[PieceType.BISHOP] += Color.colorNegate(color, BitBoard.getSquareCount(bishopAttackedSquares & freeSquares));

			// Rook
			final long rookAttackedSquares = mobilityCalculator.getRookAttackedSquares(color);
			mobility[PieceType.ROOK] += Color.colorNegate(color, BitBoard.getSquareCount(rookAttackedSquares & freeSquares));

			// Queen
			final long queenAttackedSquares = mobilityCalculator.getQueenAttackedSquares(color);
			mobility[PieceType.QUEEN] += Color.colorNegate(color, BitBoard.getSquareCount(queenAttackedSquares & freeSquares));
			
			// Knight
			final long knightAttackedSquares = mobilityCalculator.getKnightAttackedSquares(color);
			mobility[PieceType.KNIGHT] += Color.colorNegate(color, BitBoard.getSquareCount(knightAttackedSquares & freeSquares));

			directlyAttackedSquares[color] = mobilityCalculator.getAllAttackedSquares(color);
		}
	}

	private void calculateAttackEvaluation (final Position position, final MobilityCalculator mobilityCalculator) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final int kingSquare = position.getKingPosition(oppositeColor);
			final long nearAttackableMask = BoardConstants.getKingNearSquares(kingSquare);
			final long farAttackableMask = BoardConstants.getKingSafetyFarSquares(kingSquare);

			int evaluation = 0;

			evaluation += KNIGHT_ATTACK_COEFF * BitBoard.getSquareCountSparse(nearAttackableMask & mobilityCalculator.getKnightAttackedSquares(color));
			evaluation += BISHOP_ATTACK_COEFF * BitBoard.getSquareCountSparse(nearAttackableMask & mobilityCalculator.getBishopAttackedSquares(color));
			evaluation += ROOK_ATTACK_COEFF * BitBoard.getSquareCountSparse(nearAttackableMask & mobilityCalculator.getRookAttackedSquares(color));
			evaluation += QUEEN_ATTACK_COEFF * BitBoard.getSquareCountSparse(nearAttackableMask & mobilityCalculator.getQueenAttackedSquares(color));
			evaluation += PAWN_ATTACK_COEFF * BitBoard.getSquareCountSparse(nearAttackableMask & mobilityCalculator.getPawnAttackedSquares(color));

			evaluation += TOTAL_ATTACK_COEFF * BitBoard.getSquareCount(farAttackableMask & mobilityCalculator.getAllAttackedSquares(color));

			attackEvaluation[color] = evaluation;
		}
	}

	public int getMobility (final int pieceType) {
		return mobility[pieceType];
	}
	
	public int getAttackEvaluation(final int color) {
		return attackEvaluation[color];
	}

	public long getDirectlyAttackedSquares(final int color) {
		return directlyAttackedSquares[color];
	}

}
