package bishop.tablebase;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.PieceType;

public class FileNameCalculator {
	
	public static final char MATERIAL_SEPARATOR = '-';
	public static final char ON_TURN_SEPARATOR = '-';
	public static final String SUFFIX = ".tbs";
	
	// wwwww-bbbbb-c.tbbs
	private static final int SINGLE_MATERIAL_LENGTH = 5;
	
	private static final int MATERIAL_BEGIN = 0;
	private static final int WHITE_BEGIN = MATERIAL_BEGIN;
	private static final int WHITE_END = WHITE_BEGIN + SINGLE_MATERIAL_LENGTH;
	private static final int MATERIAL_SEPARATOR_POS = WHITE_END;
	private static final int BLACK_BEGIN = MATERIAL_SEPARATOR_POS + 1;
	private static final int BLACK_END = BLACK_BEGIN + SINGLE_MATERIAL_LENGTH;
	private static final int MATERIAL_END = BLACK_END;
	private static final int ON_TURN_SEPARATOR_POS = MATERIAL_END;
	private static final int ON_TURN_POS = ON_TURN_SEPARATOR_POS + 1;
	private static final int LENGTH = ON_TURN_POS + 1 + SUFFIX.length();
	

	public static String getFileName(final MaterialHash materialHash) {
		final StringBuffer definitionBuffer = new StringBuffer();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
				final int count = materialHash.getPieceCount(color, pieceType);
				
				definitionBuffer.append(Integer.toString(count));
			}
			
			definitionBuffer.append(MATERIAL_SEPARATOR);
		}
		
		final String fileName = definitionBuffer.toString() + Color.getNotation(materialHash.getOnTurn()) + SUFFIX;
		
		return fileName;
	}
		
	public static MaterialHash parseFileName(final String fileName) {
		if (!isCorrectFileName(fileName))
			throw new RuntimeException("Wrong name of file");

		final char colorChar = fileName.charAt(ON_TURN_POS);
		final int onTurn = Color.parseNotation(colorChar);

		final MaterialHash materialHash = new MaterialHash(fileName.substring(MATERIAL_BEGIN, MATERIAL_END), onTurn);
				
		return materialHash; 
	}
	
	private static final boolean isDigitString(final String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}
		
		return true;
	}

	public static boolean isCorrectFileName(final String fileName) {
		if (fileName.length() != LENGTH)
			return false;
		
		if (!fileName.endsWith(SUFFIX))
			return false;
		
		if (!isDigitString(fileName.substring(WHITE_BEGIN, WHITE_END)))
			return false;

		if (!isDigitString(fileName.substring(BLACK_BEGIN, BLACK_END)))
			return false;
		
		if (!Color.isNotation (fileName.charAt(ON_TURN_POS)))
			return false;
		
		if (fileName.charAt(MATERIAL_SEPARATOR_POS) != MATERIAL_SEPARATOR)
			return false;

		if (fileName.charAt(ON_TURN_SEPARATOR_POS) != MATERIAL_SEPARATOR)
			return false;

		return true;
	}
	
	public static String getAbsolutePath(final String directory, final MaterialHash materialHash) {
		final String fileName = FileNameCalculator.getFileName(materialHash);
		final java.io.File file = new java.io.File (directory, fileName);
		
		return file.getAbsolutePath();
	}

}
