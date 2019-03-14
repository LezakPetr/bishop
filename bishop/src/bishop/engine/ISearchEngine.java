package bishop.engine;

import bishop.base.CombinedPositionEvaluationTable;
import bishop.base.IHandlerRegistrar;
import bishop.base.PieceTypeEvaluations;

public interface ISearchEngine {
	public enum EngineState {
		STOPPED,
		SEARCHING,
		STOPPING
	};
	
	public static final int HORIZON_BITS = 8;
	public static final int MAX_HORIZON = 1 << HORIZON_BITS;

	/**
	 * Sets maximal total depth of the search.
	 * Engine must be in STOPPED state.
	 * @param maxTotalDepth maximal total depth of the search
	 */
	public void setMaximalDepth (final int maxTotalDepth);

	/**
	 * Sets piece type evaluations.
	 * Engine must be in STOPPED state.
	 */
	public void setPieceTypeEvaluations(final PieceTypeEvaluations pieceTypeEvaluations);

	/**
	 * Sets position evaluator.
	 * Engine must be in STOPPED state.
	 * @param evaluator position evaluator
	 */
	public void setPositionEvaluator (final IPositionEvaluator evaluator);
	
	/**
	 * Sets hash table for the manager.
	 * Engine must be in STOPPED state.
	 */
	public void setHashTable (final IEvaluationHashTable evaluationHashTable, final IBestMoveHashTable bestMoveHashTable);

	/**
	 * Sets combined position evaluation table.
	 * Engine must be in STOPPED state.
	 * @param table hash table
	 */
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table);

	/**
	 * Searches given task and returns results. Changes state from STOPPED
	 * to SEARCHING and when search is finished changes state from SEARCHING
	 * to STOPPED.
	 * @param task search task
	 */
	public SearchResult search(final SearchTask task);

	/**
	 * Stops the searching. If state is SEARCHING it is changed to STOPPING.
	 * Method returns immediately and ensures that method search returns as soon
	 * as possible in the future.
	 */
	public void stopSearching();
		
	/**
	 * Returns current state of the engine.
	 * @return engine state
	 */
	public EngineState getEngineState();
	
	/**
	 * Clips task boundaries.
	 * @param alpha lower boundary
	 * @param beta upper boundary
	 */
	public void updateTaskBoundaries (final int alpha, final int beta);
		
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

	/**
	 * Returns registrar for search engine handlers.
	 * @return registrar
	 */
	public IHandlerRegistrar<ISearchEngineHandler> getHandlerRegistrar();
	
	/**
	 * Returns number of searched nodes.
	 * 
	 * @return number of searched nodes
	 */
	public long getNodeCount();

	/**
	 * Clears the engine.
	 * Engine must be in STOPPED state.
	 */
	public void clear();
}
