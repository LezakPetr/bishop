package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import utils.IoUtils;

public class File {
	
	public static final int FIRST = 0;
	
	public static final int FA = 0;
	public static final int FB = 1;
	public static final int FC = 2;
	public static final int FD = 3;
	public static final int FE = 4;
	public static final int FF = 5;
	public static final int FG = 6;
	public static final int FH = 7;
	
	public static final int LAST = 8;
	public static final int NONE = 15;
	
	public static final int BIT_COUNT = 3;
	
	
	private static final char[] NOTATION = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
	
	
	/**
	 * Checks if given file is valid.
	 * @param file file
	 * @return true if file is valid, false if not
	 */
	public static boolean isValid (final int file) {
		return file >= FIRST && file < LAST;
	}

	/**
	 * Writes given file into given writer.
	 * @param writer PrintWriter
	 * @param file coordinate of file
	 */
	public static void write (final PrintWriter writer, final int file) {
		final char c = toChar(file);
		
		writer.print(c);
	}

	public static int read(final Reader reader) throws IOException {
		final char c = IoUtils.readChar(reader);
		
		return fromChar(c);
	}

	public static int fromChar(final char ch) {
		for (int file = File.FIRST; file < File.LAST; file++) {
			if (ch == NOTATION[file])
				return file;
		}
		
		throw new RuntimeException("Character does not correspond to any file");
	}

	public static char toChar(final int file) {
		return NOTATION[file];
	}
}
