package bishop.engine;

import bishop.base.PieceTypeEvaluations;

public final class SerialSearchEngineFactory implements ISearchEngineFactory {

	private PieceTypeEvaluations pieceTypeEvaluations;
	private IPositionEvaluatorFactory positionEvaluatorFactory;
	private int maximalDepth;

	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations) {
		this.pieceTypeEvaluations = pieceTypeEvaluations;
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
			return createSingleEngine();
		}
		catch (Throwable th) {
			throw new RuntimeException("Cannot create instance of search engine", th);
		}
	}

	private SerialSearchEngine createSingleEngine() {
		final IPositionEvaluator evaluator = createPositionEvaluator();
		final SerialSearchEngine searchEngine = new SerialSearchEngine();

		searchEngine.setPositionEvaluator(evaluator);
		searchEngine.setPieceTypeEvaluations(pieceTypeEvaluations);
		searchEngine.setMaximalDepth(maximalDepth);
		
		return searchEngine;
	}

}
