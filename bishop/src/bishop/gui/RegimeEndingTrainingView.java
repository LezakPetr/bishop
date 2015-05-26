package bishop.gui;

import javax.swing.JOptionPane;

import bishop.base.IHandlerRegistrar;
import bishop.base.Move;
import bishop.controller.GameEditor;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.IMoveListener;
import bishop.controller.IMoveRegimeListener;
import bishop.controller.RegimeEndingTraining;

public class RegimeEndingTrainingView extends MoveRegimeView implements ILocalizedComponent {

	private final MoveDeskListener moveDeskListener;
	private final RegimeEndingTraining regimeEndingTraining;
	
	private IMoveListener moveListener = new IMoveListener() {
		public void onMove(final Move move) {
			if (regimeEndingTraining.validateMove(move)) {
				final GameEditor gameEditor = application.getActualGameEditor();
				gameEditor.makeMove(move);
			}
			else {
				final String message = application.getLocalization().translateString("RegimeEndingTrainingView.wrongMoveMessage");
				final String title = application.getLocalization().translateString("RegimeEndingTrainingView.wrongMoveTitle");
				
				JOptionPane.showMessageDialog(applicationView.getMainFrame(), message, title, JOptionPane.OK_OPTION);
			}
		}
	};

	public RegimeEndingTrainingView(final IApplicationView applicationView) {
		super(applicationView);
		
		this.moveDeskListener = new MoveDeskListener();
		this.application.getLocalizedComponentRegister().addComponent(this);
		this.regimeEndingTraining = application.getRegimeEndingTraining();
		
		this.regimeEndingTraining.getRegimeListenerRegistrar().addHandler(moveRegimeListener);
	}
	
	@Override
	public void destroy() {
		this.application.getLocalizedComponentRegister().removeComponent(this);
		this.regimeEndingTraining.getRegimeListenerRegistrar().removeHandler(moveRegimeListener);
		
		super.destroy();
	}

	@Override
	public void updateLanguage(final ILocalization localization) {
	}
	
	@Override
	public void activateRegime() {
		super.activateRegime();
		
		final GameEditor gameEditor = application.getActualGameEditor();
		final IDesk desk = applicationView.getDesk();
		
		moveDeskListener.setDesk(desk);
		moveDeskListener.setPositionSource(gameEditor.getActualPositionSource());
		moveDeskListener.getMoveListenerRegistrar().addHandler(moveListener);
		
		final IHandlerRegistrar<IDeskListener> deskListenerRegistrar = desk.getDeskListenerRegistrar();
		deskListenerRegistrar.addHandler(moveDeskListener);
	}
	
	@Override
	public void deactivateRegime() {
		final IDesk desk = applicationView.getDesk();
		final IHandlerRegistrar<IDeskListener> deskListenerRegistrar = desk.getDeskListenerRegistrar();
		
		deskListenerRegistrar.removeHandler(moveDeskListener);

		moveDeskListener.getMoveListenerRegistrar().removeHandler(moveListener);
		super.deactivateRegime();
	}
	
	private IMoveRegimeListener moveRegimeListener = new IMoveRegimeListener() {
		@Override
		public void onGamePositionChanged() {
			onPositionChanged();
		}
		
		@Override
		public void onRegimeActivated() {
			activateRegime();
		}

		@Override
		public void onRegimeDeactivated() {
			deactivateRegime();
		}
	};
	
}
