package bishopTests;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.Assert;

import bishop.base.Color;
import bishop.base.Fen;
import bishop.base.Position;
import bishop.base.Rank;
import bishop.engine.AttackCalculator;
import bishop.engine.AttackEvaluationTable;
import bishop.engine.CoeffCountPositionEvaluation;
import bishop.engine.IPositionEvaluation;
import bishop.engine.PawnStructureCache;
import bishop.engine.PawnStructureCoeffs;
import bishop.engine.PawnStructureEvaluator;
import bishop.engine.PositionEvaluationCoeffs;

import math.Utils;

public class PawnStructureEvaluatorTest {
	
	private static final PawnStructureCoeffs COEFFS = PositionEvaluationCoeffs.MIDDLE_GAME_PAWN_STRUCTURE_COEFFS;
	
	private static class TestCase {
		public final String position;
		public final Map<Integer, Integer> coeffMap;
		
		public TestCase (final String position, final int[] coeffs) {
			this.position = position;
			this.coeffMap = new HashMap<>();
			
			for (int i = 0; i < coeffs.length; i += 2)
				coeffMap.put(coeffs[i], coeffs[i+1]);
		}
	}
	
	public TestCase[] TEST_CASES = {
		new TestCase(
			"5k2/8/6P1/5P2/1p6/1P6/P1P5/5K2 w - - 0 1",
			new int[] {
				COEFFS.getProtectedNotPassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getSinglePassedPawnBonusCoeff(Color.WHITE, Rank.R5), 1,
				COEFFS.getProtectedPassedPawnBonusCoeff(Color.WHITE, Rank.R6), 1
			}
		),
		new TestCase(
			"5k2/p1p5/1p6/1P6/5p2/6p1/8/5K2 w - - 0 1",
			new int[] {
				COEFFS.getProtectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getSinglePassedPawnBonusCoeff(Color.BLACK, Rank.R4), -1,
				COEFFS.getProtectedPassedPawnBonusCoeff(Color.BLACK, Rank.R3), -1
			}
		),
		new TestCase(
			"3k4/8/2p5/8/8/PP6/6PP/3K4 w - - 0 1",
			new int[] {
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.WHITE, Rank.R2), 2,
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
			}
		),
		new TestCase(
			"3k4/6pp/pp6/8/8/2P5/8/3K4 w - - 0 1",
			new int[] {
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.BLACK, Rank.R7), -2,
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
			}
		)
	};
	
	@Test
	public void test() throws IOException {
		final PositionEvaluationCoeffs positionEvaluationCoeffs = new PositionEvaluationCoeffs();
		
		final PawnStructureCache cache = new PawnStructureCache();
		final Supplier<IPositionEvaluation> evaluationFactory = () -> new CoeffCountPositionEvaluation(positionEvaluationCoeffs);
		final PawnStructureEvaluator evaluator = new PawnStructureEvaluator(COEFFS, cache, evaluationFactory);
		final AttackCalculator attackCalculator = new AttackCalculator(evaluationFactory);
		final Fen fen = new Fen();
		
		for (TestCase testCase: TEST_CASES) {
			fen.readFenFromString(testCase.position);
			
			final Position position = fen.getPosition();
			attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
			
			final CoeffCountPositionEvaluation evaluation = (CoeffCountPositionEvaluation) evaluator.evaluate(position, attackCalculator);
			
			final Map<Integer, Integer> givenCoeffMap = new HashMap<>();
			
			for (int coeff = COEFFS.getFirstCoeff(); coeff < COEFFS.getLastCoeff(); coeff++) {
				final int count = Utils.roundToInt(evaluation.getCoeffCount(coeff));
				
				if (count != 0)
					givenCoeffMap.put(coeff, count);
			}
			
			Assert.assertEquals(testCase.position, testCase.coeffMap, givenCoeffMap);
		}
	}
}
