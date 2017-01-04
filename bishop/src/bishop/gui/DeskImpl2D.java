package bishop.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import utils.Logger;
import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.GlobalSettings;
import bishop.base.HandlerRegistrarImpl;
import bishop.base.IHandlerRegistrar;
import bishop.base.Piece;
import bishop.base.Position;
import bishop.base.Rank;
import bishop.base.Square;
import math.IVector;
import math.Vector2D;
import math.VectorImpl;
import math.Vectors;
import bisGui.graphics.IGraphics;
import bisGui.graphics.IImage;
import bisGui.math.ICoordinateTransformation;
import bisGui.math.ILinearCoordinateTransformation;
import bisGui.math.Transformations;
import bisGui.widgets.MouseButton;
import bisGui.widgets.Widget;
import bisGuiSwing.graphics.SwingBridge;

/**
 * 2D implementation of desk.
 * @author Ing. Petr Ležák
 */
public class DeskImpl2D implements IDesk {
	
	private HandlerRegistrarImpl<IDeskListener> deskListenerRegistrar = new HandlerRegistrarImpl<IDeskListener>(); 
	private Position position;
	
	private IBoard origBoard;
	private IPieceSet origPieceSet;
	
	private RasterBoard scaledBoard;
	private RasterPieceSet scaledPieceSet;
	
	private IVector squareSize;   // Unscaled size of the square
	
	private IVector boardCenter;
	private ICoordinateTransformation squareTransformation;
	
	private boolean dimensionsValid;
	
	private int orientation;
	private boolean showCoordinates;
	private long markedSquareMask;
	
	private IVector buttonPressPosition;
	private MouseButton pressedButton;
	private int draggingBeginSquare;
	private IVector draggingPoint;
	
	private Frame ownerFrame;
	
	private static void drawImage(final IGraphics g, final IImage image, final IVector imageCenter, final IVector point) {
		final IVector corner = Vectors.minus(point, imageCenter);
		
		g.drawImage(image, corner);
	}
	
