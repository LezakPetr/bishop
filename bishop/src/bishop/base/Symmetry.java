package bishop.base;

public class Symmetry {
	
	public static final int ROTATE_MASK = 1;   // Counter clockwise rotation
	public static final int FLIP_FILE_MASK = 2;
	public static final int FLIP_RANK_MASK = 4;
	
	public static final int FIRST = 0;
	
	public static final int IDENTITY = 0;
	public static final int ROTATE = ROTATE_MASK;
	public static final int FLIP_FILE = FLIP_FILE_MASK;
	public static final int ROTATE_FLIP_FILE = ROTATE_MASK | FLIP_FILE_MASK;	
	public static final int FLIP_RANK = FLIP_RANK_MASK;
	public static final int ROTATE_FLIP_RANK = ROTATE_MASK | FLIP_RANK_MASK;
	public static final int FLIP_FILE_RANK = FLIP_FILE_MASK | FLIP_RANK_MASK;
	public static final int ROTATE_FLIP_FILE_RANK = ROTATE_MASK | FLIP_FILE_MASK | FLIP_RANK_MASK;
	
	public static final int LAST = 8;
	
	public static final int BIT_COUNT = 3;
	
	private static final int[] INVERSE_SYMMETRIES = { IDENTITY, ROTATE_FLIP_FILE_RANK, FLIP_FILE, ROTATE_FLIP_FILE, FLIP_RANK, ROTATE_FLIP_RANK, FLIP_FILE_RANK, ROTATE };
	

	public static int flipX (final int symmetry) {
		return symmetry ^ FLIP_FILE_MASK;
	}

	public static int flipY (final int symmetry) {
		return symmetry ^ FLIP_RANK_MASK;
	}

	public static int rotateClockwise (final int symmetry) {
		int result = symmetry ^ ROTATE_MASK;
		
		if ((result & ROTATE_MASK) != 0)
			result = result ^ FLIP_FILE_MASK ^ FLIP_RANK_MASK;
		
		return result;
	}

	public static int rotateCounterClockwise (final int symmetry) {
		int result = symmetry ^ ROTATE_MASK;
		
		if ((result & ROTATE_MASK) == 0)
			result = result ^ FLIP_FILE_MASK ^ FLIP_RANK_MASK;
		
		return result;
	}

	public static int getInverseSymmetry(final int symmetry) {
		return INVERSE_SYMMETRIES[symmetry];
	}

}
