package bishop.controller;

import bishop.base.Game;
import bishop.base.HandlerRegistrarImpl;
import bishop.base.IGameNode;
import bishop.base.IHandlerRegistrar;
import bishop.base.ITreeIterator;
import bishop.base.Move;
import bishop.base.Position;

public class GameEditor {
	
	private Game game;
	private ITreeIterator<IGameNode> actualNodeIterator;
	private HandlerRegistrarImpl<IGameListener> gameListenerRegistrar;
	
	
	public GameEditor() {
		game = new Game();
		actualNodeIterator = game.getRootIterator();
		gameListenerRegistrar = new HandlerRegistrarImpl<IGameListener>();
	}
	
	public void newGame(final Position startPosition) {
		game.newGame(startPosition);
		actualNodeIterator = game.getRootIterator();
		
		notifyGameChanged();
		notifyActualPositionChanged();
	}

	/**
	 * Returns position source that returns actual position.
	 * @return position source
	 */
	public IPositionSource getActualPositionSource() {
		return actualPositionSource;
	}
	
	/**
	 * Returns move listener that records moves into game.
	 * @return move listener that records moves into game
	 */
	public IMoveListener getMoveListener() {
		return moveListener;
	}
	
	/**
	 * Returns game edited by this editor.
	 * Game cannot be directly changed.
	 * @return edited game
	 */
	public Game getGame() {
		return game;
	}
	
	/**
	 * Sets game edited by this editor.
	 * @param game game edited by this editor
	 */
	public void setGame (final Game game) {
		this.game = game;
		this.actualNodeIterator = game.getRootIterator();
		
		notifyGameChanged();
		notifyActualPositionChanged();
	}
	
	public void onNodeChanged(final ITreeIterator<IGameNode> iterator) {
		notifyGameChanged();
	}
	
	public IHandlerRegistrar<IGameListener> getGameListenerRegistrar() {
		return gameListenerRegistrar;
	}
	
	private void notifyActualPositionChanged() {
		for (IGameListener listener: gameListenerRegistrar.getHandlers())
			listener.onActualPositionChanged();
	}

	private void notifyGameChanged() {
		for (IGameListener listener: gameListenerRegistrar.getHandlers())
			listener.onGameChanged();
	}

	private void notifyMoveDone() {
		for (IGameListener listener: gameListenerRegistrar.getHandlers())
			listener.onMove();
	}

	private IPositionSource actualPositionSource = new IPositionSource() {
		public Position getPosition() {
			return actualNodeIterator.getItem().getTargetPosition();
		}
	};
	
	private IMoveListener moveListener = new IMoveListener() {
		public void onMove (final Move move) {
			makeMove(move);
		}
	};
	
	public void setActualNodeIterator (final ITreeIterator<IGameNode> iterator) {
		this.actualNodeIterator = iterator.copy();
		
		notifyActualPositionChanged();
	}
	
	public ITreeIterator<IGameNode> getActualNodeIterator() {
		return actualNodeIterator.copy();
	}
	
	public void makeMove (final Move move) {
		actualNodeIterator = game.addChild(actualNodeIterator, move);
		
		notifyMoveDone();
	}
	
	/**
	 * Removes given node from the game.
	 * Given iterator will be invalidated.
	 * @param iterator node iterator
	 */
	public void removeNode (final ITreeIterator<IGameNode> iterator) {
		game.removeNode (iterator);
		
		notifyGameChanged();
		notifyActualPositionChanged();
	}

	/**
	 * Makes given child node first child of it's parent (ie. main line).
	 * @param iterator child iterator
	 */
	public void promoteChild (final ITreeIterator<IGameNode> iterator) {
		game.promoteChild (iterator);
		
		notifyGameChanged();
		notifyActualPositionChanged();
	}
}
