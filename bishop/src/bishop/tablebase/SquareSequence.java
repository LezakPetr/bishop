package bishop.tablebase;

import java.util.Arrays;

import bishop.base.Square;

public class SquareSequence {
	
	private static final byte[] CENTRAL_SEQUENCE = createSequence(
		63, 62, 61, 60, 59, 58, 57, 56,
		36, 35, 34, 33, 32, 31, 30, 55,
		37, 16, 15, 14, 13, 12, 29, 54,
		38, 17,  4,  3,  2, 11, 28, 53,
		39, 18,  5,  0,  1, 10, 27, 52,
		40, 19,  6,  7,  8,  9, 26, 51,
		41, 20, 21, 22, 23, 24, 25, 50,
		42, 43, 44, 45, 46, 47, 48, 49
	);

	private static final byte[] WHITE_PAWN_SEQUENCE = createSequence(
		48, 49, 50, 51, 52, 53, 54, 55,
		 5, 11, 17, 23, 29, 35, 41, 47,
		 4, 10, 16, 22, 28, 34, 40, 46,
		 3,  9, 15, 21, 27, 33, 39, 45,
		 2,  8, 14, 20, 26, 32, 38, 44,
		 1,  7, 13, 19, 25, 31, 37, 43,
		 0,  6, 12, 18, 24, 30, 36, 42,
		56, 57, 58, 59, 60, 61, 62, 63
	);
	
	private static final byte[] BLACK_PAWN_SEQUENCE = createSequence(
			56, 57, 58, 59, 60, 61, 62, 63,
			 0,  6, 12, 18, 24, 30, 36, 42,
			 1,  7, 13, 19, 25, 31, 37, 43,
			 2,  8, 14, 20, 26, 32, 38, 44,
			 3,  9, 15, 21, 27, 33, 39, 45,
			 4, 10, 16, 22, 28, 34, 40, 46,
			 5, 11, 17, 23, 29, 35, 41, 47,
			48, 49, 50, 51, 52, 53, 54, 55
		);

	private static final byte[] BISHOP_SEQUENCE = createSequence(
		63, 31, 62, 30, 61, 29, 60, 28,
		18, 49, 17, 48, 16, 47, 15, 59,
		50,  8, 39,  7, 38,  6, 46, 27,
		19, 40,  2, 33,  1, 37, 14, 58,
		51,  9, 34,  0, 32,  5, 45, 26,
		20, 41,  3, 35,  4, 36, 13, 57,
		52, 10, 42, 11, 43, 12, 44, 25,
		21, 53, 22, 54, 23, 55, 24, 56
	);
	
	private static byte[] createSequence(final int ...order) {
		final byte[] sequence = new byte[LAST_INDEX];
		Arrays.fill(sequence, (byte) -1);
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int currentOrder = order[square];
			
			if (sequence[currentOrder] >= 0)
				throw new RuntimeException("Wrong sequence");
			
			sequence[currentOrder] = (byte) square;
		}
	
		return sequence;
	}
	
	private static final byte[][][] SEQUENCES = {
		// King                 queen              rook             bishop           knight           pawn
		{CENTRAL_SEQUENCE, CENTRAL_SEQUENCE, CENTRAL_SEQUENCE, BISHOP_SEQUENCE, CENTRAL_SEQUENCE, WHITE_PAWN_SEQUENCE},
		{CENTRAL_SEQUENCE, CENTRAL_SEQUENCE, CENTRAL_SEQUENCE, BISHOP_SEQUENCE, CENTRAL_SEQUENCE, BLACK_PAWN_SEQUENCE},
	};

	public static final int FIRST_INDEX = 0;
	public static final int LAST_INDEX = Square.LAST - Square.FIRST;
	
	public static int getSquareOnIndex (final int color, final int pieceType, final int index) {
		return SEQUENCES[color][pieceType][index];
	}
}
