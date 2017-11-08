package bishopTests;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.Color;
import bishop.base.Fen;
import bishop.base.Position;
import bishop.base.Rank;
import bishop.engine.AttackCalculator;
import bishop.engine.AttackEvaluationTableGroup;
import bishop.engine.CoeffCountPositionEvaluation;
import bishop.engine.GameStage;
import bishop.engine.GeneralEvaluatorSettings;
import bishop.engine.GeneralPositionEvaluator;
import bishop.engine.IPositionEvaluation;
import bishop.engine.PawnStructureCache;
import bishop.engine.PawnStructureCoeffs;
import bishop.engine.PositionEvaluationCoeffs;
import math.Utils;

public class PawnStructureEvaluatorTest {
	
	private static final PawnStructureCoeffs COEFFS = PositionEvaluationCoeffs.GAME_STAGE_COEFFS.get(GameStage.PAWNS_ONLY).pawnStructureCoeffs;
	
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
	
	public TestCase[] CLASSIFICATION_TEST_CASES = {
		new TestCase(
			"5k2/8/6P1/5P2/1p6/1P6/P1P5/5K2 w - - 0 1",
			new int[] {
				COEFFS.getProtectedNotPassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getSinglePassedPawnBonusCoeff(Color.WHITE, Rank.R5), 1,
				COEFFS.getProtectedPassedPawnBonusCoeff(Color.WHITE, Rank.R6), 1,
				COEFFS.getBlockedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.WHITE, Rank.R2), 2,
				COEFFS.getBlockedPawnBonusCoeff(Color.BLACK, Rank.R4), -1,
				COEFFS.getPawnMajorityCoeff(0), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(4), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(5), 1,
			}
		),
		new TestCase(
			"5k2/p1p5/1p6/1P6/5p2/6p1/8/5K2 w - - 0 1",
			new int[] {
				COEFFS.getProtectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getSinglePassedPawnBonusCoeff(Color.BLACK, Rank.R4), -1,
				COEFFS.getProtectedPassedPawnBonusCoeff(Color.BLACK, Rank.R3), -1,
				COEFFS.getBlockedPawnBonusCoeff(Color.WHITE, Rank.R5), 1,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.BLACK, Rank.R7), -2,
				COEFFS.getBlockedPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getPawnMajorityCoeff(0), -1,
				COEFFS.getOutsidePassedPawnBonusCoeff(4), -1,
				COEFFS.getOutsidePassedPawnBonusCoeff(5), -1,
			}
		),
		new TestCase(
			"3k4/8/2p5/8/8/PP6/6PP/3K4 w - - 0 1",
			new int[] {
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.WHITE, Rank.R2), 2,
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getPawnMajorityCoeff(0), 1,
				COEFFS.getPawnMajorityCoeff(1), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(2), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(4), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(5), 1,
			}
		),
		new TestCase(
			"3k4/6pp/pp6/8/8/2P5/8/3K4 w - - 0 1",
			new int[] {
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.BLACK, Rank.R7), -2,
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getPawnMajorityCoeff(0), -1,
				COEFFS.getPawnMajorityCoeff(1), -1,
				COEFFS.getOutsidePassedPawnBonusCoeff(2), -1,
				COEFFS.getOutsidePassedPawnBonusCoeff(4), -1,
				COEFFS.getOutsidePassedPawnBonusCoeff(5), -1,
			}
		),
		new TestCase(
			"3k4/8/8/3p4/2p1p3/2P1P3/8/3K4 w - - 0 1",
			new int[] {
				COEFFS.getProtectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R4), -2,
				COEFFS.getDoubleDisadvantageAttackPawnBonusCoeff(Color.BLACK, Rank.R5), -1,
				COEFFS.getBlockedPawnBonusCoeff(Color.WHITE, Rank.R3), 2,
				COEFFS.getBlockedPawnBonusCoeff(Color.BLACK, Rank.R4), -2,
			}
		),
		new TestCase(
			"3k4/2p5/1ppppp2/8/1P3P2/8/8/3K4 w - - 0 1",
			new int[] {
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -4,
				COEFFS.getConnectedPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -1,
				COEFFS.getDoublePawnBonusCoeff(Color.BLACK, Rank.R7), -1,
				COEFFS.getBlockedPawnBonusCoeff(Color.BLACK, Rank.R6), -2,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.WHITE, Rank.R4), 2,
				COEFFS.getPawnMajorityCoeff(2), -1,
				COEFFS.getOutsidePassedPawnBonusCoeff(2), -1,
			}
		),
		new TestCase(
			"3k4/8/4pp1p/8/4PPP1/P7/8/3K4 w - - 0 1",
			new int[] {
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.WHITE, Rank.R4), 3,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.WHITE, Rank.R4), 1,
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -2,
				COEFFS.getSinglePassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.BLACK, Rank.R6), -2,
				COEFFS.getPawnMajorityCoeff(0), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(4), 1,
			}
		),
		new TestCase(
			"3k4/8/4ppp1/8/4PPP1/P7/8/3K4 w - - 0 1",
			new int[] {
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.WHITE, Rank.R4), 3,
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -3,
				COEFFS.getSinglePassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getPawnMajorityCoeff(0), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(5), 1,
			}
		),
		new TestCase(
			"4k3/8/p1pp4/8/1PPP4/7P/8/4K3 w - - 0 1",
			new int[] {
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.WHITE, Rank.R4), 3,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.WHITE, Rank.R4), 1,
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -2,
				COEFFS.getSinglePassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(Color.BLACK, Rank.R6), -2,
				COEFFS.getPawnMajorityCoeff(0), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(4), 1,
			}
		),
		new TestCase(
			"4k3/8/1ppp4/8/1PPP4/7P/8/4K3 w - - 0 1",
			new int[] {
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.WHITE, Rank.R4), 3,
				COEFFS.getConnectedNotPassedPawnBonusCoeff(Color.BLACK, Rank.R6), -3,
				COEFFS.getSinglePassedPawnBonusCoeff(Color.WHITE, Rank.R3), 1,
				COEFFS.getPawnMajorityCoeff(0), 1,
				COEFFS.getOutsidePassedPawnBonusCoeff(5), 1,
			}
		)

	};
	
	public TestCase[] RULE_OF_SQUARE_TEST_CASES = {
		new TestCase(
			"8/8/8/8/2P4k/8/8/K7 w - - 0 1",
			new int[] {
				PositionEvaluationCoeffs.RULE_OF_SQUARE_BONUS, 1
			}
		),
		new TestCase(
			"8/8/8/8/2P4k/8/8/K7 b - - 0 1",
			new int[] {
			}
		),
		new TestCase(
			"7k/p4K2/8/8/8/8/8/8 b - - 0 1",
			new int[] {
				PositionEvaluationCoeffs.RULE_OF_SQUARE_BONUS, -1
			}
		),
		new TestCase(
			"7k/p4K2/8/8/8/8/8/8 w - - 0 1",
			new int[] {
			}
		),
		new TestCase(
			"1k6/5K2/8/8/p7/8/1P6/8 w - - 0 1",
			new int[] {
			}
		),
		new TestCase(
			"1k6/5K2/7B/8/p7/8/8/8 w - - 0 1",
			new int[] {
			}
		),
		new TestCase(
			"1k6/5K2/7P/8/p7/8/8/8 w - - 0 1",
			new int[] {
				PositionEvaluationCoeffs.RULE_OF_SQUARE_BONUS, 1
			}
		),
		new TestCase(
			"1k6/5K2/7P/8/p7/8/8/8 b - - 0 1",
			new int[] {
			}
		),
		new TestCase(
			"8/5K2/6P1/8/2k5/8/1p6/8 b - - 0 1",
			new int[] {
				PositionEvaluationCoeffs.RULE_OF_SQUARE_BONUS, -1
			}
		)
	};
	
	@Test
	public void testPawnClassification() throws IOException {
		doTest(CLASSIFICATION_TEST_CASES, COEFFS.getFirstCoeff(), COEFFS.getLastCoeff());
	}
	
	@Test
	public void testRuleOfSquare() throws IOException {
		doTest(RULE_OF_SQUARE_TEST_CASES, PositionEvaluationCoeffs.RULE_OF_SQUARE_BONUS, PositionEvaluationCoeffs.RULE_OF_SQUARE_BONUS + 1);
	}

	private void doTest(final TestCase[] testCases, final int firstCoeff, final int lastCoeff) throws IOException {
		final PositionEvaluationCoeffs positionEvaluationCoeffs = new PositionEvaluationCoeffs();
		
		final PawnStructureCache cache = new PawnStructureCache();
		final Supplier<IPositionEvaluation> evaluationFactory = () -> new CoeffCountPositionEvaluation(positionEvaluationCoeffs);
		final GeneralEvaluatorSettings settings = new GeneralEvaluatorSettings();
		final GeneralPositionEvaluator evaluator = new GeneralPositionEvaluator(settings, cache, evaluationFactory);
		final AttackCalculator attackCalculator = new AttackCalculator();
		final Fen fen = new Fen();
		final CoeffCountPositionEvaluation evaluation = (CoeffCountPositionEvaluation) evaluationFactory.get();
		
		for (TestCase testCase: testCases) {
			fen.readFenFromString(testCase.position);
			
			final Position position = fen.getPosition();
			attackCalculator.calculate(position, AttackEvaluationTableGroup.ZERO_GROUP);
			
			evaluation.clear();
			
			final IPositionEvaluation tacticalEvaluation = evaluator.evaluateTactical(position, attackCalculator);
			evaluation.addSubEvaluation(tacticalEvaluation);
			
			final IPositionEvaluation positionalEvaluation = evaluator.evaluatePositional(attackCalculator);
			evaluation.addSubEvaluation(positionalEvaluation);
						
			final Map<Integer, Integer> givenCoeffMap = new HashMap<>();
			
			for (int coeff = firstCoeff; coeff < lastCoeff; coeff++) {
				final int count = Utils.roundToInt(evaluation.getCoeffCount(coeff));
				
				if (count != 0)
					givenCoeffMap.put(coeff, count);
			}
			
			Assert.assertEquals(testCase.position, buildCoeffNameMap(testCase.coeffMap), buildCoeffNameMap(givenCoeffMap));
		}
	}
	
	private static Map<String, Integer> buildCoeffNameMap (final Map<Integer, Integer> coeffMap) {
		return coeffMap.entrySet().stream()
				.collect(
						Collectors.toMap(
								e -> coeffToName (e.getKey()),
								e-> e.getValue()
						)
				);
	}
	
	private static String coeffToName(final int coeff) {
		return coeff + " " + PositionEvaluationCoeffs.getCoeffRegistry().getName(coeff);
	}
}
