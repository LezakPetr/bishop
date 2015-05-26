package bishop.engine;

import bishop.base.IHandlerRegistrar;

public interface ISearchEngine {
	public enum EngineState {
		STOPPED,
		WAITING,
		SEARCHING,
		TERMINATING,
		STOPPING
	};
	
	public static final int HORIZON_FRACTION_BITS = 4;
	public static final int HORIZON_GRANULARITY = 1 << HORIZON_FRACTION_BITS;
	
	/**
	 * Starts the engine.
	 * Changes state from STOPPED to WAITING. 
	 */
	public void start();
	
	/**
	 * Stops the engine.
	 * Changes state from WAITING or SEARCHING to STOPPING and later to STOPPED.
	 */
	public void stop();

	/**
	 * Returns handler registrar of this engine.
	 * Modification is enabled just in STOPPED state.
	 * @return registrar
	 */
	public IHandlerRegistrar<ISearchEngineHandler> getHandlerRegistrar();

	/**
	 * Sets maximal total depth of the search.
	 * Engine must be in STOPPED state.
	 * @param maxTotalDepth maximal total depth of the search
	 */
	public void setMaximalDepth (final int maxTotalDepth);
	
	/**
	 * Sets position evaluator.
	 * Engine must be in STOPPED state.
	 * @param evaluator position evaluator
	 */
	public void setPositionEvaluator (final IPositionEvaluator evaluator);
	
	/**
	 * Sets hash table for the manager.
	 * Engine must be in STOPPED state.
	 * @param table hash table
	 */
	public void setHashTable (final IHashTable table);
	
	/**
	 * Sets task for searching. Changes state from WAITING to SEARCHING. 
	 * @param task search task
	 */
	public void startSearching(final SearchTask task);

	/**
	 * Stops the searching. Changes state from SEARCHING to TERMINATING and later to WAITING.
	 */
	public void stopSearching();
		
	/**
	 * Returns current state of the engine.
	 * @return engine state
	 */
	public EngineState getEngineState();
	
	/**
	 * Clips task boundaries.
	 * Engine must be in SEARCHING state.
	 * @param alpha lower boundary
	 * @param beta upper boundary
	 */
	public void updateTaskBoundaries (final int alpha, final int beta);
	
	/**
	 * Terminates the task.
	 * Engine must be in SEARCHING state.
	 */
	public void terminateTask();
	
	/**
	 * Gets search settings.
	 * @return search settings
	 */
	public SearchSettings getSearchSettings();

	/**
	 * Sets search settings.
	 * Engine must be in STOPPED state.
	 * @param searchSettings search settings
	 */
	public void setSearchSettings(final SearchSettings searchSettings);
	
	/**
	 * Sets tablebase evaluator.
	 * Engine must be in STOPPED state.
	 * @param evaluator evaluator
	 */
	public void setTablebaseEvaluator (final TablebasePositionEvaluator evaluator);
}
