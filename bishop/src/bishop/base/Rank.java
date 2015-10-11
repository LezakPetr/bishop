package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import utils.IoUtils;

public class Rank {

	public static final int FIRST = 0;
	
	public static final int R1 = 0;
	public static final int R2 = 1;
	public static final int R3 = 2;
	public static final int R4 = 3;
	public static final int R5 = 4;
	public static final int R6 = 5;
	public static final int R7 = 6;
	public static final int R8 = 7;
	
	public static final int LAST = 8;
	public static final int COUNT = LAST - FIRST;
	public static final int BIT_COUNT = 3;
	
	private static final char[] NOTATION = {'1', '2', '3', '4', '5', '6', '7', '8'};
	
	
	/**
	 * Checks if given rank is valid.
	 * @param rank rank
	 * @return true if rank is valid, false if not
	 */
	public static boolean isValid (final int rank) {
		return rank >= FIRST && rank < LAST;
	}
	
	public static int getOppositeRank (final int rank) {
		return Rank.R8 - rank;
	}
	
	/**
	 * Writes given rank into given writer.
	 * @param writer PrintWriter
	 * @param rank coordinate of rank
	 */
	public static void write (final PrintWriter writer, final int rank) {
		final char c = toChar(rank);
		
		writer.print(c);
	}
	
	public static int read(final Reader reader) throws IOException {
		final char c = IoUtils.readChar(reader);
		
		return fromChar(c);
	}

	public static int fromChar(final char ch) {
		for (int rank = Rank.FIRST; rank < Rank.LAST; rank++) {
			if (ch == NOTATION[rank])
				return rank;
		}
		
		throw new RuntimeException("Character does not correspond to any rank");
	}

	public static char toChar(final int rank) {
		return NOTATION[rank];
	}
}
