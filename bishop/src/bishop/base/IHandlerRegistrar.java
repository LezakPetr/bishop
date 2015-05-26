package bishop.base;

public interface IHandlerRegistrar<H> {
	/**
	 * Adds handler to this registrar.
	 * @param handler handler
	 */
	public void addHandler (final H handler);

	/**
	 * Removes handler from this registrar.
	 * @param handler handler
	 */
	public void removeHandler (final H handler);
	
	/**
	 * Checks if given handler is registered to this registrar.
	 * @param handler handler
	 * @return true is handler is registered, false if not
	 */
	public boolean isHandlerRegistered (final H handler);
}
