package bishopTests;

import bishop.base.GlobalSettings;
import bishop.engine.PawnEndingKey;
import bishop.engine.PawnPromotionEstimatorTest;
import bishop.tablebase.BitArray;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import utils.Logger;
import utilsTest.*;

import java.util.LinkedList;
import java.util.List;



/**
 * Executable class that runs all tests.
 * @author Ing. Petr Ležák
 */
public class AllTests {
	
	private static final Class<?>[] classArray = {
		ColorTest.class,
		FileTest.class,
		RankTest.class,
		SquareTest.class,
		BoardConstantsTest.class,
		CastlingConstantsTest.class,
		BitLoopTest.class,
		MaterialHashTest.class,
		RepeatedPositionRegisterTest.class,
		MaxKingDistanceCalculatorTest.class,
		ImmutableOrdinalMapTest.class,
		IntUtilsTest.class,
		MixerTest.class,
		PawnEndingKeyTest.class,
		ParallelTaskRunnerTest.class,
		BitBoardTest.class,
		BitBoardCombinatorTest.class,
		BetweenTableTest.class,
		BitArrayTest.class,
		CombinatorialNumberSystemTest.class,
		TableMaterialEvaluatorTest.class,
		LineIndexerTest.class,
		FenTest.class,
		CombinedEvaluationTest.class,
		GameStageTablePositionEvaluatorTest.class,
		LegalMoveGeneratorTest.class,
		PseudoLegalMoveGeneratorTest.class,
		QuiescencePseudoLegalMoveGeneratorTest.class,
		UncompressMoveTest.class,
		PerftTest.class,
		MobilityCalculatorTest.class,
		AttackCalculatorTest.class,
		ReverseMoveGeneratorTest.class,
		VectorTest.class,
		VectorAlgorithmsTest.class,
		MatrixTest.class,
		OnlineLogisticModelTest.class,
		PieceMoveTablesTest.class,
		PositionTest.class,
		StaticExchangeEvaluatorTest.class,
		SearchEngineTest.class,
		SearchManagerTest.class,
		NotationTest.class,
		PgnTest.class,
		RangeTest.class,
		ImmutableProbabilisticSetTest.class,
		NumberArrayTest.class,
		PositionEvaluatorTest.class,
		TableResultCompressionTest.class,
		MateFinderTest.class,
		TablebaseTest.class,
		ChunkTest.class,
		TableDefinitionTest.class,
		EvaluationHashTableTest.class,
		BestMoveHashTableTest.class,
		PawnStructureEvaluatorTest.class,
		PositionIoTest.class,
		SimpleLinearModelTest.class,
		PawnEndingFileTableTest.class,
		PawnEndingEvaluatorTest.class,
		GradientOptimizerTest.class,
		ScalarFieldTest.class,
		NewtonSolverTest.class,
		LogisticRegressionTest.class,
		CholeskySolverTest.class,
		ProlongTableTest.class,
		PawnPromotionEstimatorTest.class
	};
	
	public static final void main (final String[] args) {
		final JUnitCore junitCore = new JUnitCore();
		GlobalSettings.setDebug(true);
		Logger.setStream(System.out);
		
		final List<Class<?>> failedTests = new LinkedList<Class<?>>();
		
		for (Class<?> testClass: classArray) {
			final Result result = junitCore.run(testClass);
			
			if (!result.wasSuccessful()) {
				final List<Failure> failureList = result.getFailures();
			
				for (Failure failure: failureList)
					System.out.println (failure.toString());
				
				failedTests.add(testClass);
			}
		}
		
		if (failedTests.isEmpty())
			System.out.println("All tests succeeded");
		else {
			System.out.println("Some test(s) failed:");
			
			for (Class<?> testClass: failedTests) {
				System.out.println (testClass.getName());
			}
		}
	}
}
