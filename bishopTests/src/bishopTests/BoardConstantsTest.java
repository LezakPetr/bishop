package bishopTests;

import bishop.base.BoardConstants;
import bishop.base.CastlingType;
import bishop.base.Color;
import bishop.base.Square;
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
}
