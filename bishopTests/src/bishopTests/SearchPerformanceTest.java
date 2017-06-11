package bishopTests;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

import bishop.base.Game;
import bishop.base.Holder;
import bishop.base.IGameNode;
import bishop.base.IMaterialEvaluator;
import bishop.base.ITreeIterator;
import bishop.base.Move;
import bishop.base.MoveList;
import bishop.base.PgnReader;
import bishop.base.Position;
import bishop.controller.SearchResources;
import bishop.engine.AlgebraicPositionEvaluation;
import bishop.engine.Evaluation;
import bishop.engine.HashTableImpl;
import bishop.engine.IPositionEvaluation;
import bishop.engine.ISearchManager;
import bishop.engine.ISearchManagerHandler;
import bishop.engine.PositionEvaluatorSwitchFactory;
import bishop.engine.PositionEvaluatorSwitchSettings;
import bishop.engine.SearchInfo;
import bishop.engine.SearchManagerImpl;
import bishop.engine.SerialSearchEngineFactory;
import bishop.engine.TablebasePositionEvaluator;

public class SearchPerformanceTest {

	private final ISearchManager manager = new SearchManagerImpl();
	private final Holder<Boolean> searchFinished = new Holder<Boolean>();
	private final Holder<Long> endTimeHolder = new Holder<Long>();
	
	private final Move expectedMove = new Move();
	private final MoveList lastPrincipalVariation = new MoveList();

	
	protected void initializeSearchManager(final TablebasePositionEvaluator tablebaseEvaluator, final long maxTimeForPosition) {
		final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
		
		final URL rootUrl;
		
		try {
			final File bishopDirectory = new File("../bishop/bin/");
			rootUrl = bishopDirectory.toURI().toURL();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
		final IMaterialEvaluator materialEvaluator = SearchResources.createMaterialEvaluator(rootUrl);
		final Supplier<IPositionEvaluation> positionEvaluationFactory = SearchResources.createEvaluationFactory(rootUrl);
		
		final PositionEvaluatorSwitchFactory evaluatorFactory = new PositionEvaluatorSwitchFactory(settings, positionEvaluationFactory);

		final SerialSearchEngineFactory engineFactory = new SerialSearchEngineFactory();
		final int threadCount = Math.min(Runtime.getRuntime().availableProcessors(), SearchResources.MAX_THREADS);
		final Supplier<IPositionEvaluation> evaluationFactory = AlgebraicPositionEvaluation.getTestingFactory();
		
		engineFactory.setPositionEvaluatorFactory(evaluatorFactory);
		engineFactory.setEvaluationFactory(evaluationFactory);
		engineFactory.setMaximalDepth(25);
		engineFactory.setMaterialEvaluator(materialEvaluator);
		
		final HashTableImpl hashTable = new HashTableImpl(20);
		
		manager.setTablebaseEvaluator(tablebaseEvaluator);
		manager.setEngineFactory(engineFactory);
		manager.setHashTable(hashTable);
		manager.setThreadCount(threadCount);
		
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
						}
						else {
							endTimeHolder.setValue(null);
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
	
	protected long testPosition(final Position position, final Move expectedMove) throws InterruptedException {
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
		
		synchronized (endTimeHolder) {
			if (endTimeHolder.getValue() == null) {
				elapsedTime = Long.MAX_VALUE;
			}
			else {
				elapsedTime = endTimeHolder.getValue() - beginTime;	
			}
		}
		
		return elapsedTime;
	}
	
	public long testGame(final Game game) throws InterruptedException {
		final ITreeIterator<IGameNode> iterator = game.getRootIterator();
		final Position position = iterator.getItem().getTargetPosition();
		
		iterator.moveFirstChild();
		
		final Move expectedMove = iterator.getItem().getMove();
		final long elapsedTime = testPosition(position, expectedMove);
		
		return elapsedTime;
	}
	
	public static List<Game> readGameList(final String testFile) throws IOException, FileNotFoundException {
		final PgnReader pgn = new PgnReader();
		
		try (final FileInputStream stream = new FileInputStream(testFile)) {
			pgn.readPgnFromStream(stream);
		}
		
		return pgn.getGameList();
	}
}
