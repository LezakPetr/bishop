package bishop.engine;

public interface ISearchManagerHandler {
	/**
	 * This method is called when search is complete.
	 * @param manager search manager
	 */
	public void onSearchComplete (final ISearchManager manager);
	
	/**
	 * This method is called when search info is updated.
	 * @param info updates search info
	 */
	public void onSearchInfoUpdate (final SearchInfo info);
}
