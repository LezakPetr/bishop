package bishopTests;

import bishop.base.*;
import bishop.engine.PawnEndingKey;
import org.junit.Assert;
import org.junit.Test;

public class PawnEndingKeyTest {
	private static final long WHITE_PAWN_MASK = BitBoard.fromString("a2, c3, d4");
	private static final long BLACK_PAWN_MASK = BitBoard.fromString("a7, b6, c1");
	private static final long OCCUPANCY = WHITE_PAWN_MASK | BLACK_PAWN_MASK;
	private static final PawnEndingKey KEY = new PawnEndingKey(WHITE_PAWN_MASK, BLACK_PAWN_MASK);

	@Test
	public void testGetWhitePawns() {
		final PawnEndingKey key = new PawnEndingKey(WHITE_PAWN_MASK, BLACK_PAWN_MASK);
		Assert.assertEquals(WHITE_PAWN_MASK, key.getWhitePawns());
	}

	@Test
	public void testGetBlackPawns() {
		Assert.assertEquals(BLACK_PAWN_MASK, KEY.getBlackPawns());
	}

	@Test
	public void testGetPawnOccupancy() {
		Assert.assertEquals(OCCUPANCY, KEY.getPawnOccupancy());
	}

	@Test
	public void testGetPawnMask() {
		Assert.assertEquals(WHITE_PAWN_MASK, KEY.getPawnMask(Color.WHITE));
		Assert.assertEquals(BLACK_PAWN_MASK, KEY.getPawnMask(Color.BLACK));
	}

	@Test
	public void testGetPromotedPawnColor() {
		final PawnEndingKey key1 = new PawnEndingKey(BitBoard.fromString("a2, b2, c2"), BitBoard.fromString("a5, b5, c5"));
		Assert.assertEquals(Color.NONE, key1.getPromotedPawnColor());

		final PawnEndingKey key2 = new PawnEndingKey(BitBoard.fromString("a8, b2, c8"), BitBoard.fromString("a5, b5, c5"));
		Assert.assertEquals(Color.WHITE, key2.getPromotedPawnColor());

		final PawnEndingKey key3 = new PawnEndingKey(BitBoard.fromString("a2, b2, c2"), BitBoard.fromString("a1, b5, c5"));
		Assert.assertEquals(Color.BLACK, key3.getPromotedPawnColor());
	}

	@Test
	public void testRemovePawn() {
		Assert.assertEquals(
				new PawnEndingKey(
						BitBoard.fromString("c3, d4"),
						BLACK_PAWN_MASK
				),
				KEY.removePawn(Square.A2)
		);

		Assert.assertEquals(
				new PawnEndingKey(
						WHITE_PAWN_MASK,
						BitBoard.fromString("a7, b6")
				),
				KEY.removePawn(Square.C1)
		);

		Assert.assertEquals(
				KEY,
				KEY.removePawn(Square.F6)
		);
	}

	@Test
	public void testAddPawn() {
		Assert.assertEquals(
				new PawnEndingKey(
						BitBoard.fromString("a2, c3, d4, f6"),
						BLACK_PAWN_MASK
				),
				KEY.addPawn(Color.WHITE, Square.F6)
		);

		Assert.assertEquals(
				new PawnEndingKey(
						WHITE_PAWN_MASK,
						BitBoard.fromString("a7, b6, c1, f1")
				),
				KEY.addPawn(Color.BLACK, Square.F1)
		);
	}

	@Test
	public void testGetMaterialHash() {
		Assert.assertEquals(
				new MaterialHash("00003-10002", Color.WHITE),
				KEY.getMaterialHash()
		);
	}

	@Test
	public void testEstimateComplexity() {
		Assert.assertEquals(
				6L * 10L * 10L * 10L * 3L * 10L * 10L,
				new PawnEndingKey(
						BitBoard.fromString("a3, c4, d5, e4, g3, h2"),
						BitBoard.fromString("c5, d6, e5, f3, g4, h3")
				).estimateComplexity()
		);

		Assert.assertEquals(
				Long.MAX_VALUE,
				new PawnEndingKey(
						BitBoard.fromString("a3"),
						BitBoard.fromString("b5")
				).estimateComplexity()
		);

		Assert.assertEquals(
				10 * 5,
				new PawnEndingKey(
						BitBoard.fromString("c3, d4"),
						BitBoard.fromString("c4")
				).estimateComplexity()
		);

		Assert.assertEquals(
				Long.MAX_VALUE,
				new PawnEndingKey(
						BitBoard.fromString("c3, d4"),
						BitBoard.fromString("c4, d5, d6")
				).estimateComplexity()
		);
	}
}
