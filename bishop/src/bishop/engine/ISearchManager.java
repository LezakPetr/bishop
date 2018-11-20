package bishop.engine;

import bishop.base.*;

public interface ISearchManager {
	enum ManagerState {
		STOPPED,
		WAITING,
		SEARCHING,
		TERMINATING,
		STOPPING
	};
	
	public static final long TIME_FOR_MOVE_INFINITY = 1000000000000L;
	
	/**
	 * Starts the manager.
	 * Changes state from STOPPED to WAITING.
	 */
	public void start();
	
	/**
	 * Stops the manager.
	 * Changes state from WAITING or SEARCHING to STOPPING and later to STOPPED.
	 */
	public void stop();

	/**
	 * Sets maximal horizon of the search.
	 * Manager must be in STOPPED or WAITING state.
	 * @param maxHorizon maximal horizon
	 */
	public void setMaxHorizon (final int maxHorizon);
	
	/**
	 * Sets maximal time for move.
	 * @param time maximal time for search of one move
	 */
	public void setMaxTimeForMove (final long time);
	
	/**
	 * Sets book.
	 * Manager must be in STOPPED state.
	 * @param book new book
	 */
	public void setBook (final IBook<?> book);
	
	/**
	 * Starts searching of given position.
	 * Changes state from WAITING to SEARCHING.
	 * @param position position to search
	 */
	public void startSearching (final Position position);
	
	/**
	 * Stops searching.
	 * Changes state from SEARCHING to TERMINATING and later to WAITING.
	 */
	public void stopSearching();
	
	/**
	 * Returns result of the search.
	 * Manager must be in WAITING state.
	 * @return result of the search
	 */
	public SearchResult getResult();
	
	/**
	 * Returns handler registrar.
	 * Modification of registrar is allowed just in STOPPED state.
	 * @return registrar
	 */
	public IHandlerRegistrar<ISearchManagerHandler> getHandlerRegistrar();
	
	/**
	 * Sets search engine factory used to create engines controlled by this manager.
	 * Manager must be in STOPPED state.
	 * @param factory search engine factory
	 */
	public void setEngineFactory (final ISearchEngineFactory factory);

	/**
	 * Sets piece type evaluations.
	 * Manager must be in STOPPED state.
	 */
	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations);

	/**
	 * Sets combined position evaluation table.
	 * Manager must be in STOPPED state.
	 * @param table evaluation table
	 */
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table);

	/**
	 * Returns hash table.
	 * @returns hash table
	 */
	public IHashTable getHashTable();
	
	/**
	 * Sets hash table for the manager.
	 * Manager must be in STOPPED state.
	 * @param table hash table
	 */
	public void setHashTable (final IHashTable table);
	
	/**
	 * Returns current state of the manager.
	 * @return manager state
	 */
	public ManagerState getManagerState();
	
	/**
	 * Gets search settings.
	 * @return search settings
	 */
	public SearchSettings getSearchSettings();

	/**
	 * Sets search settings.
	 * Manager must be in STOPPED state.
	 * @param searchSettings search settings
	 */
	public void setSearchSettings(final SearchSettings searchSettings);
	
	/**
	 * Gets thread count.
	 * @return thread count
	 */
	public int getThreadCount();

	/**
	 * Sets thread counts.
	 * Manager must be in STOPPED state.
	 * @param threadCount thread count
	 */
	public void setThreadCount(final int threadCount);

	/**
	 * Enables or disables book.
	 * Manager must be in STOPPED or WAITING state.
	 * @param enabled if book search is enabled
	 */
	public void setBookSearchEnabled(final boolean enabled);
	
	/**
	 * Enables or disables single move search.
	 * Manager must be in STOPPED or WAITING state.
	 * @param enabled if single move search is enabled
	 */
	public void setSingleMoveSearchEnabled(final boolean enabled);
	
	/**
	 * Sets tablebase position evaluator.
	 * Manager must be in STOPPED state.
	 * @param tablebaseEvaluator evaluator
	 */
	public void setTablebaseEvaluator(final TablebasePositionEvaluator tablebaseEvaluator);
}
