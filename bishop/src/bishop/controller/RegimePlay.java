package bishop.controller;

import javax.swing.SwingUtilities;

import bishop.base.HandlerRegistrarImpl;
import bishop.base.MoveList;
import bishop.base.Position;
import bishop.engine.ISearchManager;
import bishop.engine.ISearchManager.ManagerState;
import bishop.engine.ISearchManagerHandler;
import bishop.engine.SearchInfo;
import bishop.engine.SearchResult;


public class RegimePlay extends MoveRegime<IRegimePlayListener> {
	
	private boolean searchingEnabled;
	
	/**
	 * Creates the regime.
	 * @param application application object
	 */
	public RegimePlay(final IApplication application) {
		super (application, new HandlerRegistrarImpl<IRegimePlayListener>());
	}
	
	/**
	 * Activates this regime.
	 */
	public void activateRegime() {
		searchingEnabled = false;

		super.activateRegime();
				
		final ISearchManager searchManager = application.getSearchResources().getSearchManager();
		searchManager.getHandlerRegistrar().addHandler(searchManagerHandler);
		
		final GameEditor gameEditor = application.getActualGameEditor();
		gameEditor.getGameListenerRegistrar().addHandler(gameListener);

		startSearchManager();
		updateSide();
	}

	/**
	 * Deactivates this regime.
	 */
	public void deactivateRegime() {
		final ISearchManager searchManager = application.getSearchResources().getSearchManager();
		searchManager.stop();
		searchManager.getHandlerRegistrar().removeHandler(searchManagerHandler);
		
		final GameEditor gameEditor = application.getActualGameEditor();
		gameEditor.getGameListenerRegistrar().removeHandler(gameListener);
				
		super.deactivateRegime();
	}

	/**
	 * Removes all handlers from the application and frees resources.
	 */
	public void destroy() {		
		super.destroy();
	}

	private void startSearchManager() {
		final SearchResources resources = application.getSearchResources();
		final ISearchManager searchManager = resources.getSearchManager();
		
		searchManager.setBookSearchEnabled(true);
		searchManager.setSingleMoveSearchEnabled(true);
		
		resources.updateSettings();
		resources.getSearchManager().start();
	}
		
	public SideSettings getSideSettingsOnTurn() {
		final GameEditor gameEditor = application.getActualGameEditor();
		final Position actualPosition = gameEditor.getActualPositionSource().getPosition();
		final int onTurn = actualPosition.getOnTurn();
		final GameSettings gameSettings = application.getSettings().getGameSettings();
		
		return gameSettings.getSideSettings(onTurn);
	}
	
	public void updateSide() {
		final SearchResources searchResources = application.getSearchResources();
		final ISearchManager searchManager = searchResources.getSearchManager();
		
		// Stop search
		if (searchManager.getManagerState() == ManagerState.SEARCHING)
			searchManager.stopSearching();
		
		// Deactivate user input
		for (IRegimePlayListener listener: regimeListenerRegistrar.getHandlers())
			listener.deactivateUserInput();

		final GameEditor gameEditor = application.getActualGameEditor();
		final Position actualPosition = gameEditor.getActualPositionSource().getPosition();
		final GameSettings gameSettings = application.getSettings().getGameSettings();
		final SideSettings sideSettings = getSideSettingsOnTurn();
		
		switch (sideSettings.getSideType()) {
			case HUMAN:
				for (IRegimePlayListener listener: regimeListenerRegistrar.getHandlers())
					listener.activateUserInput();
				
				break;
			
			case COMPUTER:
				if (searchingEnabled) {
					final int onTurn = actualPosition.getOnTurn();
					
					searchManager.setMaxTimeForMove(gameSettings.getSideSettings(onTurn).getTimeForMove());
					searchManager.startSearching(actualPosition);
				}
				
				break;
		}
	}
	
	private ISearchManagerHandler searchManagerHandler = new ISearchManagerHandler() {
		public void onSearchComplete(final ISearchManager manager) {
			final Runnable runnable = new Runnable() {
				public void run() {
					manager.stopSearching();
					
					final SearchResult result = manager.getResult();
					final MoveList principalVariation = result.getPrincipalVariation();
					
					if (principalVariation.getSize() > 0) {
						final GameEditor gameEditor = application.getActualGameEditor();
						gameEditor.getMoveListener().onMove(principalVariation.get(0));
					}
				}
			};
			
			SwingUtilities.invokeLater(runnable);
		}

		public void onSearchInfoUpdate(final SearchInfo info) {
		}
	};
	
	
	private IGameListener gameListener = new IGameListener() {
		public void onActualPositionChanged() {
			searchingEnabled = false;
			
			updateSide();
		}
		
		public void onMove() {
			searchingEnabled = true;
			
			updateSide();
		}

		public void onGameChanged() {
		}
	};
	
	/**
	 * Returns type of regime.
	 * @return regime type
	 */
	public RegimeType getRegimeType() {
		return RegimeType.PLAY;
	}

	public boolean isSearchingEnabled() {
		return searchingEnabled;
	}

	public void setSearchingEnabled(final boolean enabled) {
		this.searchingEnabled = enabled;
	}

}
