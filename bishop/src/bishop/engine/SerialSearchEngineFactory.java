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
			final SerialSearchEngine searchEngine = createSingleEngine();
			
			return searchEngine;
		}
		catch (Throwable th) {
			throw new RuntimeException("Cannot create instance of search engine", th);
		}
	}

	private SerialSearchEngine createSingleEngine() {
		final IPositionEvaluator evaluator = createPositionEvaluator();
		final SerialSearchEngine searchEngine = new SerialSearchEngine(parallel);
		
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
