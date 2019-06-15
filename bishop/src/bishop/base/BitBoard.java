package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringWriter;
import java.util.Arrays;

import utils.IoUtils;


public class BitBoard {
	
	public static final long EMPTY = 0L;
	public static final long FULL = 0xFFFFFFFFFFFFFFFFL;
	

	/**
	 * Returns bit board with just one set bit on given square.
	 * @param square square
	 * @return mask of square
	 */
	public static long getSquareMask (final int square) {
		return 1L << square;
	}
	
	/**
	 * Creates bit board from array of squares.
	 * @param squareArray array of squares
	 * @return bit board
	 */
	public static long fromSquareArray (final int ...squareArray) {
		long board = 0;
		
		for (int square: squareArray)
			board |= BitBoard.getSquareMask(square);
		
		return board;
	}

	@SuppressWarnings("SameReturnValue")
	public static long of() {
		return BitBoard.EMPTY;
	}

	public static long of(final int square1) {
		return BitBoard.getSquareMask(square1);
	}

	public static long of(final int square1, final int square2) {
		return BitBoard.getSquareMask(square1) | BitBoard.getSquareMask(square2);
	}

	public static long of(final int square1, final int square2, final int square3) {
		return BitBoard.getSquareMask(square1) | BitBoard.getSquareMask(square2) | BitBoard.getSquareMask(square3);
	}

	public static long of (final int ...squares) {
		return fromSquareArray(squares);
	}
	
