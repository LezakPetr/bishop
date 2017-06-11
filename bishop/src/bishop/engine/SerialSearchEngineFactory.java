package bishop.engine;

import java.util.function.Supplier;

import bishop.base.IMaterialEvaluator;
import parallel.Parallel;

public final class SerialSearchEngineFactory implements ISearchEngineFactory {
	
	private IMaterialEvaluator materialEvaluator;
	private IPositionEvaluatorFactory positionEvaluatorFactory;
	private Supplier<IPositionEvaluation> evaluationFactory;
	private int maximalDepth;

	public IPositionEvaluatorFactory getPositionEvaluatorFactory() {
		return positionEvaluatorFactory;
	}
	
	public void setEvaluationFactory(final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluationFactory = evaluationFactory;
	}
	
	/**
	 * Sets material evaluator.
	 * @param evaluator material evaluator
	 */
	public void setMaterialEvaluator(final IMaterialEvaluator evaluator) {
		this.materialEvaluator = evaluator;
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
		final SerialSearchEngine searchEngine = new SerialSearchEngine();
		
		searchEngine.setEvaluationFactory(evaluationFactory);
		searchEngine.setPositionEvaluator(evaluator);
		searchEngine.setMaterialEvaluator(materialEvaluator);
		searchEngine.setMaximalDepth(maximalDepth);
		
		return searchEngine;
	}

}
