package bishop.base;

import java.io.PrintWriter;

public class CastlingType {
	
	public static final int FIRST = 0;
	
	public static final int SHORT = 0;
	public static final int LONG = 1;
	
	public static final int LAST = 2;
	public static final int BIT_COUNT = 1;
	
	
	final static char[] NOTATION = {'k', 'q'};


	public static void write(final PrintWriter printWritter, final int castlingType) {
		printWritter.append(NOTATION[castlingType]);
	}
	
}
