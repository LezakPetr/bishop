package bishop.controller;

import bishop.base.HandlerRegistrarImpl;
import bishop.base.IHandlerRegistrar;

public class RegimeEditPosition implements IRegime {
	
	private final HandlerRegistrarImpl<IRegimeListener> regimeListenerRegistrar;
	
	/**
	 * Creates the regime.
	 * @param application application object
	 */
	public RegimeEditPosition(final IApplication application) {
		this.regimeListenerRegistrar = new HandlerRegistrarImpl<IRegimeListener>();
	}
	
	/**
	 * Activates this regime.
	 */
	public void activateRegime() {
		for (IRegimeListener listener: regimeListenerRegistrar.getHandlers())
			listener.onRegimeActivated();
	}

	/**
	 * Deactivates this regime.
	 */
	public void deactivateRegime() {
		for (IRegimeListener listener: regimeListenerRegistrar.getHandlers())
			listener.onRegimeDeactivated();
	}
	
	private void destroyPositionEditor() {
	}
	
	/**
	 * Removes all handlers from the application and frees resources.
	 */
	public void destroy() {
		destroyPositionEditor();
	}
	
	/**
	 * Returns type of regime.
	 * @return regime type
	 */
	public RegimeType getRegimeType() {
		return RegimeType.EDIT_POSITION;
	}
	
	public IHandlerRegistrar<IRegimeListener> getRegimeListenerRegistrar() {
		return regimeListenerRegistrar;
	}

}
