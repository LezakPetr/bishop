package bishopTests;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.function.Supplier;

import bishop.base.*;
import org.junit.Assert;
import org.junit.Test;

import bishop.engine.AlgebraicPositionEvaluation;
import bishop.engine.Evaluation;
import bishop.engine.IPositionEvaluation;
import bishop.engine.ISearchEngine;
import bishop.engine.MaterialPositionEvaluator;
import bishop.engine.RepeatedPositionRegister;
import bishop.engine.SearchResult;
import bishop.engine.SearchSettings;
import bishop.engine.SearchTask;
import bishop.engine.SerialSearchEngine;

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
		new TestValue("k7/1R6/2K5/8/8/8/8/8 b - - 0 1", 1, Evaluation.DRAW, null),
		new TestValue("2k5/8/2K1R3/8/8/8/8/8 w - - 0 1", 0, Evaluation.getMateEvaluation(1), "e6e8"),
		new TestValue("7k/8/8/6RK/8/8/8/8 w - - 0 1", 4, Evaluation.getMateEvaluation(5), "h5g6"),
		new TestValue("8/8/8/8/6rk/8/8/7K b - - 0 1", 4, Evaluation.getMateEvaluation(5), "h4g3"),
		new TestValue("8/1k1KQ3/8/8/8/8/8/8 b - - 0 1", 5, -Evaluation.getMateEvaluation(6), "b7b6"),
		new TestValue("QR6/7k/8/8/7q/8/6P1/6K1 b - - 0 1", 4, Evaluation.DRAW_BY_REPETITION, "h4e1"),
		new TestValue("8/1k1K3R/8/8/8/8/8/8 w - - 0 1", 6, Evaluation.getMateEvaluation(7), "h7h6")
	};


	
	@Test
	public void searchEngineTestSerial() throws Exception {
		runTest(false);
	}

	@Test
	public void searchEngineTestParallel() throws Exception {
		runTest(true);
	}

	public void runTest(final boolean runParallel) throws IOException, InterruptedException {
		final SerialSearchEngine engine = new SerialSearchEngine();
		engine.setEvaluationFactory(AlgebraicPositionEvaluation.getTestingFactory());
		
		configureEngine(engine);
		
		// Prewarm
		for (int i = 0; i < 3; i++)
			doCalculation(engine);
		
		// Measure
		final long beginTime = System.currentTimeMillis();
		long nodeCount = 0;
		
		for (int i = 0; i < 3; i++)
			nodeCount += doCalculation(engine);
		
		final long endTime = System.currentTimeMillis();
				
		final long time = endTime - beginTime;
		final long nodesPerSec = 1000 * nodeCount / time;
		
		System.out.println ("Node count: " + nodeCount + ", Time: " + time + "ms, Nodes per second: " + nodesPerSec);
	}


	public void configureEngine(final SerialSearchEngine engine) {
		final Supplier<IPositionEvaluation> evaluationFactory = AlgebraicPositionEvaluation.getTestingFactory();
		final MaterialPositionEvaluator evaluator = new MaterialPositionEvaluator(evaluationFactory);
		
		engine.setPositionEvaluator(evaluator);
		engine.setMaximalDepth(20);
		engine.setSearchSettings(new SearchSettings());
		engine.setPieceTypeEvaluations(pte);
	}


	private long doCalculation(final SerialSearchEngine engine) throws IOException, InterruptedException {
		long nodeCount = 0;
		
		for (TestValue testValue: TEST_VALUES) {	
			final Fen fen = new Fen();
			fen.readFen(new PushbackReader(new StringReader(testValue.positionFen)));
			
			final SearchTask task = new SearchTask();
			task.setHorizon(testValue.depth * ISearchEngine.HORIZON_GRANULARITY);
			
			final Position position = fen.getPosition();
			final RepeatedPositionRegister register = new RepeatedPositionRegister();
			
			register.clearAnsReserve(1);
			register.pushPosition(position, null);
			
			task.setRepeatedPositionRegister(register);
			task.getPosition().assign(position);

			final DefaultAdditiveMaterialEvaluator materialEvaluator = new DefaultAdditiveMaterialEvaluator(pte);
			final int materialEvaluation = materialEvaluator.evaluateMaterial(position.getMaterialHash());
			task.setRootMaterialEvaluation(materialEvaluation);
			
			final SearchResult result = engine.search(task);			
			
			final MoveList principalVariation = result.getPrincipalVariation();
			System.out.println (principalVariation.toString());
			
			Assert.assertEquals(testValue.positionFen, testValue.evaluation, result.getNodeEvaluation().getEvaluation());
			
			if (testValue.moveString != null)
				Assert.assertEquals(testValue.positionFen, testValue.moveString, principalVariation.get(0).toString());
			else
				Assert.assertEquals(0, principalVariation.getSize());
			
			nodeCount += engine.getNodeCount();
		}
		return nodeCount;
	}
}