	public static void write (final PrintWriter writer, final long board) {
		boolean first = true;
		
		for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
		
			if (!first)
				writer.print(", ");
				
			Square.write(writer, square);
			first = false;
		}
	}
	
	public static long read (final PushbackReader reader) throws IOException {
		long board = BitBoard.EMPTY;
		
		while (!IoUtils.isEndOfStream(reader)) {
			IoUtils.skipWhiteSpace(reader);
			
			final int square = Square.read(reader);
			board |= BitBoard.getSquareMask(square);
			
			IoUtils.skipWhiteSpace(reader);
			
			if (IoUtils.isEndOfStream(reader))
				break;
			
			final char ch = IoUtils.readChar(reader);
			
			if (ch != ',') {
				reader.unread(ch);
				break;
			}
		}
		
		return board;
	}
	
	public static long fromString (final String str) {
		try (PushbackReader reader = IoUtils.getPushbackReader(str)) {
			return read(reader);
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot read BotBoard", ex);
		}
	}
	
	public static String toString(final long board) {
		try (
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
		)
		{
			write(printWriter, board);
			printWriter.flush();
			
			return stringWriter.toString();
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot write board", ex);
		}
	}

	public static int getSquareCount (final long board) {
		/*//final long counterWidth2 = (board         & 0x5555555555555555L) + ((board         & 0xAAAAAAAAAAAAAAAAL) >>> 1);

		// 00 = 0 => 0
		// 01 = 1 => 1
		// 10 = 2 => 1
		// 11 = 3 => 2
		// counterWidth2 = board - upper bit shifted down
		final long counterWidth2 = board - ((board & 0xAAAAAAAAAAAAAAAAL) >>> 1);
		final long counterWidth4 = (counterWidth2 & 0x3333333333333333L) + ((counterWidth2 & 0xCCCCCCCCCCCCCCCCL) >>> 2);
		final long counterWidth8 = (counterWidth4 & 0x0F0F0F0F0F0F0F0FL) + ((counterWidth4 & 0xF0F0F0F0F0F0F0F0L) >>> 4);
		final int count = (int) ((counterWidth8 * 0x0101010101010101L) >>> 56);

		return count;*/
		return Long.bitCount(board);
	}

	public static int getSquareCountSparse (final long board) {
		long mask = board;
		int count = 0;

		while (mask != 0) {
			mask &= mask - 1;
			count++;
		}

		return count;
	}

	public static boolean hasSingleSquare (final long board) {
		return board != 0 && (board & (board - 1)) == 0;
	}

	public static boolean hasAtLeastTwoSquares (final long board) {
		return (board & (board - 1)) != 0;
	}

	private static final long FIRST_SQUARE_TABLE_COEFF = 544578837055249459L;
	private static final int FIRST_SQUARE_TABLE_BITS = Square.BIT_COUNT + 1;
	private static final int FIRST_SQUARE_TABLE_SIZE = 1 << FIRST_SQUARE_TABLE_BITS;
	private static final int FIRST_SQUARE_TABLE_SHIFT = Long.SIZE - FIRST_SQUARE_TABLE_BITS;
	private static final byte[] FIRST_SQUARE_TABLE = initializeFirstSquareTable();

	private static byte[] initializeFirstSquareTable() {
		final byte[] table = new byte[FIRST_SQUARE_TABLE_SIZE];
		Arrays.fill(table, (byte) -1);

		table[getFirstSquareTableIndex(BitBoard.EMPTY)] = Square.NONE;

		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int index = getFirstSquareTableIndex(BitBoard.of(square));

			if (table[index] != -1)
				throw new RuntimeException("Collision in table");

			table[index] = (byte) square;
		}

		return table;
	}

	private static int getFirstSquareTableIndex (final long board) {
		final long firstSetBit = board & -board;

		return (int) ((firstSetBit * FIRST_SQUARE_TABLE_COEFF) >>> FIRST_SQUARE_TABLE_SHIFT);
	}

	/**
	 * Returns first set square on the board.
	 * @param board board
	 * @return first set square or Square.NONE
	 */
	public static int getFirstSquare(final long board) {
		final int index = getFirstSquareTableIndex(board);

		return FIRST_SQUARE_TABLE[index];
	}

	/**
	 * Returns last set square on the board.
	 * @param board board
	 * @return last set square or Square.NONE
	 */
	public static int getLastSquare(final long board) {
		if (board != 0)
			return 63 - Long.numberOfLeadingZeros(board);
		else
			return Square.NONE;
	}

	/**
	 * Returns board with mirrored ranks.
	 * @param board board to mirror
	 * @return mirrored board
	 */
	public static long getMirrorBoard(final long board) {
		return Long.reverseBytes(board);
/*		long result = board;
		
		result = ((result & 0xFF00FF00FF00FF00L) >>> 8) | ((result & 0x00FF00FF00FF00FFL) << 8);
		result = ((result & 0xFFFF0000FFFF0000L) >>> 16) | ((result & 0x0000FFFF0000FFFFL) << 16);
		result = ((result & 0xFFFFFFFF00000000L) >>> 32) | ((result & 0x00000000FFFFFFFFL) << 32);
		
		return result;*/
	}

	/**
	 * Returns n-th lowest set square.
	 * @param possibleSquares mask with squares
	 * @param index index of the square
	 * @return square
	 */
	public static int getNthSquare(final long possibleSquares, final int index) {
		long mask = possibleSquares;

		for (int i = 0; i < index; i++)
			mask &= mask - 1;
		
		return getFirstSquare(mask);
	}

	/**
	 * Returns index of given square. In is the index of the square if set squares would be sorted.
	 * @param possibleSquares mask of square
	 * @param square square, it must be set in possibleSquares
	 * @return index of given square
	 */
	public static int getSquareIndex(final long possibleSquares, final int square) {
		final long squareMask = getSquareMask(square);
		final long preSquareMask = squareMask - 1;   // Contains 1 on squares lower than given square
		
		return getSquareCount(possibleSquares & preSquareMask);
	}

	public static long extendForward (final long mask) {
		long result = mask;
		
		result |= result << File.LAST;
		result |= result << (2 * File.LAST);
		result |= result << (4 * File.LAST);
		
		return result;
	}

	public static long extendBackward (final long mask) {
		long result = mask;
		
		result |= result >>> File.LAST;
		result |= result >>> (2 * File.LAST);
		result |= result >>> (4 * File.LAST);
		
		return result;
	}

	public static long extendForwardByColor (final int color, final long mask) {
		if (color == Color.WHITE)
			return extendForward(mask);
		else
			return extendBackward(mask);
	}

	public static long extendForwardByColorWithoutItself (final int color, final long mask) {
		if (color == Color.WHITE)
			return BitBoard.extendForward(mask) << File.LAST;
		else
			return BitBoard.extendBackward(mask) >>> File.LAST;
	}

	public static boolean containsSquare(final long mask, final int square) {
		return ((mask >>> square) & 0x01) != 0;
	}

}
