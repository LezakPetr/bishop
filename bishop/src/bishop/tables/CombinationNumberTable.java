package bishop.tables;

import bishop.base.Square;

/**
 * This table contains combination numbers (e.g. binomial coefficients) C(n, k).
 * @author Ing. Petr Ležák
 */
public class CombinationNumberTable {
	
	public static final int MAX_N = Square.LAST - 1;
	public static final int MAX_K = 4;
	
	private static final int N_SHIFT = 0;
	private static final int K_SHIFT = N_SHIFT + Square.BIT_COUNT;
	private static final int BIT_COUNT = K_SHIFT + 3;
	
	private static final int[] table = createTable();
	

	private static int getItemIndex (final int n, final int k) {
		return n + (k << K_SHIFT);
	}
	
	public static int getItem (final int n, final int k) {
		final int index = getItemIndex(n, k);
		
		return table[index];
	}
	
	private static int[] createTable() {
		final int size = 1 << BIT_COUNT;
		final int[] table = new int[size];
		
    	for (int n = 0; n <= MAX_N; n++) {
    		table[getItemIndex (n, 0)] = 1;
    	}
    	
    	for (int k = 1; k <= MAX_K; k++) {
    		table[getItemIndex (k, k)] = 1;
    		
    		for (int n = k+1; n <= MAX_N; n++) {
    			final int s1 = table[getItemIndex (n-1, k-1)];
    			final int s2 = table[getItemIndex (n-1, k)];
    			
    			table[getItemIndex (n, k)] = s1 + s2;
    		}
    	}
    	
    	if (table[getItemIndex (52, 3)] != 22100)
    		throw new RuntimeException("Wrong combination table");
    	
    	return table;
	}

}
