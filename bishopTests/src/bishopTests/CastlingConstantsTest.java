package bishopTests;

import bishop.base.*;
import bishop.tables.BetweenTable;
import org.junit.Assert;
import org.junit.Test;

public class CastlingConstantsTest {
	@Test
	public void getCastlingMiddleSquareMaskTest() {
		Assert.assertEquals(BetweenTable.getItem(Square.E1, Square.H1), CastlingConstants.getCastlingMiddleSquareMask(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(BetweenTable.getItem(Square.E1, Square.A1), CastlingConstants.getCastlingMiddleSquareMask(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(BetweenTable.getItem(Square.E8, Square.H8), CastlingConstants.getCastlingMiddleSquareMask(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(BetweenTable.getItem(Square.E8, Square.A8), CastlingConstants.getCastlingMiddleSquareMask(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingRookBeginSquareTest() {
		Assert.assertEquals(Square.H1, CastlingConstants.getCastlingRookBeginSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.A1, CastlingConstants.getCastlingRookBeginSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.H8, CastlingConstants.getCastlingRookBeginSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.A8, CastlingConstants.getCastlingRookBeginSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingRookTargetSquareTest() {
		Assert.assertEquals(Square.F1, CastlingConstants.getCastlingRookTargetSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.D1, CastlingConstants.getCastlingRookTargetSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.F8, CastlingConstants.getCastlingRookTargetSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.D8, CastlingConstants.getCastlingRookTargetSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingTargetSquareTest() {
		Assert.assertEquals(Square.G1, CastlingConstants.getCastlingKingTargetSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.C1, CastlingConstants.getCastlingKingTargetSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.G8, CastlingConstants.getCastlingKingTargetSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.C8, CastlingConstants.getCastlingKingTargetSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingMiddleSquareTest() {
		Assert.assertEquals(Square.F1, CastlingConstants.getCastlingKingMiddleSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.D1, CastlingConstants.getCastlingKingMiddleSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.F8, CastlingConstants.getCastlingKingMiddleSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.D8, CastlingConstants.getCastlingKingMiddleSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingChangeMaskTest() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
				Assert.assertEquals(
						BitBoard.of(
								CastlingConstants.getCastlingKingBeginSquare(color),
								CastlingConstants.getCastlingKingTargetSquare(color, castlingType)
						),
						CastlingConstants.getCastlingKingChangeMask(color, castlingType)
				);
			}
		}
	}

	@Test
	public void getCastlingRookChangeMaskTest() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
				Assert.assertEquals(
						BitBoard.of(
								CastlingConstants.getCastlingRookBeginSquare(color, castlingType),
								CastlingConstants.getCastlingRookTargetSquare(color, castlingType)
						),
						CastlingConstants.getCastlingRookChangeMask(color, castlingType)
				);
			}
		}
	}

}
