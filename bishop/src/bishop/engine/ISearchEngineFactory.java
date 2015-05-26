package bishop.engine;

public interface ISearchEngineFactory {
	/**
	 * Creates new instance of search engine.
	 * @return new engine
	 */
	public ISearchEngine createEngine();
}
