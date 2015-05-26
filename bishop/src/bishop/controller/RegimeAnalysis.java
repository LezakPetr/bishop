package bishop.controller;

import bishop.base.HandlerRegistrarImpl;
import bishop.base.Position;
import bishop.engine.ISearchManager;
import bishop.engine.ISearchManager.ManagerState;

public class RegimeAnalysis extends MoveRegime<IMoveRegimeListener> {
	
	public RegimeAnalysis(final IApplication application) {
		super(application, new HandlerRegistrarImpl<IMoveRegimeListener>());
	}
	
	public void destroy() {
		super.destroy();
	}

	/**
	 * Activates this regime.
	 */
	public void activateRegime() {
		super.activateRegime();
				
		final SearchResources searchResources = application.getSearchResources();
		final ISearchManager searchManager = searchResources.getSearchManager();
		
		searchManager.setBookSearchEnabled(false);
		searchManager.setSingleMoveSearchEnabled(false);
		searchManager.setMaxTimeForMove(ISearchManager.TIME_FOR_MOVE_INFINITY);
		searchManager.start();
		
		positionChanged();
		
		final GameEditor gameEditor = application.getActualGameEditor();
		gameEditor.getGameListenerRegistrar().addHandler(gameListener);
	}
	
	/**
	 * Deactivates this regime.
	 */
	public void deactivateRegime() {
		final SearchResources searchResources = application.getSearchResources();
		final ISearchManager searchManager = searchResources.getSearchManager();
		
		stopSearching(searchManager);
		searchManager.stop();
		
		final GameEditor gameEditor = application.getActualGameEditor();
		gameEditor.getGameListenerRegistrar().removeHandler(gameListener);
		
		super.deactivateRegime();
	}
	
	private static void stopSearching(final ISearchManager searchManager) {
		if (searchManager.getManagerState() == ManagerState.SEARCHING) {
			searchManager.stopSearching();
		}
	}
	
	private void positionChanged() {
		final SearchResources searchResources = application.getSearchResources();
		final ISearchManager searchManager = searchResources.getSearchManager();
		
		stopSearching(searchManager);
		
		final GameEditor gameEditor = application.getActualGameEditor();
		final Position position = gameEditor.getActualPositionSource().getPosition();
		
		searchManager.startSearching(position);
	}
	
	private final IGameListener gameListener = new IGameListener() {
		public void onActualPositionChanged() {
			positionChanged();
		}
		
		public void onMove() {
			positionChanged();
		}

		public void onGameChanged() {
			positionChanged();
		}
	};

	/**
	 * Returns type of regime.
	 * @return regime type
	 */
	public RegimeType getRegimeType() {
		return RegimeType.ANALYSIS;
	}

}
