package bishopTests;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import org.junit.Assert;
import org.junit.Test;
import bishop.base.Fen;
import bishop.base.Holder;
import bishop.base.MoveList;
import bishop.base.Position;
import bishop.engine.Evaluation;
import bishop.engine.ISearchEngine;
import bishop.engine.ISearchEngineHandler;
import bishop.engine.MaterialPositionEvaluator;
import bishop.engine.RepeatedPositionRegister;
import bishop.engine.SearchResult;
import bishop.engine.SearchSettings;
import bishop.engine.SearchTask;
import bishop.engine.SerialSearchEngine;

public class SearchEngineTest {
	
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
		new TestValue("8/1k1K4/7R/8/8/8/8/8 b - - 1 1", 5, -Evaluation.getMateEvaluation(6), "b7b8"),
		new TestValue("QR6/7k/8/8/7q/8/6P1/6K1 b - - 0 1", 4, Evaluation.DRAW, "h4e1"),
		new TestValue("8/1k1K3R/8/8/8/8/8/8 w - - 0 1", 6, Evaluation.getMateEvaluation(7), "h7h6")
	};


	
	@Test
	public void searchEngineTest() throws Exception {
		final SerialSearchEngine engine = new SerialSearchEngine();
		final MaterialPositionEvaluator evaluator = new MaterialPositionEvaluator();

		final Holder<SearchResult> searchResult = new Holder<SearchResult>();
		
		final ISearchEngineHandler handler = new ISearchEngineHandler() {
			public void onSearchComplete (final ISearchEngine engine, final SearchTask task, final SearchResult result) {
				synchronized (searchResult) {
					searchResult.setValue(result);
					searchResult.notify();
				}
			}
		};
		
		engine.getHandlerRegistrar().addHandler(handler);
		engine.setPositionEvaluator(evaluator);
		engine.setMaximalDepth(20);
		engine.setSearchSettings(new SearchSettings());
		engine.start();
		
		// Prewarm
		for (int i = 0; i < 3; i++)
			doCalculation(engine, searchResult);
		
		// Measure
		final long beginTime = System.currentTimeMillis();
		long nodeCount = 0;
		
		for (int i = 0; i < 3; i++)
			nodeCount += doCalculation(engine, searchResult);
		
		final long endTime = System.currentTimeMillis();
		
		engine.stop();
		engine.getHandlerRegistrar().removeHandler(handler);
		
		final long time = endTime - beginTime;
		final long nodesPerSec = 1000 * nodeCount / time;
		
		System.out.println ("Node count: " + nodeCount + ", Time: " + time + "ms, Nodes per second: " + nodesPerSec);
	}


	private long doCalculation(final SerialSearchEngine engine, final Holder<SearchResult> searchResult) throws IOException, InterruptedException {
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
			task.setRootMaterialEvaluation(position.getMaterialEvaluation());
			
			searchResult.setValue(null);
			engine.startSearching(task);
			
			SearchResult result;
			
			synchronized (searchResult) {
				while ((result = searchResult.getValue()) == null)
					searchResult.wait();
			}
			
			engine.stopSearching();
			
			
			final MoveList principalVariation = result.getPrincipalVariation();
			System.out.println (principalVariation.toString());
			
			Assert.assertEquals(testValue.positionFen, testValue.evaluation, result.getNodeEvaluation().getEvaluation());
			
			if (testValue.moveString != null)
				Assert.assertEquals(testValue.moveString, principalVariation.get(0).toString());
			else
				Assert.assertEquals(0, principalVariation.getSize());
			
			nodeCount += engine.getNodeCount();
		}
		return nodeCount;
	}
}
