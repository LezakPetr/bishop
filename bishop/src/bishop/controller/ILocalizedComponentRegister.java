package bishop.controller;

public interface ILocalizedComponentRegister {
	/**
	 * Adds component from the register.
	 * @param component registered component
	 */
	public void addComponent (final ILocalizedComponent component);
	
	/**
	 * Removes component from the register.
	 * @param component registered component
	 */
	public void removeComponent (final ILocalizedComponent component);
	
	/**
	 * Updates languages of registered components.
	 */
	public void updateLanguage();
}
