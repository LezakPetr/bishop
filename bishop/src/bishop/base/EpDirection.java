package bishop.base;

public class EpDirection {
	public static final int FIRST = 0;

	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	public static final int LAST = 2;
	public static final int NONE = 4;
	
	private static final int[] FILE_OFFSETS = { -1, +1 };

	
	public static final int getSourceFile (final int targetFile, final int direction) {
		return targetFile + FILE_OFFSETS[direction];
	}
}
