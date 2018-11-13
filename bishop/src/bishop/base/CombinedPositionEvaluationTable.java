package bishop.base;

import bishop.engine.CombinedEvaluation;
import bishop.engine.PositionEvaluationCoeffs;

public class CombinedPositionEvaluationTable {

	private static final int PIECE_TYPE_SHIFT = 6;
	private static final int COLOR_SHIFT = 9;
	private static final int TOTAL_BITS = 1 + 3 + 6;

	private final long[] combinedEvaluations;

	public static CombinedPositionEvaluationTable ZERO_TABLE = new CombinedPositionEvaluationTable();

	private static int getIndex (final int color, final int pieceType, final int square) {
		return (color << COLOR_SHIFT) + (pieceType << PIECE_TYPE_SHIFT) + square;
	}

	public CombinedPositionEvaluationTable() {
		combinedEvaluations = new long[1 << TOTAL_BITS];
	}

	private int getCoeff (final PositionEvaluationCoeffs evaluationCoeffs, final int color, final int pieceType, final int square, final int gameStageIndex) {
		final int coeffIndex = PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.get(gameStageIndex).getCoeff(color, pieceType, square);
		final int coeff = evaluationCoeffs.getEvaluationCoeff(coeffIndex);

		if (color == Color.WHITE)
			return coeff;
		else
			return -coeff;
	}

	public CombinedPositionEvaluationTable(final PositionEvaluationCoeffs evaluationCoeffs) {
		this();

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final long board = BoardConstants.getPieceAllowedSquares(pieceType);

				for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
					final int square = loop.getNextSquare();
					final int index = getIndex(color, pieceType, square);

					combinedEvaluations[index] = CombinedEvaluation.combine(
							getCoeff(evaluationCoeffs, color, pieceType, square, CombinedEvaluation.COMPONENT_OPENING),
							getCoeff(evaluationCoeffs, color, pieceType, square, CombinedEvaluation.COMPONENT_MIDDLE_GAME),
							getCoeff(evaluationCoeffs, color, pieceType, square, CombinedEvaluation.COMPONENT_ENDING)
					);
				}
			}
		}
	}

	public long getCombinedEvaluation(final int color, final int pieceType, final int square) {
		final int index = getIndex(color, pieceType, square);

		return combinedEvaluations[index];
	}

}
