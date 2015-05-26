package bishop.tablebase;

import java.util.Arrays;

import bishop.base.Color;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.tables.CombinationNumberTable;

public class Utils {

	/**
	 * Writes pieces into position. First is written white king, then black king and then pieces in same order as combination definitions.  
	 * @param position target position
	 * @param iterator iterator
	 * @param tableDefinition definition of the table
	 */
	public static void setSquareArrayToPosition(final Position position, final MultiItemIterator iterator, final TableDefinition tableDefinition) {
		int index = 0;
		
		position.setSquareContent(iterator.getItemAt(index), Piece.withColorAndType(Color.WHITE, PieceType.KING));
		index++;
		
		position.setSquareContent(iterator.getItemAt(index), Piece.withColorAndType(Color.BLACK, PieceType.KING));
		index++;
		
		for (int i = 0; i < tableDefinition.getCombinationDefinitionCount(); i++) {
			final CombinationDefinition combinationDefinition = tableDefinition.getCombinationDefinitionAt(i);
			final Piece piece = combinationDefinition.getPiece();
			
			for (int j = 0; j < combinationDefinition.getCount(); j++) {
				position.setSquareContent(iterator.getItemAt(index), piece);
				index++;
			}
		}
	}
	
	public static int findCombinationNumber (final int maxN, final int k, final int value) {
		int low = k - 1;
		int high = maxN;
		
		while (high - low > 1) {
			final int middle = (high + low) / 2;
			
			if (value >= CombinationNumberTable.getItem(middle, k))
				low = middle;
			else
				high = middle;
		}
		
		return low;
	}

	public static int[] createFilledArray(final int item, final int count) {
		final int[] array = new int[count];
		Arrays.fill(array, item);
		
		return array;
	}
	
	public static int boolToInt (final boolean b) {
		return (b) ? 1 : 0;
	}
	
	public static boolean intToBool (final int i) {
		return i != 0;
	}

}
