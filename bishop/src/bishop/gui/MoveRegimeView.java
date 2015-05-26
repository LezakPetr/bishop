package bishop.gui;

import bishop.controller.GameEditor;
import bishop.controller.IApplication;

public class MoveRegimeView implements IRegimeView {
	
	protected final IApplicationView applicationView;
	protected final IApplication application;
	protected final NotationPanel notationPanel;
	
	public MoveRegimeView(final IApplicationView applicationView) {
		this.applicationView = applicationView;
		this.application = applicationView.getApplication();
		this.notationPanel = new NotationPanel(applicationView.getApplication());
	}

	/**
	 * Activates this regime.
	 */
	protected void activateRegime() {
		applicationView.setRegimeComponent(notationPanel);
	}
	
	/**
	 * Deactivates this regime.
	 */
	protected void deactivateRegime() {
		applicationView.setRegimeComponent(null);
	}
	
	public void destroy() {
		notationPanel.destroy();
	}
	
	protected void onPositionChanged() {
		final IDesk desk = applicationView.getDesk();
		final GameEditor gameEditor = application.getActualGameEditor();
		
		desk.changePosition(gameEditor.getActualPositionSource().getPosition());
	}

}
