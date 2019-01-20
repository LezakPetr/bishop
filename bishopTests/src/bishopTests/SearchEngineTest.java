package bishopTests;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.function.Supplier;

import bishop.base.*;
import bishop.engine.*;
import org.junit.Assert;
import org.junit.Test;

public class SearchEngineTest {
	private static final PieceTypeEvaluations pte = PieceTypeEvaluations.DEFAULT;
	
	private static class TestValue {
		public String positionFen;
		public int depth;
		public int evaluation;
		public String moveString;
		
		public TestValue (final String positionFen, final int depth, final int evaluation, final String moveString) {
			this.positionFen = positionFen;
			this.depth = depth;
			this.evaluation = evaluation;
			this.moveString = moveString;
		}
	}

	private static final TestValue[] TEST_VALUES = {
		new TestValue("3k4/8/3K4/8/8/8/8/8 w - - 0 1", 0, 0, null),
		new TestValue("3k1R2/8/3K4/8/8/8/8/8 b - - 0 1", 0, -Evaluation.getMateEvaluation(0), null),
		new TestValue("8/Q7/8/8/8/5kn1/5p2/5K2 w - - 0 1", 0, -Evaluation.getMateEvaluation(0), null),
		new TestValue("k7/1R6/2K5/8/8/8/8/8 b - - 0 1", 1, Evaluation.DRAW, null),
		new TestValue("2k5/8/2K1R3/8/8/8/8/8 w - - 0 1", 0, Evaluation.getMateEvaluation(1), "e6e8"),
		new TestValue("7k/8/8/6RK/8/8/8/8 w - - 0 1", 5, Evaluation.getMateEvaluation(5), "h5g6"),
		new TestValue("8/8/8/8/6rk/8/8/7K b - - 0 1", 5, Evaluation.getMateEvaluation(5), "h4g3"),
		new TestValue("QR6/7k/8/8/7q/8/6P1/6K1 b - - 0 1", 4, Evaluation.DRAW_BY_REPETITION, "h4e1")
	};

	@Test
	public void testExpectedResult() throws Exception {
		runTest();
	}

	/**
	 * Checks that the alpha-beta algorithm is behaving consistently with different alpha-beta
	 * boundaries.
	 */
	@Test
	public void testConsistentResults() throws Exception {
		for (boolean withCheckSearch: new boolean[] {false, true}) {
			final SerialSearchEngine engine = configureEngine(withCheckSearch);

			for (TestValue testValue : TEST_VALUES) {
				final Fen fen = new Fen();
				fen.readFen(new PushbackReader(new StringReader(testValue.positionFen)));

				for (int horizon = 0; horizon < 4 * SerialSearchEngine.HORIZON_STEP_WITHOUT_EXTENSION; horizon++) {
					final SearchResult correctResult = search(engine, fen, horizon, Evaluation.MIN, Evaluation.MAX);
					final int correctEvaluation = correctResult.getEvaluation();

					final SearchResult nullWindowResult = search(engine, fen, horizon, correctEvaluation, correctEvaluation);
					final int nullWindowEvaluation = nullWindowResult.getEvaluation();

					Assert.assertEquals(correctEvaluation, nullWindowEvaluation);

					final SearchResult drawWindowResult = search(engine, fen, horizon, Evaluation.DRAW, Evaluation.DRAW);
					final int drawWindowEvaluation = drawWindowResult.getEvaluation();

					Assert.assertTrue(correctEvaluation < 0 || correctEvaluation >= drawWindowEvaluation);
					Assert.assertTrue(correctEvaluation > 0 || correctEvaluation <= drawWindowEvaluation);

					final SearchResult lowerBoundResult = search(engine, fen, horizon, correctEvaluation + 100, Evaluation.MAX);
					final int lowerBoundEvaluation = lowerBoundResult.getEvaluation();

					Assert.assertTrue(correctEvaluation <= lowerBoundEvaluation);

					final SearchResult upperBoundResult = search(engine, fen, horizon, Evaluation.MIN, correctEvaluation - 100);
					final int upperBoundEvaluation = upperBoundResult.getEvaluation();

					Assert.assertTrue(correctEvaluation >= upperBoundEvaluation);
				}
			}
		}
	}

	public void runTest() throws IOException, InterruptedException {
		final SerialSearchEngine engine = configureEngine(true);

		// Prewarm
		final long prewarmNodeCount = doCalculation(engine);

		// Measure
		final long beginTime = System.currentTimeMillis();
		final long measureNodeCount = doCalculation(engine);
		final long endTime = System.currentTimeMillis();
				
		final long time = endTime - beginTime;
		final long nodesPerSec = 1000 * measureNodeCount / time;
		
		System.out.println ("Node count: " + measureNodeCount + ", Time: " + time + "ms, Nodes per second: " + nodesPerSec);

		Assert.assertEquals("SerialSearchEngine is not deterministic", prewarmNodeCount, measureNodeCount);
	}


	public SerialSearchEngine configureEngine(final boolean withCheckSearch) {
		final SerialSearchEngine engine = new SerialSearchEngine();
		final Supplier<IPositionEvaluation> evaluationFactory = AlgebraicPositionEvaluation.getTestingFactory();
		final MaterialPositionEvaluator evaluator = new MaterialPositionEvaluator(evaluationFactory);

		engine.setEvaluationFactory(AlgebraicPositionEvaluation.getTestingFactory());
		engine.setPositionEvaluator(evaluator);
		engine.setMaximalDepth(20);

		final SearchSettings settings = new SearchSettings();

		if (!withCheckSearch)
			settings.setMaxCheckSearchDepth(-1);

		engine.setSearchSettings(settings);
		engine.setPieceTypeEvaluations(pte);

		return engine;
	}

	private long doCalculation(final SerialSearchEngine engine) throws IOException, InterruptedException {
		engine.clear();

		long nodeCount = 0;
		
		for (TestValue testValue: TEST_VALUES) {	
			final Fen fen = new Fen();
			fen.readFen(new PushbackReader(new StringReader(testValue.positionFen)));

			final SearchResult result = search(engine, fen, SerialSearchEngine.HORIZON_STEP_WITHOUT_EXTENSION * testValue.depth, Evaluation.MIN, Evaluation.MAX);
			
			final MoveList principalVariation = result.getPrincipalVariation();
			System.out.println (principalVariation.toString());
			
			Assert.assertEquals(testValue.positionFen, testValue.evaluation, result.getEvaluation());
			
			if (testValue.moveString != null)
				Assert.assertEquals(testValue.positionFen, testValue.moveString, principalVariation.get(0).toString());
			else
				Assert.assertEquals(0, principalVariation.getSize());
			
			nodeCount += engine.getNodeCount();
		}

		return nodeCount;
	}

	private SearchResult search(final SerialSearchEngine engine, final Fen fen, final int horizon, final int alpha, final int beta) {
		final SearchTask task = new SearchTask();
		task.setHorizon(horizon);

		final Position position = fen.getPosition();
		final RepeatedPositionRegister register = new RepeatedPositionRegister();

		register.clearAnsReserve(1);
		register.pushPosition(position, null);

		task.setRepeatedPositionRegister(register);
		task.getPosition().assign(position);
		task.setAlpha(alpha);
		task.setBeta(beta);

		final DefaultAdditiveMaterialEvaluator materialEvaluator = new DefaultAdditiveMaterialEvaluator(pte);
		final int materialEvaluation = materialEvaluator.evaluateMaterial(position.getMaterialHash());
		task.setRootMaterialEvaluation(materialEvaluation);

		return engine.search(task);
	}
}
