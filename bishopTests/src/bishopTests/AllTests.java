package bishopTests;

import java.util.LinkedList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import utils.Logger;
import utilsTest.ImmutableOrdinalMapTest;
import utilsTest.ImmutableProbabilisticSetTest;
import utilsTest.MatrixTest;
import utilsTest.MixerTest;
import utilsTest.NeuralTest;
import utilsTest.NumberArrayTest;
import utilsTest.ParallelTaskRunnerTest;
import utilsTest.RangeTest;
import utilsTest.SimpleLinearModelTest;
import utilsTest.VectorAlgorithmsTest;
import utilsTest.VectorTest;
import bishop.base.GlobalSettings;



/**
 * Executable class that runs all tests.
 * @author Ing. Petr Ležák
 */
public class AllTests {
	
	private static final Class<?>[] classArray = {
		ImmutableOrdinalMapTest.class,
		MixerTest.class,
		ParallelTaskRunnerTest.class,
		BetweenTableTest.class,
		CombinatorialNumberSystemTest.class,
		TableMaterialEvaluatorTest.class,
		LineIndexerTest.class,
		FenTest.class,
		LegalMoveGeneratorTest.class,
		PseudoLegalMoveGeneratorTest.class,
		QuiescencePseudoLegalMoveGeneratorTest.class,
		UncompressMoveTest.class,
		PerftTest.class,
		AttackCalculatorTest.class,
		ReverseMoveGeneratorTest.class,
		VectorTest.class,
		VectorAlgorithmsTest.class,
		MatrixTest.class,
		PieceMoveTablesTest.class,
		PositionTest.class,
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
		HashTableTest.class,
		PawnStructureEvaluatorTest.class,
		PositionIoTest.class,
		SimpleLinearModelTest.class,
		NeuralTest.class
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
