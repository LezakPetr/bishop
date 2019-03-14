package bishop.tables;

import java.util.Arrays;

import bishop.base.Square;
import bishop.base.Symmetry;

public final class SymmetryTable {
	
	private static final int[] FULL_SYMMETRY_LIST = { Symmetry.IDENTITY, Symmetry.ROTATE, Symmetry.FLIP_FILE, Symmetry.ROTATE_FLIP_FILE, Symmetry.FLIP_RANK, Symmetry.ROTATE_FLIP_RANK, Symmetry.FLIP_FILE_RANK, Symmetry.ROTATE_FLIP_FILE_RANK };
	private static final int[] PAWN_SYMMETRY_LIST = { Symmetry.IDENTITY, Symmetry.FLIP_FILE };
	
	public static final SymmetryTable FULL_TABLE = new SymmetryTable(FULL_SYMMETRY_LIST);
	public static final SymmetryTable PAWN_TABLE = new SymmetryTable(PAWN_SYMMETRY_LIST);
	
	
	private final int[][] symmetryArray;
	
	private SymmetryTable (final int[] symmetryList) {
		symmetryArray = new int[Square.LAST][Square.LAST];
		fillSymmetry(symmetryList);
	}
	
	private void fillSymmetry(final int[] symmetryList) {
		for (int whiteKingSquare = Square.FIRST; whiteKingSquare < Square.LAST; whiteKingSquare++) {
			Arrays.fill(symmetryArray[whiteKingSquare], -1);
		}
		
		for (int whiteKingSquare = Square.FIRST; whiteKingSquare < Square.LAST; whiteKingSquare++) {
			for (int blackKingSquare = Square.FIRST; blackKingSquare < Square.LAST; blackKingSquare++) {
				if (symmetryArray[whiteKingSquare][blackKingSquare] < 0) {
					for (int symmetry: symmetryList) {
						final int transformedWhiteKingSquare = SquareSymmetryTable.getItem(symmetry, whiteKingSquare);
						final int transformedBlackKingSquare = SquareSymmetryTable.getItem(symmetry, blackKingSquare);
						
						if (symmetryArray[transformedWhiteKingSquare][transformedBlackKingSquare] < 0) {
							final int inverseSymmetry = Symmetry.getInverseSymmetry (symmetry);
							
							symmetryArray[transformedWhiteKingSquare][transformedBlackKingSquare] = inverseSymmetry;
						}
					}
				}
			}
		}
	}

	public int getSymmetry(final int whiteKingSquare, final int blackKingSquare) {
		return symmetryArray[whiteKingSquare][blackKingSquare];
	}

	public static int[] getFullSymmetryList() {
		return Arrays.copyOf(FULL_SYMMETRY_LIST, FULL_SYMMETRY_LIST.length);
	}
	
	public static int[] getPawnSymmetryList() {
		return Arrays.copyOf(PAWN_SYMMETRY_LIST, PAWN_SYMMETRY_LIST.length);
	}

}