	private Widget widget = new Widget() {
		@Override
		public void paintWidget (final IGraphics g) {
			updateDimensions();
			
			final IVector size = getSize();
			
			g.fillRect(Vector2D.ZERO, size, bisGui.graphics.Color.BLACK);
			
			paintBoard(g);
			paintAllPieces(g);
			paintAllMarks(g);			
			paintDraggedPiece(g);
		}
		
		private void paintBoard (final IGraphics g) {
			final IVector centerPoint = scaledBoard.getBoardCenterPoint();
			
			final IImage boardImage = scaledBoard.getBoardImage();
			drawImage(g, boardImage, centerPoint, boardCenter);
			
			if (showCoordinates) {
				final IImage coordinatesImage = scaledBoard.getCoordinatesImage(orientation);
				drawImage(g, coordinatesImage, centerPoint, boardCenter);
			}
		}
		
		private void paintDraggedPiece(final IGraphics g) {
			if (draggingBeginSquare != Square.NONE) {
				final Piece piece = position.getSquareContent(draggingBeginSquare);
				
				if (piece != null) {
					final IImage image = scaledPieceSet.getPieceImage(piece.getColor(), piece.getPieceType());
					final IVector pieceCenter = scaledPieceSet.getCenterPoint();
					
					drawImage(g, image, pieceCenter, draggingPoint);		
				}
			}
		}

		private void paintAllPieces (final IGraphics g) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				if (square != draggingBeginSquare) {
					final Piece piece = position.getSquareContent(square);
					
					if (piece != null) {
						final VectorImpl squareVector = getSquareVector (square);
						
						paintPiece (g, piece, squareVector);
					}
				}
			}

		}
		
		private void paintPiece(final IGraphics g, final Piece piece, final IVector squareVector) {
			final IImage image = scaledPieceSet.getPieceImage(piece.getColor(), piece.getPieceType());
			final IVector position = squareTransformation.transformPointForward(squareVector);
			final IVector pieceCenter = scaledPieceSet.getCenterPoint();
			
			drawImage(g, image, pieceCenter, position);
		}
		
		private void paintAllMarks (final IGraphics g) {
			for (BitLoop loop = new BitLoop(markedSquareMask); loop.hasNextSquare(); ) {
				final int square = loop.getNextSquare();
				final VectorImpl squareVector = getSquareVector (square);
				
				paintMark (g, squareVector);			
			}
		}

		private void paintMark(final IGraphics g, final VectorImpl squareVector) {
			final IImage image = scaledBoard.getMarkImage();
			final IVector position = squareTransformation.transformPointForward(squareVector);
			final IVector markCenter = scaledBoard.getMarkCenterPoint();
			
			drawImage(g, image, markCenter, position);
		}
		
		@Override
		public void mousePressed (final IVector position, final MouseButton button) {
			if (pressedButton == null) {
				pressedButton = button;
				buttonPressPosition = position;
			}
		}
		
		@Override
		public void mouseDragged (final IVector position, final MouseButton button) {
			final IVector oldDraggingPoint = draggingPoint;
			draggingPoint = position;
			
			if (draggingBeginSquare == Square.NONE) {
				final int square = getSquareOnPoint(buttonPressPosition);
				
				if (square != Square.NONE && pressedButton != null) {
					for (IDeskListener listener: deskListenerRegistrar.getHandlers()) {
						listener.onDrag(square, pressedButton);
					}
				}
			}
			else {
				final Rectangle rectangle = getPieceRectangle (oldDraggingPoint);
				rectangle.add (getPieceRectangle (draggingPoint));
				
				bridge.repaint(rectangle);
			}
		}
		
		@Override
		public void mouseReleased (final IVector position, final MouseButton button) {
			final int square = getSquareOnPoint(position);
			
			if (pressedButton != null && button == pressedButton) {
				if (draggingBeginSquare == Square.NONE) {
					// Click
					final int buttonPressSquare = getSquareOnPoint(buttonPressPosition);
					
					if (square != Square.NONE && square == buttonPressSquare) {
						for (IDeskListener listener: deskListenerRegistrar.getHandlers()) {
							listener.onSquareClick(square, button);
						}
					}				
				}
				else {
					// Drag
					for (IDeskListener listener: deskListenerRegistrar.getHandlers()) {
						listener.onDrop(draggingBeginSquare, square);
					}
				}
			
				pressedButton = null;
			}
		}

		@Override
		public IVector getPosition() {
			final Rectangle rectangle = bridge.getBounds();
			
			return Vector2D.fromComponents(rectangle.x, rectangle.y);
		}

		@Override
		public IVector getSize() {
			final Rectangle rectangle = bridge.getBounds();
			
			return Vector2D.fromComponents(rectangle.width, rectangle.height);
		}

	};
	
	
	private final SwingBridge bridge = new SwingBridge(widget);

	private ComponentListener componentListener = new ComponentAdapter() {
		public void componentResized (final ComponentEvent event) {
			dimensionsValid = false;
			bridge.repaint();
		}
	};
		
	private int getSquareOnPoint(final IVector point) {
		final IVector squareVector = squareTransformation.transformPointBackward(point);
		
		final int file = (int) Math.round(squareVector.getElement(Vector2D.COORDINATE_X));
		final int rank = (int) Math.round(squareVector.getElement(Vector2D.COORDINATE_Y));
		
		if (File.isValid(file) && Rank.isValid(rank))
			return Square.onFileRank(file, rank);
		else
			return Square.NONE;
	}
	
	private Rectangle getImageRectangle (final IVector imageSize, final IVector imageCenter, final IVector point) {
		final IVector leftCorner = Vectors.minus(point, imageCenter);
		
		final Rectangle rectangle = new Rectangle(
			(int) Math.floor(leftCorner.getElement(Vector2D.COORDINATE_X)),
			(int) Math.floor(leftCorner.getElement(Vector2D.COORDINATE_Y)),
			(int) Math.ceil(imageSize.getElement(Vector2D.COORDINATE_X)) + 1,
			(int) Math.ceil(imageSize.getElement(Vector2D.COORDINATE_Y)) + 1
		);
		
		return rectangle;		
	}
	
	/**
	 * Returns bounding rectangle around piece on given position.
	 * @param point position of the piece
	 * @return bounding rectangle
	 */
	private Rectangle getPieceRectangle (final IVector point) {
		return getImageRectangle(scaledPieceSet.getPieceSize(), scaledPieceSet.getCenterPoint(), point);
	}

	/**
	 * Returns bounding rectangle around piece on given position.
	 * @param point position of the piece
	 * @return bounding rectangle
	 */
	private Rectangle getSquareRectangle (final int square) {
		final IVector squareVector = getSquareVector(square);
		final IVector point = squareTransformation.transformPointForward(squareVector);
		final IVector imageCenter = Vectors.multiply(0.5, squareSize);

		return getImageRectangle(squareSize, imageCenter, point);
	}

	private void repaintSquares(final long squaresToRepaint) {
		for (BitLoop loop = new BitLoop(squaresToRepaint); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			final Rectangle rectangle = getSquareRectangle(square);
			
			bridge.repaint(rectangle);
		}
	}
	
	/**
	 * Creates 2D desk implementation with given owner frame.
	 * @param ownerFrame frame that owns this desk
	 */
	public DeskImpl2D (final Frame ownerFrame) {
		this.ownerFrame = ownerFrame;
		
		position = new Position();
		position.setInitialPosition();
		
		orientation = Color.WHITE;
		showCoordinates = true;
		markedSquareMask = BitBoard.EMPTY;
		dimensionsValid = false;
		draggingBeginSquare = Square.NONE;
		
		bridge.addComponentListener(componentListener);
	}
	
	/**
	 * Changes position to given one.
	 * Position is copied.
	 * @param position new position
	 */
	public void changePosition (final Position position) {
		this.position.assign(position);
		bridge.repaint();
		
		if (GlobalSettings.isDebug()) {
			logPositionEvaluation();
		}
	}
	
	/**
	 * Returns desk listener registrar.
	 * @return desk listener registrar
	 */
	public IHandlerRegistrar<IDeskListener> getDeskListenerRegistrar() {
		return deskListenerRegistrar;
	}
	
	private void updateDimensions() {
		if (!dimensionsValid) {
			final IVector position = widget.getPosition();
			final IVector size = widget.getSize();
			final IVector boardSize = Vectors.multiply(2, origBoard.getBoardCenterPoint());
			final double boardWidth = boardSize.getElement(0);
			final double boardHeight = boardSize.getElement(1);
			final double boardScale = Math.min(size.getElement(Vector2D.COORDINATE_X) / boardWidth, size.getElement(Vector2D.COORDINATE_Y) / boardHeight);
			scaledBoard = origBoard.renderScaledBoard(boardScale);
			
			final IVector squaresVector = Vectors.minus(scaledBoard.getMaxSquareCorner(), scaledBoard.getMinSquareCorner());
			
			squareSize = Vector2D.fromComponents(
				squaresVector.getElement(0) / File.LAST,
				squaresVector.getElement(1) / Rank.LAST
			);
			
			final IVector pieceImageSize = origPieceSet.getPieceSize();
			final double pieceScaleX = squareSize.getElement(Vector2D.COORDINATE_X) / pieceImageSize.getElement(Vector2D.COORDINATE_X);
			final double pieceScaleY = squareSize.getElement(Vector2D.COORDINATE_Y) / pieceImageSize.getElement(Vector2D.COORDINATE_Y);
			
			final double pieceScale = Math.min(pieceScaleX, pieceScaleY);
			scaledPieceSet = origPieceSet.renderScaledPieceSet(pieceScale);
			boardCenter = Vectors.plus(position, Vectors.multiply(0.5, size));
						
			final IVector orientationVector = (orientation == Color.WHITE) ? Vector2D.fromComponents(+1, -1) : Vector2D.fromComponents(-1, +1);
			final IVector squareTranslationVector = Vector2D.fromComponents (-0.5*(File.LAST-1), -0.5*(Rank.LAST-1));
			
			final ILinearCoordinateTransformation[] transformations = {
				Transformations.getTranslation(boardCenter),
				Transformations.getScaling(orientationVector),
				Transformations.getScaling(squareSize),
				Transformations.getTranslation(squareTranslationVector)
			};
			
			squareTransformation = Transformations.composeLinearTransformations(transformations);
			dimensionsValid = true;
		}
	}
	
	
	private static VectorImpl getSquareVector (final int square) {
		final int file = Square.getFile(square);
		final int rank = Square.getRank(square);

		return Vector2D.fromComponents(file, rank);
	}
	
	public IBoard getBoard() {
		return origBoard;
	}

	public void setBoard (final IBoard board) {
		this.origBoard = board;
		dimensionsValid = false;
		
		bridge.repaint();
	}

	public IPieceSet getPieceSet() {
		return origPieceSet;
	}

	public void setPieceSet (final IPieceSet pieceSet) {
		this.origPieceSet = pieceSet;
		dimensionsValid = false;
		
		bridge.repaint();
	}
	
	/**
	 * Sets orientation of the board.
	 * @param color color of viewing player
	 */
	public void setOrientation (final int color) {
		this.orientation = color;
		this.dimensionsValid = false;
		
		bridge.repaint();
	}
	
	/**
	 * Changes mask of marked squares.
	 * @param newMarkedSquareMask new mask of marked squares
	 */
	public void changeMarkedSquares (final long newMarkedSquareMask) {
		final long changedSquares = this.markedSquareMask ^ newMarkedSquareMask;
		this.markedSquareMask = newMarkedSquareMask;
		
		repaintSquares (changedSquares);
	}
	
	/**
	 * Returns position on the desk.
	 * Position cannot be changed.
	 * @return position
	 */
	public Position getPosition() {
		return position;
	}
	
	public Component getComponent() {
		return bridge;
	}

	public int selectPromotionPieceType(int color) {
		return PromotionDialog.selectPromotionPieceType(ownerFrame, color, origPieceSet);
	}
	
	/**
	 * Starts dragging from given square.
	 * @param square begin square of the dragging
	 */
	public void startDragging (final int square) {
		draggingBeginSquare = square;
		bridge.repaint();	
	}
	
	/**
	 * Stops dragging.
	 */
	public void stopDragging() {
		draggingBeginSquare = Square.NONE;
		bridge.repaint();
	}
	
	private void logPositionEvaluation() {
		try {
			/*
			position.refreshCachedData();
			
			final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
			final PositionEvaluatorSwitch evaluator = new PositionEvaluatorSwitch(settings);
			evaluator.evaluatePosition(position, Evaluation.MIN, Evaluation.MAX, new AttackCalculator());
			
			final PrintWriter writer = new PrintWriter(System.out);
			
			evaluator.writeLog(writer);
			writer.println ("Material evaluation: " + Evaluation.toString(position.getMaterialEvaluation()));
			writer.flush();
			*/
		}
		catch (Throwable th) {
			Logger.logException(th);
		}
	}

	public boolean getShowCoordinates() {
		return showCoordinates;
	}
	
	public void setShowCoordinates(final boolean show) {
		if (show != this.showCoordinates) {
			this.showCoordinates = show;
			bridge.repaint();
		}
	}
}
