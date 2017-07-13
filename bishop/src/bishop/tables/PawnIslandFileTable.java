package bishop.tables;

import java.util.Arrays;

import bishop.base.BoardConstants;
import bishop.base.File;

public class PawnIslandFileTable {
	
	private static final long[][] TABLE = createTable();
	
	public static long[] getIslandsFiles (final int files) {
		return TABLE[files];
	}
	
	private static long[][] createTable() {
		final int size = 1 << File.LAST;
		final long[][] table = new long[size][];
		
		for (int fileCombination = 0; fileCombination < size; fileCombination++) {
			long[] item = new long[0];
			boolean inIsland = false;
			
			for (int file = File.FIRST; file < File.LAST; file++) {
				final boolean isFile = ((fileCombination >>> file) & 0x01) != 0;
				
				if (isFile) {
					if (!inIsland) {
						item = Arrays.copyOf(item, item.length + 1);
						inIsland = true;
					}
					
					item[item.length - 1] |= BoardConstants.getFileMask(file);
				}
				else
					inIsland = false;
			}
			
			table[fileCombination] = item;
		}
		
		return table;
	}
}
