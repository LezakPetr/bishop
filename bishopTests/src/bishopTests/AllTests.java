package bishopTests;

import java.util.LinkedList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import utils.Logger;
import utilsTest.RangeTest;
import bishop.base.GlobalSettings;



/**
 * Executable class that runs all tests.
 * @author Ing. Petr Ležák
 */
public class AllTests {
	
	private static final Class<?>[] classArray = {
		BetweenTableTest.class,
		LineIndexerTest.class,
		FenTest.class,
		LegalMoveGeneratorTest.class,
		PseudoLegalMoveGeneratorTest.class,
		QuiescencePseudoLegalMoveGeneratorTest.class,
		AttackCalculatorTest.class,
		ReverseMoveGeneratorTest.class,
		MatrixTest.class,
		PieceMoveTablesTest.class,
		PositionTest.class,
		SearchEngineTest.class,
		SearchManagerTest.class,
		NotationTest.class,
		PgnTest.class,
		RangeTest.class,
		PositionEvaluatorTest.class,
		PositionInterpreterTest.class,
		TableResultCompressionTest.class,
		MateFinderTest.class,
		TablebaseTest.class,
		ChunkTest.class,
		TableDefinitionTest.class
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
