package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringWriter;

import utils.IoUtils;


public class BitBoard {
	
	public static final long EMPTY = 0l;
	public static final long FULL = 0xFFFFFFFFFFFFFFFFl;
	

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
		return Long.bitCount(board);
	}
	
	public static int getFirstSquare(final long board) {
		if (board != 0)
			return Long.numberOfTrailingZeros(board);
		else
			return Square.NONE;
	}

	public static int getLastSquare(final long board) {
		if (board != 0)
			return 63 - Long.numberOfLeadingZeros(board);
		else
			return Square.NONE;
	}

	public static long getMirrorBoard(final long board) {
		return Long.reverseBytes(board);
/*		long result = board;
		
		result = ((result & 0xFF00FF00FF00FF00L) >>> 8) | ((result & 0x00FF00FF00FF00FFL) << 8);
		result = ((result & 0xFFFF0000FFFF0000L) >>> 16) | ((result & 0x0000FFFF0000FFFFL) << 16);
		result = ((result & 0xFFFFFFFF00000000L) >>> 32) | ((result & 0x00000000FFFFFFFFL) << 32);
		
		return result;*/
	}
	
	public static int getNthSquare(final long possibleSquares, final int index) {
		long mask = possibleSquares;

		for (int i = 0; i < index; i++)
			mask &= Long.lowestOneBit(mask);
		
		return getFirstSquare(mask);
	}

	public static int getSquareIndex(final long possibleSquares, final int square) {
		final long squareMask = getSquareMask(square);
		final long preSquareMask = squareMask - 1;   // Contains 1 on squares lower than given square
		
		return getSquareCount(possibleSquares & preSquareMask);
	}

}
