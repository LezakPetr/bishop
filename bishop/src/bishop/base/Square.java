package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

public class Square {

	public static final int FIRST = 0;
	
	public static final int A1 =  0;
	public static final int B1 =  1;
	public static final int C1 =  2;
	public static final int D1 =  3;
	public static final int E1 =  4;
	public static final int F1 =  5;
	public static final int G1 =  6;
	public static final int H1 =  7;
	
	public static final int A2 =  8;
	public static final int B2 =  9;
	public static final int C2 = 10;
	public static final int D2 = 11;
	public static final int E2 = 12;
	public static final int F2 = 13;
	public static final int G2 = 14;
	public static final int H2 = 15;

	public static final int A3 = 16;
	public static final int B3 = 17;
	public static final int C3 = 18;
	public static final int D3 = 19;
	public static final int E3 = 20;
	public static final int F3 = 21;
	public static final int G3 = 22;
	public static final int H3 = 23;

	public static final int A4 = 24;
	public static final int B4 = 25;
	public static final int C4 = 26;
	public static final int D4 = 27;
	public static final int E4 = 28;
	public static final int F4 = 29;
	public static final int G4 = 30;
	public static final int H4 = 31;

	public static final int A5 = 32;
	public static final int B5 = 33;
	public static final int C5 = 34;
	public static final int D5 = 35;
	public static final int E5 = 36;
	public static final int F5 = 37;
	public static final int G5 = 38;
	public static final int H5 = 39;

	public static final int A6 = 40;
	public static final int B6 = 41;
	public static final int C6 = 42;
	public static final int D6 = 43;
	public static final int E6 = 44;
	public static final int F6 = 45;
	public static final int G6 = 46;
	public static final int H6 = 47;
	
	public static final int A7 = 48;
	public static final int B7 = 49;
	public static final int C7 = 50;
	public static final int D7 = 51;
	public static final int E7 = 52;
	public static final int F7 = 53;
	public static final int G7 = 54;
	public static final int H7 = 55;

	public static final int A8 = 56;
	public static final int B8 = 57;
	public static final int C8 = 58;
	public static final int D8 = 59;
	public static final int E8 = 60;
	public static final int F8 = 61;
	public static final int G8 = 62;
	public static final int H8 = 63;

	public static final int LAST = 64;
	
	public static final int NONE = 100;
	public static final int BIT_COUNT = 6;
	
	// Range of squares where pawn can be placed
	public static final int FIRST_PAWN_SQUARE = Square.A2;
	public static final int LAST_PAWN_SQUARE = Square.H7 + 1;

	public static final int COUNT = LAST - FIRST;
	

	/**
	 * Checks if given square is valid.
	 * @param square square
	 * @return true if square is valid, false if not
	 */
	public static boolean isValid (final int square) {
		return square >= FIRST && square < LAST;
	}

	/**
	 * Returns file of given square.
	 * @param square square coordinate
	 * @return file of square
	 */
	public static int getFile (final int square) {
		return square & 0x07;
	}

	/**
	 * Returns rank of given square.
	 * @param square square coordinate
	 * @return rank of square
	 */
	public static int getRank (final int square) {
		return square >> 3;
	}
	
	/**
	 * Returns square on given file and rank.
	 * @param file file coordinate
	 * @param rank rank coordinate
	 * @return square on given file and rank.
	 */
	public static int onFileRank (final int file, final int rank) {
		return file + (rank << 3); 
	}
	
	/**
	 * Returns corresponding square from view of opposite side.
	 * @param square square
	 * @return opposite square
	 */
	public static int getOppositeSquare (final int square) {
		final int rank = Square.getRank(square);
		final int file = Square.getFile(square);
		final int oppositeSquare = Square.onFileRank(file, Rank.getOppositeRank (rank));
		
		return oppositeSquare;
	}
	
	/**
	 * Writes given square into given writer.
	 * @param writer PrintWriter
	 * @param square coordinate of square
	 */
	public static void write (final PrintWriter writer, final int square) {
		final int file = getFile(square);
		final int rank = getRank(square);
		
		File.write(writer, file);
		Rank.write(writer, rank);
	}

	public static int read(final Reader reader) throws IOException {
		final int file = File.read (reader);
		final int rank = Rank.read (reader);
		
		return Square.onFileRank(file, rank);
	}
}
