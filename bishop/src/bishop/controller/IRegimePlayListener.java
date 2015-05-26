package bishop.controller;

public interface IRegimePlayListener extends IMoveRegimeListener {
	/**
	 * Activates user input.
	 */
	public void activateUserInput();
	
	/**
	 * Deactivates user input.
	 */
	public void deactivateUserInput();
}
