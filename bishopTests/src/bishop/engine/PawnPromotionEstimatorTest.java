package bishop.engine;

import bishop.base.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;

public class PawnPromotionEstimatorTest {

	private void test(final String positionStr, final Predicate<PawnPromotionEstimator> predicate) {
		final Fen fen = new Fen();

		try {
			fen.readFenFromString(positionStr);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		final Position position = fen.getPosition();
		testPosition(position, predicate);
		testPosition(new MirrorPosition(position), predicate);
	}

	private void testPosition (final IPosition position, final Predicate<PawnPromotionEstimator> predicate) {
		final PawnEndingKey key = new PawnEndingKey(
				position.getPiecesMask(Color.WHITE, PieceType.PAWN) | position.getPiecesMask(Color.WHITE, PieceType.QUEEN),
				position.getPiecesMask(Color.BLACK, PieceType.PAWN) | position.getPiecesMask(Color.BLACK, PieceType.QUEEN)
		);

		final PawnPromotionEstimator estimator = new PawnPromotionEstimator();

		if (position.getOnTurn() == Color.BLACK)
			estimator.init(key, position.getKingPosition(Color.WHITE), position.getKingPosition(Color.BLACK));
		else
			estimator.init(key, position.getKingPosition(Color.BLACK), position.getKingPosition(Color.WHITE));

		Assert.assertTrue(predicate.test(estimator));
	}

	@Test
	public void testDefendantInCheck() {
		test("1Q6/8/1K6/8/8/8/5p1k/8 b - - 0 1", e -> e.defendantInCheck);
		test("1Q6/8/1K6/4P3/8/8/5p1k/8 b - - 0 1", e -> !e.defendantInCheck);
	}

	@Test
	public void testAttackerPawnCount() {
		test("1Q6/8/1K3P2/4P3/8/8/5p1k/8 b - - 0 1", e -> e.attackerPawnCount == 2);
	}

	@Test
	public void testDefendantPawnCount() {
		test("1Q6/8/1K3P2/4P3/8/8/5p1k/8 b - - 0 1", e -> e.defendantPawnCount == 1);
	}

	@Test
	public void testAttackerKingInMateRisk() {
		test("6Q1/8/8/8/K1k5/8/8/8 b - - 0 1", e -> e.attackerKingInMateRisk);
		test("6Q1/8/8/2k5/K7/8/8/8 b - - 0 1", e -> !e.attackerKingInMateRisk);
		test("6Q1/8/8/8/8/k7/8/K7 b - - 0 1", e -> e.attackerKingInMateRisk);
		test("5Q2/8/8/8/8/1k6/8/K7 b - - 0 1", e -> e.attackerKingInMateRisk);
	}

	@Test
	public void testDefendantKingInMateRisk() {
		test("6Q1/8/8/8/k1K5/8/8/8 b - - 0 1", e -> e.defendantKingInMateRisk);
		test("6Q1/8/8/2K5/k7/8/8/8 b - - 0 1", e -> !e.defendantKingInMateRisk);
		test("6Q1/8/8/8/8/K7/8/k7 b - - 0 1", e -> e.defendantKingInMateRisk);
		test("5Q2/8/8/8/8/1K6/8/k7 b - - 0 1", e -> e.defendantKingInMateRisk);
	}

	@Test
	public void testQueenProtected() {
		test("3Q4/2K5/8/5k2/8/8/8/8 b - - 0 1", e -> e.queenProtected);
		test("3Q4/8/2K5/5k2/8/8/8/8 b - - 0 1", e -> !e.queenProtected);
	}

	@Test
	public void testQueenAttacked() {
		test("3Qk3/8/2K5/8/8/8/8/8 b - - 0 1", e -> e.queenAttacked);
		test("3Q4/8/2K5/5k2/8/8/8/8 b - - 0 1", e -> !e.queenAttacked);
	}

	@Test
	public void testExchangeableQueenCount() {
		test("2Q5/1K6/8/8/8/8/2pk4/8 b - - 0 1", e -> e.exchangeableQueenCount == 1);
		test("2Q5/8/8/8/8/1K6/2pk4/8 b - - 0 1", e -> e.exchangeableQueenCount == 1);
		test("2Q5/8/8/8/8/8/1Kpk4/8 b - - 0 1", e -> e.exchangeableQueenCount == 0);
		test("Q7/8/8/8/7K/8/7p/6k1 b - - 0 1", e -> e.exchangeableQueenCount == 1);
		test("Q7/8/8/3P4/6K1/8/7p/6k1 b - - 0 1", e -> e.exchangeableQueenCount == 1);
		test("Q7/8/8/3P4/7K/8/7p/6k1 b - - 0 1", e -> e.exchangeableQueenCount == 0);
		test("Q7/8/8/3P4/P6K/8/7p/6k1 b - - 0 1", e -> e.exchangeableQueenCount == 0);
		test("1Q6/8/8/8/7K/8/1p6/1k6 b - - 0 1", e -> e.exchangeableQueenCount == 0);
		test("5Q2/8/8/8/7K/3k4/1p6/8 b - - 0 1", e -> e.exchangeableQueenCount == 0);
		test("5Q2/8/8/8/7K/8/1pk5/8 b - - 0 1", e -> e.exchangeableQueenCount == 1);
		test("4Q3/8/8/5k2/7K/8/1p6/8 b - - 0 1", e -> e.exchangeableQueenCount == 0);
		test("1Q6/8/8/5k2/7K/8/1p6/8 b - - 0 1", e -> e.exchangeableQueenCount == 0);
		test("4Q3/8/8/8/7K/8/1pk5/8 b - - 0 1", e -> e.exchangeableQueenCount == 1);
	}

	@Test
	public void testCapturableQueenCount() {
		test("K1Q5/8/8/8/8/3k4/2p5/8 b - - 0 1", e -> e.capturableQueenCount == 1);
		test("2Q5/8/8/8/8/8/1Kpk4/8 b - - 0 1", e -> e.capturableQueenCount == 1);
		test("2Q5/8/8/8/8/1K6/2pk4/8 b - - 0 1", e -> e.capturableQueenCount == 0);
		test("3Q4/8/8/8/8/1K2k3/2p5/8 b - - 0 1", e -> e.capturableQueenCount == 1);
		test("6Q1/8/8/8/5k2/1K6/2p5/8 b - - 0 1", e -> e.capturableQueenCount == 0);
		test("5Q2/8/8/8/8/2K1k3/2p5/8 b - - 0 1", e -> e.capturableQueenCount == 0);
	}

	@Test
	public void testNormalPawnTwoMovesToPromotionCount() {
		test("2Q5/8/1K6/8/8/4pk2/8/8 b - - 0 1", e -> e.normalPawnTwoMovesToPromotionCount == 1);
		test("2Q5/8/1K6/8/8/4p3/5k2/8 b - - 0 1", e -> e.normalPawnTwoMovesToPromotionCount == 1);
		test("2Q5/8/1K6/8/8/4p3/4k3/8 b - - 0 1", e -> e.normalPawnTwoMovesToPromotionCount == 0);
	}

	@Test
	public void testStalematingPawnTwoMovesToPromotionCount() {
		test("2Q5/8/1K6/8/8/6kp/8/8 b - - 0 1", e -> e.stalematingPawnTwoMovesToPromotionCount == 1);
		test("2Q5/8/1K6/8/8/5p2/6k1/8 b - - 0 1", e -> e.stalematingPawnTwoMovesToPromotionCount == 1);
		test("2Q5/8/1K6/8/8/p7/k7/8 b - - 0 1", e -> e.stalematingPawnTwoMovesToPromotionCount == 0);
	}

	@Test
	public void testSavedQueenCount() {
		test("2Q5/8/1K6/8/8/8/4pk2/8 b - - 0 1", e -> e.savedQueenCount == 1);
		test("2Q5/8/8/8/8/8/3Kpk2/8 b - - 0 1", e -> e.savedQueenCount == 1);
		test("4Q3/8/8/1K6/8/8/4pk2/8 b - - 0 1", e -> e.savedQueenCount == 0);
		test("7Q/8/8/1K6/8/8/4pk2/8 b - - 0 1", e -> e.savedQueenCount == 0);
	}

	@Test
	public void testAttackerPawnOnSevenRankCount() {
		test("7Q/1PP5/8/1K6/8/8/4pk2/8 b - - 0 1", e -> e.attackerPawnOnSevenRankCount == 2);
		test("7Q/8/1P6/1K1P4/8/8/4pk2/8 b - - 0 1", e -> e.attackerPawnOnSevenRankCount == 0);
	}

}
