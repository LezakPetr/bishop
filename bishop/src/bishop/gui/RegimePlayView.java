package bishop.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import bishop.base.BitBoard;
import bishop.base.IHandlerRegistrar;
import bishop.controller.GameEditor;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.IRegimePlayListener;
import bishop.controller.RegimePlay;
import bishop.controller.SideSettings;
import bishop.controller.SideType;


public class RegimePlayView extends MoveRegimeView implements ILocalizedComponent {
	
	private JMenuItem enableSearchMenuItem;
	
	private final MoveDeskListener moveDeskListener;
	private final RegimePlay regimePlay;
	

	public RegimePlayView(final IApplicationView applicationView) {
		super (applicationView);
		
		this.regimePlay = application.getRegimePlay();
		this.moveDeskListener = new MoveDeskListener();
		
		this.regimePlay.getRegimeListenerRegistrar().addHandler(regimePlayListener);
		
		createMenus();
		application.getLocalizedComponentRegister().addComponent(this);
		
		updateMenuItemsEnabled();
	}
	
	private void createMenus() {
		final JMenu gameMenu = applicationView.getGameMenu();
		
		enableSearchMenuItem = new JMenuItem(enableSearchAction);
		gameMenu.add(enableSearchMenuItem);
	}
	
	/**
	 * Activates this regime.
	 */
	@Override
	protected void activateRegime() {
		super.activateRegime();
		
		final IDesk desk = applicationView.getDesk();
		final GameEditor gameEditor = application.getActualGameEditor();
		
		moveDeskListener.setDesk(desk);
		moveDeskListener.setPositionSource(gameEditor.getActualPositionSource());
		moveDeskListener.getMoveListenerRegistrar().addHandler(gameEditor.getMoveListener());
		
		updateMenuItemsEnabled();
	}
	
	/**
	 * Deactivates this regime.
	 */
	@Override
	protected void deactivateRegime() {
		final IDesk desk = applicationView.getDesk();		
		final IHandlerRegistrar<IDeskListener> deskListenerRegistrar = desk.getDeskListenerRegistrar();
		
		if (deskListenerRegistrar.isHandlerRegistered(moveDeskListener)) {
			deskListenerRegistrar.removeHandler(moveDeskListener);
		}

		final GameEditor gameEditor = application.getActualGameEditor();
		moveDeskListener.getMoveListenerRegistrar().removeHandler(gameEditor.getMoveListener());
		
		super.deactivateRegime();
		
		updateMenuItemsEnabled();
	}

	/**
	 * Removes all handlers from the application and frees resources.
	 */
	@Override
	public void destroy() {
		final JMenu gameMenu = applicationView.getGameMenu();
		
		gameMenu.remove(enableSearchMenuItem);
		
		application.getLocalizedComponentRegister().removeComponent(this);
		this.regimePlay.getRegimeListenerRegistrar().removeHandler(regimePlayListener);
		
		super.destroy();
	}
	
	private void updateMenuItemsEnabled () {
		final SideSettings sideSettings = regimePlay.getSideSettingsOnTurn();
		final boolean regimeActive = regimePlay.isRegimeActive();
		final boolean searchingEnabled = regimePlay.isSearchingEnabled();
		
		enableSearchAction.setEnabled(regimeActive && !searchingEnabled && sideSettings.getSideType() == SideType.COMPUTER);
	}
	
	@SuppressWarnings("serial")
	private final AbstractAction enableSearchAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			regimePlay.setSearchingEnabled (true);
			regimePlay.updateSide();
		}
	};
	
	private IRegimePlayListener regimePlayListener = new IRegimePlayListener() {
		@Override
		public void onGamePositionChanged() {
			onPositionChanged();
		}
		
		@Override
		public void deactivateUserInput() {
			final IDesk desk = applicationView.getDesk();
			final IHandlerRegistrar<IDeskListener> deskListenerRegistrar = desk.getDeskListenerRegistrar();
			
			if (deskListenerRegistrar.isHandlerRegistered(moveDeskListener))
				deskListenerRegistrar.removeHandler(moveDeskListener);
			
			desk.changeMarkedSquares(BitBoard.EMPTY);
			desk.stopDragging();
			updateMenuItemsEnabled();
		}
		
		@Override
		public void activateUserInput() {
			final IDesk desk = applicationView.getDesk();
			final IHandlerRegistrar<IDeskListener> deskListenerRegistrar = desk.getDeskListenerRegistrar();
			deskListenerRegistrar.addHandler(moveDeskListener);
			
			updateMenuItemsEnabled();
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

	@Override
	public void updateLanguage(final ILocalization localization) {
		enableSearchAction.putValue(Action.NAME, localization.translateString("Menu.game.enableSearch"));
	}
	

}
