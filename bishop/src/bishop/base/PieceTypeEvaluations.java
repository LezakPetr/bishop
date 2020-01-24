package bishop.base;

import math.Utils;
import utils.IntArrayBuilder;
import utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public final class PieceTypeEvaluations {

	public static final int SIZE = PieceType.LAST + 1;

	public static final int PAWN_EVALUATION = 1000;
	public static final int KING_EVALUATION = 0;
	public static final int NONE_EVALUATION = 0;

	public static final PieceTypeEvaluations DEFAULT = new PieceTypeEvaluations(
			new IntArrayBuilder(SIZE)
			.put(PieceType.PAWN, PAWN_EVALUATION)
			.put(PieceType.KNIGHT, 3 * PAWN_EVALUATION)
			.put(PieceType.BISHOP, 3 * PAWN_EVALUATION)
			.put(PieceType.ROOK, 5 * PAWN_EVALUATION)
			.put(PieceType.QUEEN, 9 * PAWN_EVALUATION)
			.put(PieceType.KING, KING_EVALUATION)
			.put(PieceType.NONE, NONE_EVALUATION)
			.build()
	);

	private final int[] pieceTypeEvaluations;

	private PieceTypeEvaluations (final int[] pieceTypeEvaluations) {
		this.pieceTypeEvaluations = pieceTypeEvaluations;
	}

	public static int getPawnEvaluation (final int color) {
		return Color.colorNegate(color, PAWN_EVALUATION);
	}

	
	public int getPieceTypeEvaluation (final int pieceType) {
		return pieceTypeEvaluations[pieceType];
	}

	public int getPieceEvaluation(final int color, final int pieceType) {
		return Color.colorNegate(color, pieceTypeEvaluations[pieceType]);
	}
	
	public static int getPawnMultiply (final double multiplier) {
		return Utils.roundToInt (multiplier * PieceTypeEvaluations.PAWN_EVALUATION);
	}

	public static PieceTypeEvaluations read (final InputStream stream) throws IOException {
		final int[] pieceTypeEvaluations = new int[PieceTypeEvaluations.SIZE];

		for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++)
			pieceTypeEvaluations[pieceType] = (int) IoUtils.readSignedNumberBinary(stream, IoUtils.SHORT_BYTES);

		setFixedEvaluations(pieceTypeEvaluations);

		return new PieceTypeEvaluations(pieceTypeEvaluations);
	}

	private static void setFixedEvaluations(final int[] pieceTypeEvaluations) {
		pieceTypeEvaluations[PieceType.KING] = KING_EVALUATION;
		pieceTypeEvaluations[PieceType.PAWN] = PAWN_EVALUATION;
		pieceTypeEvaluations[PieceType.NONE] = NONE_EVALUATION;
	}

	public void write(final OutputStream stream) throws IOException {
		for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++)
			IoUtils.writeNumberBinary(stream, pieceTypeEvaluations[pieceType], IoUtils.SHORT_BYTES);
	}

	public static PieceTypeEvaluations of (final int[] pieceTypeEvaluations) {
		final int[] copy = Arrays.copyOf(pieceTypeEvaluations, PieceTypeEvaluations.SIZE);
		setFixedEvaluations(copy);

		return new PieceTypeEvaluations(copy);
	}
}
