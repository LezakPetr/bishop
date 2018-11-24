package bishopTests;

import bishop.base.*;
import bishop.engine.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;
import java.util.function.Supplier;

public class GameStageTablePositionEvaluatorTest {
	private static final int WHITE_KING_SQUARE = Square.E1;
	private static final int BLACK_KING_SQUARE = Square.H6;

	private static final double EPS = 1e-9;

	@Test
	public void testCorrectness() {
		final PositionEvaluationCoeffs coeffs = new PositionEvaluationCoeffs();

		for (int i = 0; i < PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.size(); i++) {
			final TablePositionCoeffs tablePositionCoeffs = PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.get(i);

			for (int square = Square.FIRST; square < Square.LAST; square++)
				coeffs.setEvaluationCoeff(tablePositionCoeffs.getCoeff(Color.WHITE, PieceType.KING, square), Objects.hash(i, square) & 0xFFF);
		}

		final Position position = new Position();
		position.setCombinedPositionEvaluationTable(new CombinedPositionEvaluationTable(coeffs));
		position.clearPosition();
		position.setSquareContent(WHITE_KING_SQUARE, Piece.WHITE_KING);
		position.setSquareContent(BLACK_KING_SQUARE, Piece.BLACK_KING);
		position.refreshCachedData();

		final Supplier<IPositionEvaluation> normalEvaluationFactory = () -> new CoeffCountPositionEvaluation(coeffs);
		final GameStageTablePositionEvaluator normalEvaluator = new GameStageTablePositionEvaluator(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS, normalEvaluationFactory);

		final Supplier<IPositionEvaluation> iterativeEvaluationFactory = () -> new AlgebraicPositionEvaluation(coeffs);
		final IterativeGameStageTablePositionEvaluator iterativeEvaluator = new IterativeGameStageTablePositionEvaluator(iterativeEvaluationFactory);

		for (int gameStage = GameStage.FIRST; gameStage < GameStage.LAST; gameStage++) {
			final CoeffCountPositionEvaluation normalEvaluation = (CoeffCountPositionEvaluation) normalEvaluator.evaluate(position, gameStage);

			final double whiteKingOpening = normalEvaluation.getCoeffCount(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.get(CombinedEvaluation.COMPONENT_OPENING).getCoeff(Color.WHITE, PieceType.KING, WHITE_KING_SQUARE));
			final double whiteKingMiddleGame = normalEvaluation.getCoeffCount(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.get(CombinedEvaluation.COMPONENT_MIDDLE_GAME).getCoeff(Color.WHITE, PieceType.KING, WHITE_KING_SQUARE));
			final double whiteKingEnding = normalEvaluation.getCoeffCount(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.get(CombinedEvaluation.COMPONENT_ENDING).getCoeff(Color.WHITE, PieceType.KING, WHITE_KING_SQUARE));

			final double blackKingOpening = normalEvaluation.getCoeffCount(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.get(CombinedEvaluation.COMPONENT_OPENING).getCoeff(Color.BLACK, PieceType.KING, BLACK_KING_SQUARE));
			final double blackKingMiddleGame = normalEvaluation.getCoeffCount(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.get(CombinedEvaluation.COMPONENT_MIDDLE_GAME).getCoeff(Color.BLACK, PieceType.KING, BLACK_KING_SQUARE));
			final double blackKingEnding = normalEvaluation.getCoeffCount(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS.get(CombinedEvaluation.COMPONENT_ENDING).getCoeff(Color.BLACK, PieceType.KING, BLACK_KING_SQUARE));

			Assert.assertEquals(1.0, whiteKingOpening + whiteKingMiddleGame + whiteKingEnding, EPS);
			Assert.assertEquals(-1.0, blackKingOpening + blackKingMiddleGame + blackKingEnding, EPS);

			Assert.assertTrue(0.0 <= whiteKingOpening && whiteKingOpening <= 1.0);
			Assert.assertTrue(0.0 <= whiteKingMiddleGame && whiteKingMiddleGame <= 1.0);
			Assert.assertTrue(0.0 <= whiteKingEnding && whiteKingEnding <= 1.0);
			Assert.assertTrue(-1.0 <= blackKingOpening && blackKingOpening <= 0.0);
			Assert.assertTrue(-1.0 <= blackKingMiddleGame && blackKingMiddleGame <= 0.0);
			Assert.assertTrue(-1.0 <= blackKingEnding && blackKingEnding <= 0.0);

			final IPositionEvaluation iterativeEvaluation = iterativeEvaluator.evaluate(position, gameStage);
			Assert.assertEquals(normalEvaluation.getEvaluation(), iterativeEvaluation.getEvaluation());
		}

	}
}
