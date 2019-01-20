package bishopTests;

import bishop.base.*;
import bishop.tables.BetweenTable;
import org.junit.Assert;
import org.junit.Test;

public class CastlingConstantsTest {
	@Test
	public void getCastlingMiddleSquareMaskTest() {
		Assert.assertEquals(BetweenTable.getItem(Square.E1, Square.H1), CastlingConstants.of(Color.WHITE, CastlingType.SHORT).getMiddleSquareMask());
		Assert.assertEquals(BetweenTable.getItem(Square.E1, Square.A1), CastlingConstants.of(Color.WHITE, CastlingType.LONG).getMiddleSquareMask());
		Assert.assertEquals(BetweenTable.getItem(Square.E8, Square.H8), CastlingConstants.of(Color.BLACK, CastlingType.SHORT).getMiddleSquareMask());
		Assert.assertEquals(BetweenTable.getItem(Square.E8, Square.A8), CastlingConstants.of(Color.BLACK, CastlingType.LONG).getMiddleSquareMask());
	}

	@Test
	public void getCastlingRookBeginSquareTest() {
		Assert.assertEquals(Square.H1, CastlingConstants.of(Color.WHITE, CastlingType.SHORT).getRookBeginSquare());
		Assert.assertEquals(Square.A1, CastlingConstants.of(Color.WHITE, CastlingType.LONG).getRookBeginSquare());
		Assert.assertEquals(Square.H8, CastlingConstants.of(Color.BLACK, CastlingType.SHORT).getRookBeginSquare());
		Assert.assertEquals(Square.A8, CastlingConstants.of(Color.BLACK, CastlingType.LONG).getRookBeginSquare());
	}

	@Test
	public void getCastlingRookTargetSquareTest() {
		Assert.assertEquals(Square.F1, CastlingConstants.of(Color.WHITE, CastlingType.SHORT).getRookTargetSquare());
		Assert.assertEquals(Square.D1, CastlingConstants.of(Color.WHITE, CastlingType.LONG).getRookTargetSquare());
		Assert.assertEquals(Square.F8, CastlingConstants.of(Color.BLACK, CastlingType.SHORT).getRookTargetSquare());
		Assert.assertEquals(Square.D8, CastlingConstants.of(Color.BLACK, CastlingType.LONG).getRookTargetSquare());
	}

	@Test
	public void getCastlingKingTargetSquareTest() {
		Assert.assertEquals(Square.G1, CastlingConstants.of(Color.WHITE, CastlingType.SHORT).getKingTargetSquare());
		Assert.assertEquals(Square.C1, CastlingConstants.of(Color.WHITE, CastlingType.LONG).getKingTargetSquare());
		Assert.assertEquals(Square.G8, CastlingConstants.of(Color.BLACK, CastlingType.SHORT).getKingTargetSquare());
		Assert.assertEquals(Square.C8, CastlingConstants.of(Color.BLACK, CastlingType.LONG).getKingTargetSquare());
	}

	@Test
	public void getCastlingKingMiddleSquareTest() {
		Assert.assertEquals(Square.F1, CastlingConstants.of(Color.WHITE, CastlingType.SHORT).getKingMiddleSquare());
		Assert.assertEquals(Square.D1, CastlingConstants.of(Color.WHITE, CastlingType.LONG).getKingMiddleSquare());
		Assert.assertEquals(Square.F8, CastlingConstants.of(Color.BLACK, CastlingType.SHORT).getKingMiddleSquare());
		Assert.assertEquals(Square.D8, CastlingConstants.of(Color.BLACK, CastlingType.LONG).getKingMiddleSquare());
	}

	@Test
	public void getCastlingKingChangeMaskTest() {
		Assert.assertEquals(BitBoard.of(Square.E1, Square.G1), CastlingConstants.of(Color.WHITE, CastlingType.SHORT).getKingChangeMask());
		Assert.assertEquals(BitBoard.of(Square.E1, Square.C1), CastlingConstants.of(Color.WHITE, CastlingType.LONG).getKingChangeMask());
		Assert.assertEquals(BitBoard.of(Square.E8, Square.G8), CastlingConstants.of(Color.BLACK, CastlingType.SHORT).getKingChangeMask());
		Assert.assertEquals(BitBoard.of(Square.E8, Square.C8), CastlingConstants.of(Color.BLACK, CastlingType.LONG).getKingChangeMask());
	}

	@Test
	public void getCastlingRookChangeMaskTest() {
		Assert.assertEquals(BitBoard.of(Square.H1, Square.F1), CastlingConstants.of(Color.WHITE, CastlingType.SHORT).getRookChangeMask());
		Assert.assertEquals(BitBoard.of(Square.A1, Square.D1), CastlingConstants.of(Color.WHITE, CastlingType.LONG).getRookChangeMask());
		Assert.assertEquals(BitBoard.of(Square.H8, Square.F8), CastlingConstants.of(Color.BLACK, CastlingType.SHORT).getRookChangeMask());
		Assert.assertEquals(BitBoard.of(Square.A8, Square.D8), CastlingConstants.of(Color.BLACK, CastlingType.LONG).getRookChangeMask());
	}

}
