package bishopTests;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

import bishop.base.*;
import bishop.controller.SearchResources;
import bishop.engine.*;
import utils.Holder;

public class SearchPerformanceTest {

	public static class SearchStatictics {
		private final long time;
		private final long nodeCount;

		public SearchStatictics(final long time, final long nodeCount) {
			this.time = time;
			this.nodeCount = nodeCount;
		}

		public long getTime() {
			return time;
		}

		public long getNodeCount() {
			return nodeCount;
		}
	}

	private static final int HASH_TABLE_EXPONENT = 26;
	private static final int MAX_DEPTH = 25 * SerialSearchEngine.HORIZON_STEP_WITHOUT_EXTENSION;
	private static final long SEARCH_INFO_TIMEOUT = 200;

	private final ISearchManager manager = new SearchManagerImpl();
	private final Holder<Boolean> searchFinished = new Holder<Boolean>();
	private final Holder<Long> endTimeHolder = new Holder<Long>();
	private final Holder<Long> nodeCountHolder = new Holder<Long>();
	
	private final Move expectedMove = new Move();
	private final MoveList lastPrincipalVariation = new MoveList();
	private int threadCount = Runtime.getRuntime().availableProcessors();

	public void setThreadCount (final int threadCount) {
		this.threadCount = threadCount;
	}
	
	protected void initializeSearchManager(final TablebasePositionEvaluator tablebaseEvaluator, final long maxTimeForPosition) {
		final URL rootUrl;
		
		try {
			final File bishopDirectory = new File("../bishop/bin/");
			rootUrl = bishopDirectory.toURI().toURL();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		final PositionEvaluationCoeffs positionEvaluationCoeffs = SearchResources.createEvaluationCoeffs(rootUrl);
		final Supplier<IPositionEvaluation> positionEvaluationFactory = SearchResources.createEvaluationFactory(positionEvaluationCoeffs);
		
		final PositionEvaluatorSwitchFactory evaluatorFactory = new PositionEvaluatorSwitchFactory(positionEvaluationFactory);

		final SerialSearchEngineFactory engineFactory = new SerialSearchEngineFactory();

		engineFactory.setPositionEvaluatorFactory(evaluatorFactory);
		engineFactory.setMaximalDepth(MAX_DEPTH);

		final PieceTypeEvaluations pieceTypeEvaluations = positionEvaluationCoeffs.getPieceTypeEvaluations();
		engineFactory.setPieceTypeEvaluations(pieceTypeEvaluations);
		
		final EvaluationHashTableImpl evaluationHashTable = new EvaluationHashTableImpl(HASH_TABLE_EXPONENT);
		final BestMoveHashTableImpl bestMoveHashTable = new BestMoveHashTableImpl(HASH_TABLE_EXPONENT);
		
		manager.setTablebaseEvaluator(tablebaseEvaluator);
		manager.setEngineFactory(engineFactory);
		manager.setHashTable(evaluationHashTable, bestMoveHashTable);
		manager.setThreadCount(threadCount);
		manager.setPieceTypeEvaluations(pieceTypeEvaluations);
		manager.setSearchInfoTimeout(SEARCH_INFO_TIMEOUT);

		System.out.println (threadCount);
		
		manager.setMaxTimeForMove(maxTimeForPosition);
				
		final ISearchManagerHandler handler = new ISearchManagerHandler() {
			public void onSearchComplete(ISearchManager manager) {
				synchronized (searchFinished) {
					searchFinished.setValue(Boolean.TRUE);
					searchFinished.notify();
				}
			}

			public void onSearchInfoUpdate(final SearchInfo info) {
				final MoveList principalVariation = info.getPrincipalVariation();
				
				if (!principalVariation.equals(lastPrincipalVariation)) {
					lastPrincipalVariation.assign(principalVariation);
					
					System.out.println (info.getElapsedTime() + ": " + Evaluation.toString(info.getEvaluation()) + " [" + info.getHorizon() + "] " + principalVariation);
				}
				
				if (principalVariation.getSize() > 0) {
					final Move bestMove = principalVariation.get(0);

					synchronized (endTimeHolder) {
						if (bestMove.equals(expectedMove)) {
							if (endTimeHolder.getValue() == null)
								endTimeHolder.setValue(System.currentTimeMillis());

							if (nodeCountHolder.getValue() == null)
								nodeCountHolder.setValue(info.getNodeCount());
						}
						else {
							endTimeHolder.setValue(null);
							nodeCountHolder.setValue(null);
						}
					}
				}
			}
		};
		
		manager.getHandlerRegistrar().addHandler(handler);
		
		manager.start();
	}
	
	protected void stopSearchManager() {		
		manager.stop();
	}
	
	protected SearchStatictics testPosition(final Position position, final Move expectedMove) throws InterruptedException {
		this.expectedMove.assign(expectedMove);
		
		lastPrincipalVariation.clear();
		
		System.out.println (position.toString());
		System.out.println ("Expected move: " + expectedMove.toString());

		synchronized (searchFinished) {
			searchFinished.setValue(Boolean.FALSE);
		}
		
		synchronized (endTimeHolder) {
			endTimeHolder.setValue(null);
		}
		
		final long beginTime = System.currentTimeMillis();
		manager.startSearching(position);

		synchronized (searchFinished) {
			while (!searchFinished.getValue())
				searchFinished.wait();
		}
		
		manager.stopSearching();
		
		final long elapsedTime;
		final long nodeCount;
		
		synchronized (endTimeHolder) {
			if (endTimeHolder.getValue() == null) {
				elapsedTime = Long.MAX_VALUE;
			}
			else {
				elapsedTime = endTimeHolder.getValue() - beginTime;
			}

			if (nodeCountHolder.getValue() == null)
				nodeCount = Long.MAX_VALUE;
			else
				nodeCount = nodeCountHolder.getValue();
		}
		
		return new SearchStatictics(elapsedTime, nodeCount);
	}
	
	public SearchStatictics testGame(final Game game) throws InterruptedException {
		final ITreeIterator<IGameNode> iterator = game.getRootIterator();
		final Position position = iterator.getItem().getTargetPosition();
		
		iterator.moveFirstChild();
		
		final Move expectedMove = iterator.getItem().getMove();
		final SearchStatictics statictics = testPosition(position, expectedMove);
		
		return statictics;
	}
	
	public static List<Game> readGameList(final String testFile) throws IOException {
		final PgnReader pgn = new PgnReader();
		
		try (final FileInputStream stream = new FileInputStream(testFile)) {
			pgn.readPgnFromStream(stream);
		}
		
		return pgn.getGameList();
	}
}
