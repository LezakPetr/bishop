package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.function.IntToLongFunction;
import java.util.stream.IntStream;

import utils.IoUtils;

/**
 * Color of piece or player.
 * @author Ing. Petr Ležák
 */
public class Color {

	public static final int FIRST = 0;
	
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	
	public static final int LAST = 2;
	
	public static final int NONE = 15;
	
	// Number of bits to store color
	public static final int BIT_COUNT = 1;
	
	private static final char[] NOTATION = {'w', 'b'};
	
	public static final String NAME_WHITE = "white";	
	public static final String NAME_BLACK = "black";

	private static final String[] NAMES = {NAME_WHITE, NAME_BLACK};
	
	
	/**
	 * Checks if given color is valid.
	 * @param color color
	 * @return true if color is valid, false if not
	 */
	public static boolean isValid (final int color) {
		return color >= FIRST && color < LAST;
	}
	
	/**
	 * Returns opposite color to given one.
	 * @param color color
	 * @return opposite color
	 */
	public static int getOppositeColor (final int color) {
		return color ^ 0x01;
	}
	
	public static char getNotation (final int color) {
		return NOTATION[color];
	}
	
	/**
	 * Writes given color into given writer.
	 * @param writer PrintWriter
	 * @param color color to write
	 */
	public static void write (final PrintWriter writer, final int color) {
		writer.print(getNotation (color));
	}
	
	public static int read (final Reader reader) throws IOException {
		final char c = IoUtils.readChar(reader);
		
		return parseNotation(c);
	}

	public static int parseNotation(final char c) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			if (c == getNotation (color))
				return color;
		}
		
		throw new RuntimeException("Character does not correspond to color");
	}

	public static String getName(final int color) {
		return NAMES[color];
	}

	public static boolean isNotation(final char c) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			if (c == getNotation (color))
				return true;
		}

		return false;
	}

	public static IntStream stream() {
		return IntStream.range(FIRST, LAST);
	}

	public static long[] mapToBitBoardArray(final IntToLongFunction mapper) {
		final long[] table = new long[Color.LAST];

		for (int color = Color.FIRST; color < Color.LAST; color++)
			table[color] = mapper.applyAsLong(color);

		return table;
	}

	/**
	 * Returns value if color == Color.WHITE.
	 * Returns -value if color == Color.BLACK.
	 * Result is undefined if color has other value.
	 */
	public static int colorNegate (final int color, final int value) {
		assert WHITE == 0;
		assert BLACK == 1;

		return (value ^ -color) + color;
	}
}
