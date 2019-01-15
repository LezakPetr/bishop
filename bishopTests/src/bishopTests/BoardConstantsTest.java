package bishopTests;

import bishop.base.*;
import bishop.tables.BetweenTable;
import org.junit.Assert;
import org.junit.Test;

public class BoardConstantsTest {
	@Test
	public void getSquareColorMaskTest() {
		Assert.assertEquals(BoardConstants.WHITE_SQUARE_MASK, BoardConstants.getSquareColorMask(Color.WHITE));
		Assert.assertEquals(BoardConstants.BLACK_SQUARE_MASK, BoardConstants.getSquareColorMask(Color.BLACK));
	}

	@Test
	public void getCastlingMiddleSquareMaskTest() {
		Assert.assertEquals(BetweenTable.getItem(Square.E1, Square.H1), BoardConstants.getCastlingMiddleSquareMask(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(BetweenTable.getItem(Square.E1, Square.A1), BoardConstants.getCastlingMiddleSquareMask(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(BetweenTable.getItem(Square.E8, Square.H8), BoardConstants.getCastlingMiddleSquareMask(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(BetweenTable.getItem(Square.E8, Square.A8), BoardConstants.getCastlingMiddleSquareMask(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingRookBeginSquareTest() {
		Assert.assertEquals(Square.H1, BoardConstants.getCastlingRookBeginSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.A1, BoardConstants.getCastlingRookBeginSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.H8, BoardConstants.getCastlingRookBeginSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.A8, BoardConstants.getCastlingRookBeginSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingRookTargetSquareTest() {
		Assert.assertEquals(Square.F1, BoardConstants.getCastlingRookTargetSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.D1, BoardConstants.getCastlingRookTargetSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.F8, BoardConstants.getCastlingRookTargetSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.D8, BoardConstants.getCastlingRookTargetSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingTargetSquareTest() {
		Assert.assertEquals(Square.G1, BoardConstants.getCastlingKingTargetSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.C1, BoardConstants.getCastlingKingTargetSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.G8, BoardConstants.getCastlingKingTargetSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.C8, BoardConstants.getCastlingKingTargetSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingMiddleSquareTest() {
		Assert.assertEquals(Square.F1, BoardConstants.getCastlingKingMiddleSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.D1, BoardConstants.getCastlingKingMiddleSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.F8, BoardConstants.getCastlingKingMiddleSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.D8, BoardConstants.getCastlingKingMiddleSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingChangeMaskTest() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
				Assert.assertEquals(
						BitBoard.of(
								BoardConstants.getCastlingKingBeginSquare(color),
								BoardConstants.getCastlingKingTargetSquare(color, castlingType)
						),
						BoardConstants.getCastlingKingChangeMask(color, castlingType)
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
								BoardConstants.getCastlingRookBeginSquare(color, castlingType),
								BoardConstants.getCastlingRookTargetSquare(color, castlingType)
						),
						BoardConstants.getCastlingRookChangeMask(color, castlingType)
				);
			}
		}
	}

}
