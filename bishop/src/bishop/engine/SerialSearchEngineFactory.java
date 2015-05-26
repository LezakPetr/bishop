package bishop.engine;

public final class SerialSearchEngineFactory implements ISearchEngineFactory {
	
	private IPositionEvaluatorFactory positionEvaluatorFactory;
	private int maximalDepth;

	public IPositionEvaluatorFactory getPositionEvaluatorFactory() {
		return positionEvaluatorFactory;
	}


	public void setPositionEvaluatorFactory(final IPositionEvaluatorFactory positionEvaluatorFactory) {
		this.positionEvaluatorFactory = positionEvaluatorFactory;
	}


	public int getMaximalDepth() {
		return maximalDepth;
	}


	public void setMaximalDepth(int maximalDepth) {
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
			final IPositionEvaluator evaluator = createPositionEvaluator();
			final SerialSearchEngine searchEngine = new SerialSearchEngine();
			
			searchEngine.setPositionEvaluator(evaluator);
			searchEngine.setMaximalDepth(maximalDepth);
			
			return searchEngine;
		}
		catch (Throwable th) {
			throw new RuntimeException("Cannot create instance of search engine", th);
		}
	}

}
