package bishop.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import parallel.Parallel;

public final class SerialSearchEngineFactory implements ISearchEngineFactory {
	
	private IPositionEvaluatorFactory positionEvaluatorFactory;
	private int maximalDepth;
	private Parallel parallel;

	public IPositionEvaluatorFactory getPositionEvaluatorFactory() {
		return positionEvaluatorFactory;
	}

	public void setPositionEvaluatorFactory(final IPositionEvaluatorFactory positionEvaluatorFactory) {
		this.positionEvaluatorFactory = positionEvaluatorFactory;
	}


	public int getMaximalDepth() {
		return maximalDepth;
	}


	public void setMaximalDepth(final int maximalDepth) {
		this.maximalDepth = maximalDepth;
	}
	
	/**
	 * Creates position evaluator used by the engine.
	 * @return position evaluator
	 */
	public IPositionEvaluator createPositionEvaluator() {
		return positionEvaluatorFactory.createEvaluator();
	}
	
	/**
	 * Creates new instance of search engine.
	 * @return new engine
	 */
	public ISearchEngine createEngine() {
		try {
			final int threadCount = (parallel != null) ? parallel.getThreadCount() : 1;
			final List<ISearchEngine> childEngineList;
			
			if (threadCount > 1) {
				childEngineList = new ArrayList<>(threadCount);
				
				for (int i = 0; i < threadCount; i++) {
					final SerialSearchEngine childEngine = createSingleEngine(Collections.<ISearchEngine>emptyList());
					childEngineList.add(childEngine);
				}
			}
			else
				childEngineList = null;
			
			final SerialSearchEngine searchEngine = createSingleEngine(childEngineList);
			
			return searchEngine;
		}
		catch (Throwable th) {
			throw new RuntimeException("Cannot create instance of search engine", th);
		}
	}

	private SerialSearchEngine createSingleEngine(final List<ISearchEngine> childEngineList) {
		final IPositionEvaluator evaluator = createPositionEvaluator();
		final SerialSearchEngine searchEngine = new SerialSearchEngine(parallel, childEngineList);
		
		searchEngine.setPositionEvaluator(evaluator);
		searchEngine.setMaximalDepth(maximalDepth);
		
		return searchEngine;
	}

	public Parallel getParallel() {
		return parallel;
	}

	public void setParallel(Parallel parallel) {
		this.parallel = parallel;
	}

}
