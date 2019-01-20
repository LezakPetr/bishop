package bishopTests;

import bishop.base.File;
import bishop.base.Rank;
import bishop.base.Square;
import org.junit.Assert;
import org.junit.Test;

public class SquareTest {
	@Test
	public void testOnFileRank() {
		Assert.assertEquals(Square.A1, Square.onFileRank(File.FA, Rank.R1));
		Assert.assertEquals(Square.B1, Square.onFileRank(File.FB, Rank.R1));
		Assert.assertEquals(Square.C1, Square.onFileRank(File.FC, Rank.R1));
		Assert.assertEquals(Square.D1, Square.onFileRank(File.FD, Rank.R1));
		Assert.assertEquals(Square.E1, Square.onFileRank(File.FE, Rank.R1));
		Assert.assertEquals(Square.F1, Square.onFileRank(File.FF, Rank.R1));
		Assert.assertEquals(Square.G1, Square.onFileRank(File.FG, Rank.R1));
		Assert.assertEquals(Square.H1, Square.onFileRank(File.FH, Rank.R1));

		Assert.assertEquals(Square.A2, Square.onFileRank(File.FA, Rank.R2));
		Assert.assertEquals(Square.B2, Square.onFileRank(File.FB, Rank.R2));
		Assert.assertEquals(Square.C2, Square.onFileRank(File.FC, Rank.R2));
		Assert.assertEquals(Square.D2, Square.onFileRank(File.FD, Rank.R2));
		Assert.assertEquals(Square.E2, Square.onFileRank(File.FE, Rank.R2));
		Assert.assertEquals(Square.F2, Square.onFileRank(File.FF, Rank.R2));
		Assert.assertEquals(Square.G2, Square.onFileRank(File.FG, Rank.R2));
		Assert.assertEquals(Square.H2, Square.onFileRank(File.FH, Rank.R2));

		Assert.assertEquals(Square.A3, Square.onFileRank(File.FA, Rank.R3));
		Assert.assertEquals(Square.B3, Square.onFileRank(File.FB, Rank.R3));
		Assert.assertEquals(Square.C3, Square.onFileRank(File.FC, Rank.R3));
		Assert.assertEquals(Square.D3, Square.onFileRank(File.FD, Rank.R3));
		Assert.assertEquals(Square.E3, Square.onFileRank(File.FE, Rank.R3));
		Assert.assertEquals(Square.F3, Square.onFileRank(File.FF, Rank.R3));
		Assert.assertEquals(Square.G3, Square.onFileRank(File.FG, Rank.R3));
		Assert.assertEquals(Square.H3, Square.onFileRank(File.FH, Rank.R3));

		Assert.assertEquals(Square.A4, Square.onFileRank(File.FA, Rank.R4));
		Assert.assertEquals(Square.B4, Square.onFileRank(File.FB, Rank.R4));
		Assert.assertEquals(Square.C4, Square.onFileRank(File.FC, Rank.R4));
		Assert.assertEquals(Square.D4, Square.onFileRank(File.FD, Rank.R4));
		Assert.assertEquals(Square.E4, Square.onFileRank(File.FE, Rank.R4));
		Assert.assertEquals(Square.F4, Square.onFileRank(File.FF, Rank.R4));
		Assert.assertEquals(Square.G4, Square.onFileRank(File.FG, Rank.R4));
		Assert.assertEquals(Square.H4, Square.onFileRank(File.FH, Rank.R4));

		Assert.assertEquals(Square.A5, Square.onFileRank(File.FA, Rank.R5));
		Assert.assertEquals(Square.B5, Square.onFileRank(File.FB, Rank.R5));
		Assert.assertEquals(Square.C5, Square.onFileRank(File.FC, Rank.R5));
		Assert.assertEquals(Square.D5, Square.onFileRank(File.FD, Rank.R5));
		Assert.assertEquals(Square.E5, Square.onFileRank(File.FE, Rank.R5));
		Assert.assertEquals(Square.F5, Square.onFileRank(File.FF, Rank.R5));
		Assert.assertEquals(Square.G5, Square.onFileRank(File.FG, Rank.R5));
		Assert.assertEquals(Square.H5, Square.onFileRank(File.FH, Rank.R5));

		Assert.assertEquals(Square.A6, Square.onFileRank(File.FA, Rank.R6));
		Assert.assertEquals(Square.B6, Square.onFileRank(File.FB, Rank.R6));
		Assert.assertEquals(Square.C6, Square.onFileRank(File.FC, Rank.R6));
		Assert.assertEquals(Square.D6, Square.onFileRank(File.FD, Rank.R6));
		Assert.assertEquals(Square.E6, Square.onFileRank(File.FE, Rank.R6));
		Assert.assertEquals(Square.F6, Square.onFileRank(File.FF, Rank.R6));
		Assert.assertEquals(Square.G6, Square.onFileRank(File.FG, Rank.R6));
		Assert.assertEquals(Square.H6, Square.onFileRank(File.FH, Rank.R6));

		Assert.assertEquals(Square.A7, Square.onFileRank(File.FA, Rank.R7));
		Assert.assertEquals(Square.B7, Square.onFileRank(File.FB, Rank.R7));
		Assert.assertEquals(Square.C7, Square.onFileRank(File.FC, Rank.R7));
		Assert.assertEquals(Square.D7, Square.onFileRank(File.FD, Rank.R7));
		Assert.assertEquals(Square.E7, Square.onFileRank(File.FE, Rank.R7));
		Assert.assertEquals(Square.F7, Square.onFileRank(File.FF, Rank.R7));
		Assert.assertEquals(Square.G7, Square.onFileRank(File.FG, Rank.R7));
		Assert.assertEquals(Square.H7, Square.onFileRank(File.FH, Rank.R7));

		Assert.assertEquals(Square.A8, Square.onFileRank(File.FA, Rank.R8));
		Assert.assertEquals(Square.B8, Square.onFileRank(File.FB, Rank.R8));
		Assert.assertEquals(Square.C8, Square.onFileRank(File.FC, Rank.R8));
		Assert.assertEquals(Square.D8, Square.onFileRank(File.FD, Rank.R8));
		Assert.assertEquals(Square.E8, Square.onFileRank(File.FE, Rank.R8));
		Assert.assertEquals(Square.F8, Square.onFileRank(File.FF, Rank.R8));
		Assert.assertEquals(Square.G8, Square.onFileRank(File.FG, Rank.R8));
		Assert.assertEquals(Square.H8, Square.onFileRank(File.FH, Rank.R8));
	}

	@Test
	public void testGetFileGetRank() {
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int file = Square.getFile(square);
			final int rank = Square.getRank(square);

			Assert.assertEquals(square, Square.onFileRank(file, rank));
		}
	}

	@Test
	public void testGetOppositeSquare() {
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int oppositeSquare = Square.getOppositeSquare(square);

			Assert.assertEquals(Square.getFile(square), Square.getFile(oppositeSquare));
			Assert.assertEquals(Rank.getOppositeRank(Square.getRank(square)), Square.getRank(oppositeSquare));
		}
	}
}
