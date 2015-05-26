package bishop.engine;


public interface ISearchEngineHandler {
	/**
	 * This method is called when search is complete.
	 * @param engine search engine
	 * @param task searched task
	 * @param result result of the search
	 */
	public void onSearchComplete (final ISearchEngine engine, final SearchTask task, final SearchResult result);
}
