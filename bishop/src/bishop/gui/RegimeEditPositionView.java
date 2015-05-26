package bishop.gui;

import bishop.base.IHandlerRegistrar;
import bishop.controller.IApplication;
import bishop.controller.IRegimeListener;
import bishop.controller.RegimeEditPosition;


public class RegimeEditPositionView {
	
	private PositionEditorPanel positionEditor;
	private final IApplicationView applicationView;
	private final IApplication application;
	private final RegimeEditPosition regimeEditPosition;
	
	
	public RegimeEditPositionView(final IApplicationView applicationView) {
		this.applicationView = applicationView;
		this.application = applicationView.getApplication();
		this.regimeEditPosition = application.getRegimeEditPosition();
		
		regimeEditPosition.getRegimeListenerRegistrar().addHandler(regimeListener);
	}
	
	public void destroy() {
		regimeEditPosition.getRegimeListenerRegistrar().removeHandler(regimeListener);
	}
	
	/**
	 * Activates this regime.
	 */
	public void activateRegime() {
		positionEditor = new PositionEditorPanel(applicationView);
		
		final IDesk desk = applicationView.getDesk();
		final IHandlerRegistrar<IDeskListener> deskListenerRegistrar = desk.getDeskListenerRegistrar();
		deskListenerRegistrar.addHandler(positionEditor);
		
		applicationView.setRegimeComponent(positionEditor);
	}

	/**
	 * Deactivates this regime.
	 */
	public void deactivateRegime() {
		final IDesk desk = applicationView.getDesk();
		final IHandlerRegistrar<IDeskListener> deskListenerRegistrar = desk.getDeskListenerRegistrar();
		
		deskListenerRegistrar.removeHandler(positionEditor);
		
		applicationView.setRegimeComponent(null);
		destroyPositionEditor();
	}
	
	private void destroyPositionEditor() {
		if (positionEditor != null) {
			positionEditor.destroy();
			positionEditor = null;
		}
	}
	
	private IRegimeListener regimeListener = new IRegimeListener() {
		@Override
		public void onRegimeDeactivated() {
			deactivateRegime();
		}
		
		@Override
		public void onRegimeActivated() {
			activateRegime();
		}
	};

}
