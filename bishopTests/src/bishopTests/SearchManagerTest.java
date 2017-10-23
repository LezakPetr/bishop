package bishopTests;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.DefaultAdditiveMaterialEvaluator;
import bishop.base.Fen;
import bishop.base.GlobalSettings;
import bishop.base.MoveList;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.engine.AlgebraicPositionEvaluation;
import bishop.engine.Evaluation;
import bishop.engine.HashTableImpl;
import bishop.engine.IPositionEvaluation;
import bishop.engine.ISearchEngine;
import bishop.engine.ISearchManager;
import bishop.engine.ISearchManagerHandler;
import bishop.engine.MaterialPositionEvaluatorFactory;
import bishop.engine.SearchInfo;
import bishop.engine.SearchManagerImpl;
import bishop.engine.SearchResult;
import bishop.engine.SerialSearchEngineFactory;
import utils.Holder;
import utils.Logger;

public class SearchManagerTest {
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
	
	@Test
	public void searchManagerTest() throws Exception {
		final TestValue[] testValueArray = {
			new TestValue("2k5/8/2K1R3/8/8/8/8/8 w - - 0 1", 1, Evaluation.getMateEvaluation(1), "e6e8"),
			new TestValue("7k/8/8/6RK/8/8/8/8 w - - 0 1", 4, Evaluation.getMateEvaluation(5), "h5g6"),
			new TestValue("8/8/8/8/6rk/8/8/7K b - - 0 1", 4, Evaluation.getMateEvaluation(5), "h4g3"),
			new TestValue("3r3k/8/1b6/8/3r4/8/2N5/3Q3K w - - 0 1", 1, PieceTypeEvaluations.getPieceTypeEvaluation(PieceType.QUEEN) + PieceTypeEvaluations.getPieceTypeEvaluation(PieceType.KNIGHT) - 2*PieceTypeEvaluations.getPieceTypeEvaluation(PieceType.ROOK), "d1h5"),
			new TestValue("k3r3/8/8/3N4/8/8/8/K7 w - - 0 1", 2, Evaluation.DRAW, "d5c7"),
			new TestValue("3k2q1/8/8/8/8/8/1R6/K7 w - - 0 1", 2, PieceTypeEvaluations.getPieceTypeEvaluation(PieceType.ROOK), "b2b8"),
			new TestValue("7k/4Np1p/q2n2p1/6P1/8/8/2R2PP1/2R3K1 w - - 0 1", 6, PieceTypeEvaluations.getPieceTypeEvaluation(PieceType.KNIGHT), "c2c8"),
			new TestValue("8/1k1K3R/8/8/8/8/8/8 w - - 0 1", 6, Evaluation.getMateEvaluation(7), "h7h6"),   // Test of hash tables
			new TestValue("QR6/7k/8/8/7q/8/6P1/6K1 b - - 0 1", 4, Evaluation.DRAW_BY_REPETITION, "h4e1"),   // Test of draw by repetition
			new TestValue("2N5/8/k2K4/8/p1PB4/P7/8/8 w - - 0 1", 7, Evaluation.getMateEvaluation(7), "d6c7"),
			new TestValue("1k6/8/2n5/3KNB2/8/8/8/8 w - - 0 1", 11, Evaluation.getMateEvaluation(11), "d5c6")
		};
		
		GlobalSettings.setDebug(true);
		Logger.setStream(System.out);
		
		final HashTableImpl hashTable = new HashTableImpl(24);

		final ISearchManager manager = new SearchManagerImpl();
		manager.setHashTable(hashTable);
		
		final Holder<Boolean> searchFinished = new Holder<Boolean>();
		
		final ISearchManagerHandler handler = new ISearchManagerHandler() {
			public void onSearchComplete(ISearchManager manager) {
				synchronized (searchFinished) {
					searchFinished.setValue(Boolean.TRUE);
					searchFinished.notify();
					
					Logger.logMessage("Handler called");
				}
			}

			public void onSearchInfoUpdate(final SearchInfo info) {
			}
		};
		
		manager.getHandlerRegistrar().addHandler(handler);
		
		// Prefetch
		System.out.println("Prefetching");
		doSearch(testValueArray, manager, searchFinished, 1);

		// Do the search
		long serialTime = 0;
		
		System.out.println("Searchching");
		
		for (int threadCount = 1; threadCount <= Runtime.getRuntime().availableProcessors(); threadCount++) {
			final long beginTime = System.currentTimeMillis();
			
			doSearch(testValueArray, manager, searchFinished, threadCount);
			
			final long endTime = System.currentTimeMillis();
			final long elapsedTime = endTime - beginTime;
			
			System.out.printf("Threads: %d, time: %.3fs%n", threadCount, 1e-3*elapsedTime);
			
			if (threadCount == 1)
				serialTime = elapsedTime;
			else {
				final double speedup = serialTime / (double) elapsedTime;
				System.out.printf("Speedup: %.2f%n", speedup);
			}
		}
		
		manager.getHandlerRegistrar().removeHandler(handler);
	}

	private void doSearch(final TestValue[] testValueArray,	final ISearchManager manager, final Holder<Boolean> searchFinished, final int threadCount) throws IOException, InterruptedException {
		final SerialSearchEngineFactory engineFactory = new SerialSearchEngineFactory();
		final Supplier<IPositionEvaluation> evaluationFactory = AlgebraicPositionEvaluation.getTestingFactory();
		
		engineFactory.setPositionEvaluatorFactory(new MaterialPositionEvaluatorFactory(evaluationFactory));
		engineFactory.setMaximalDepth(25);
		engineFactory.setEvaluationFactory(evaluationFactory);
		engineFactory.setMaterialEvaluator(DefaultAdditiveMaterialEvaluator.getInstance());

		manager.setEngineFactory(engineFactory);
		manager.setThreadCount(threadCount);

		manager.start();
		
		for (TestValue testValue: testValueArray) {
			final Fen fen = new Fen();
			fen.readFen(new PushbackReader(new StringReader(testValue.positionFen)));
			
			synchronized (searchFinished) {
				searchFinished.setValue(Boolean.FALSE);
			}
			
			utils.Logger.logMessage("Starting " + searchFinished.getValue());
			
			manager.setMaxHorizon(testValue.depth * ISearchEngine.HORIZON_GRANULARITY);
			manager.startSearching(fen.getPosition());
			
			utils.Logger.logMessage("Waiting for result " + searchFinished.getValue());
			
			synchronized (searchFinished) {
				while (!searchFinished.getValue())
					searchFinished.wait();
			}
			
			utils.Logger.logMessage ("Result received");
			
			manager.stopSearching();
			
			final SearchResult result = manager.getResult();
			System.out.println (result.getPrincipalVariation());
			Assert.assertEquals(testValue.positionFen, testValue.evaluation, result.getNodeEvaluation().getEvaluation());
			
			final MoveList principalVariation = result.getPrincipalVariation();
			Assert.assertEquals(testValue.positionFen, testValue.moveString, principalVariation.get(0).toString());
		}
		
		manager.stop();
	}

}
