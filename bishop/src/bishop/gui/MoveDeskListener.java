package bishop.gui;

import java.util.List;

import bisGui.widgets.MouseButton;
import bishop.base.BitBoard;
import bishop.base.HandlerRegistrarImpl;
import bishop.base.IHandlerRegistrar;
import bishop.base.Move;
import bishop.base.MoveParser;
import bishop.base.Square;
import bishop.controller.IMoveListener;
import bishop.controller.IPositionSource;

public class MoveDeskListener implements IDeskListener {
	
	private IDesk desk;
	private IPositionSource positionSource;
	private HandlerRegistrarImpl<IMoveListener> moveListenerRegistrar;
	private int beginSquare;
	private MoveParser parser;
	
	public MoveDeskListener() {
		desk = null;
		beginSquare = Square.NONE;
		parser = new MoveParser();
		moveListenerRegistrar = new HandlerRegistrarImpl<IMoveListener>();
	}
	
	/**
	 * This method is called when user clicks on some square.
	 * @param square coordinate of square
	 * @param button clicked button
	 */
	public void onSquareClick (final int square, final MouseButton button) {
		if (button == MouseButton.LEFT) {
			if (beginSquare == Square.NONE)
				processBeginSquare (square);
			else
				processTargetSquare (square, false);
		}
	}
	
	/**
	 * This method is called when user starts dragging from some square.
	 * @param square source square
	 * @param button dragging button
	 */
	public void onDrag (final int square, final MouseButton button) {
		if (button == MouseButton.LEFT) {
			if (processBeginSquare (square))
				desk.startDragging(square);
		}
	}
	
	/**
	 * This method is called when user drops object to some square.
	 * @param beginSquare begin square of the dragging
	 * @param targetSquare target square of the dragging
	 */
	public void onDrop (final int beginSquare, final int targetSquare) {
		if (beginSquare != Square.NONE) {
			processTargetSquare(targetSquare, true);
		}
	}
	
	private boolean processBeginSquare (final int square) {
		parser.initPosition(positionSource.getPosition());
		parser.filterByBeginSquare(square);
		
		if (!parser.getMoveList().isEmpty()) {
			beginSquare = square;		
		
			desk.changeMarkedSquares(BitBoard.getSquareMask(beginSquare));
			
			return true;
		}
		else {
			resetMove();
			
			return false;
		}
	}
	
	private void processTargetSquare (final int square, boolean isDrop) {
		parser.filterByTargetSquare(square);
		
		List<Move> moveList = parser.getMoveList();
		int moveCount = moveList.size();
		
		if (moveCount > 0) {
			if (moveCount > 1) {
				final int onTurn = positionSource.getPosition().getOnTurn();
				final int promotionPieceType = desk.selectPromotionPieceType(onTurn);
				
				parser.filterByPromotionPieceType(promotionPieceType);
				
				moveList = parser.getMoveList();
				moveCount = moveList.size();
			}
			
			if (moveCount == 1) {
				final Move move = moveList.get(0);
				
				for (IMoveListener listener: moveListenerRegistrar.getHandlers())
					listener.onMove(move);
			}
			
			resetMove();
		}
		else {
			if (square != beginSquare && !isDrop)
				processBeginSquare(square);
			else
				resetMove();
		}
	}
	
	private void resetMove() {
		beginSquare = Square.NONE;
		
		desk.changeMarkedSquares(BitBoard.EMPTY);
		desk.stopDragging();
	}
		
	public void setPositionSource (final IPositionSource positionSource) {
		this.positionSource = positionSource;
	}

	public void setDesk (final IDesk desk) {
		this.desk = desk;
	}
	
	public IHandlerRegistrar<IMoveListener> getMoveListenerRegistrar() {
		return moveListenerRegistrar;
	}
}
