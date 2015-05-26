package bishop.controller;

import bishop.base.HandlerRegistrarImpl;
import bishop.base.IHandlerRegistrar;

public abstract class MoveRegime<T extends IMoveRegimeListener> implements IRegime {
	
	protected final IApplication application;
	protected final HandlerRegistrarImpl<T> regimeListenerRegistrar;
	
	private boolean regimeActive;
	
	public MoveRegime(final IApplication application, final HandlerRegistrarImpl<T> regimeListenerRegistrar) {
		this.application = application;
		this.regimeListenerRegistrar = regimeListenerRegistrar;
	}
	
	/**
	 * Activates this regime.
	 */
	public void activateRegime() {
		regimeActive = true;
		
		updatePosition();
		
		final GameEditor gameEditor = application.getActualGameEditor();
		gameEditor.getGameListenerRegistrar().addHandler(gameListener);
		
		for (IRegimeListener listener: regimeListenerRegistrar.getHandlers())
			listener.onRegimeActivated();		
	}
	
	/**
	 * Deactivates this regime.
	 */
	public void deactivateRegime() {
		regimeActive = false;
		
		for (IRegimeListener listener: regimeListenerRegistrar.getHandlers())
			listener.onRegimeDeactivated();

		final GameEditor gameEditor = application.getActualGameEditor();
		gameEditor.getGameListenerRegistrar().removeHandler(gameListener);
	}
	
	public void destroy() {
	}
	
	private void updatePosition() {
		for (IMoveRegimeListener listener: regimeListenerRegistrar.getHandlers())
			listener.onGamePositionChanged();
	}
	
	private IGameListener gameListener = new IGameListener() {
		public void onActualPositionChanged() {
			updatePosition();
		}
		
		public void onMove() {
			updatePosition();
		}

		public void onGameChanged() {
		}
	};
	
	public IHandlerRegistrar<T> getRegimeListenerRegistrar() {
		return regimeListenerRegistrar;
	}
	
	public boolean isRegimeActive() {
		return regimeActive;
	}

}
