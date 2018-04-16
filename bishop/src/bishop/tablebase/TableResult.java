package bishop.tablebase;

public class TableResult {
	
	public static final int BIT_COUNT = 14;   // Result is designed in a way that it can be written in 14 bits
	public static final int ILLEGAL = -(1 << (BIT_COUNT - 1));
	public static final int DRAW = 0;
	public static final int MATE = ILLEGAL + 10;
	
	public static final int UNKNOWN_MATERIAL = ILLEGAL - 10;   // Internal error, outside of BIT_COUNT bits
	
	public static final byte COMPRESSED_ILLEGAL = Byte.MIN_VALUE;
	public static final byte COMPRESSED_DRAW = 0;
	public static final byte COMPRESSED_MATE = -Byte.MAX_VALUE;
	
	public static int getOpposite (final int result) {
		// Win => Lose
		if (isWin (result)) {
			return -result;
		}
		
		// Lose => Win
		if (isLose (result)) {
			return -result - 1;
		}
		
		// Illegal and draw
		return result;
	}
	
	public static boolean isWin(final int result) {
		return result > 0 && result <= -MATE;
	}
	
	public static boolean isLose(final int result) {
		return result >= MATE && result < 0;
	}
	
	public static int getWinDepth (final int result) {
		return -(result + MATE);
	}

	public static int getLoseDepth (final int result) {
		return result - MATE;
	}
	
	public static boolean canBeCompressed(final int result) {
		if (result == DRAW || result == ILLEGAL)
			return true;
		
		if (isWin (result))
			return result + MATE - COMPRESSED_MATE > 0;
		
		if (isLose (result))
			return result - MATE + COMPRESSED_MATE < 0;
		
		return false;
	}

	public static byte compress (final int result) {
		if (!canBeCompressed(result))
			throw new RuntimeException("Result cannot be compressed: " + result);
		
		if (result == DRAW)
			return COMPRESSED_DRAW;
		
		if (result == ILLEGAL)
			return COMPRESSED_ILLEGAL;
		
		if (result > 0) {
			return (byte) (result + MATE - COMPRESSED_MATE);
		}
		else {
			return (byte) (result - MATE + COMPRESSED_MATE);
		}
	}
	
	public static int decompress (final byte compressedResult) {
		if (compressedResult == COMPRESSED_DRAW)
			return DRAW;
		
		if (compressedResult == COMPRESSED_ILLEGAL)
			return ILLEGAL;
		
		if (compressedResult > 0) {
			return compressedResult - MATE + COMPRESSED_MATE;
		}
		else {
			return compressedResult + MATE - COMPRESSED_MATE;
		}
	}

	public static int getClassification (final int result) {
		if (result == DRAW)
			return Classification.DRAW;

		if (result == ILLEGAL)
			return Classification.ILLEGAL;

		if (isWin (result))
			return Classification.WIN;

		if (isLose (result))
			return Classification.LOSE;

		throw new RuntimeException("Unknown result " + result);
	}

	public static String toString(final int result) {
		if (result == DRAW)
			return "draw";
		
		if (result == ILLEGAL)
			return "illegal";
		
		if (isWin(result))
			return "Win in " + getWinDepth(result);

		if (isLose(result))
			return "Lose in " + getLoseDepth(result);

		throw new RuntimeException("Unknown result: " + result);
	}

}
