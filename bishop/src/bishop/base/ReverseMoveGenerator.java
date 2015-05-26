package bishop.base;

import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.ReversePawnMoveTable;

public class ReverseMoveGenerator implements IMoveGenerator {
	
	private IMoveWalker walker;
	private Position position;
	private final Move move = new Move();
	
	
	public MoveGeneratorType getGeneratorType() {
		return MoveGeneratorType.REVERSE;
	}
	
    // Generates moves of some figure.
    // Returns if generation should continue.
    private boolean generateFigureMoves(final int figure) {
    	final boolean isShortMovingFigure = PieceType.isShortMovingFigure(figure);
    	final int onTurn = position.getOnTurn();
    	final int oppositeColor = Color.getOppositeColor(onTurn);
    	final long targetSquareMask = position.getPiecesMask(oppositeColor, figure);
    	final long occupancy = position.getOccupancy();
    	final long emptySquares = ~occupancy;

    	final BitLoop targetSquareLoop = new BitLoop();
    	final BitLoop beginSquareLoop = new BitLoop();

    	// Loop through all target squares
    	for (targetSquareLoop.init(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
    		final int targetSquare = targetSquareLoop.getNextSquare();
    		final long beginSquareMask = FigureAttackTable.getItem(figure, targetSquare) & emptySquares;
    		
    		// Loop through all begin squares
    		for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    			final int beginSquare = beginSquareLoop.getNextSquare();
    			
    			if (isShortMovingFigure || (BetweenTable.getItem(beginSquare, targetSquare) & occupancy) == 0) {
    				if (!expandMove (figure, beginSquare, targetSquare)) {
    					return false;
    				}
    			}
    		}
    	}

    	return true;
    }

    // Generates moves of pawns.
    // Returns if generation should continue.
    private boolean generatePawnMoves() {
    	final int onTurn = position.getOnTurn();
    	final int oppositeColor = Color.getOppositeColor(onTurn);
    	final long targetSquareMask = position.getPiecesMask(oppositeColor, PieceType.PAWN);
    	final long occupancy = position.getOccupancy();
    	final long emptySquares = ~occupancy;

    	final BitLoop targetSquareLoop = new BitLoop();
    	final BitLoop beginSquareLoop = new BitLoop();

    	// Loop through all target squares
    	for (targetSquareLoop.init(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
    		final int targetSquare = targetSquareLoop.getNextSquare();
    		final long beginSquareMask = ReversePawnMoveTable.getItem(oppositeColor, targetSquare) & emptySquares;
    		
    		// Loop through all begin squares
    		for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    			final int beginSquare = beginSquareLoop.getNextSquare();

    			if ((BetweenTable.getItem(beginSquare, targetSquare) & occupancy) == 0) {
    				if (!expandMove (PieceType.PAWN, beginSquare, targetSquare)) {
    					return false;
    				}
    			}
    		}
    	}

    	return true;
    }

	private boolean expandMove(final int pieceType, final int beginSquare, final int targetSquare) {
		final int castlingRights = position.getCastlingRights().getIndex();
		
		// No EP
		move.initialize(castlingRights, File.NONE);
		move.setBeginSquare(beginSquare);
		move.setMovingPieceType(pieceType);
		move.finishNormalMove(targetSquare, PieceType.NONE);
		
		if (!walker.processMove(move))
			return false;
		
		// EP
    	final int onTurn = position.getOnTurn();
    	final int oppositeColor = Color.getOppositeColor(onTurn);
		final long pawnMask = position.getPiecesMask(onTurn, PieceType.PAWN) & BoardConstants.getEpRankMask(onTurn);
		
		long prevOppositePawnMask = position.getPiecesMask(oppositeColor, PieceType.PAWN) & ~BitBoard.getSquareMask(targetSquare);
		
		if (pieceType == PieceType.PAWN)
			prevOppositePawnMask |= BitBoard.getSquareMask(beginSquare);
		
		for (BitLoop loop = new BitLoop(pawnMask); loop.hasNextSquare(); ) {
			final int epSquare = loop.getNextSquare();
			final int epFile = Square.getFile(epSquare);
			final long connectedSquareMask = BoardConstants.getConnectedPawnSquareMask(epSquare);
			
			if ((prevOppositePawnMask & connectedSquareMask) != 0) {
				move.initialize(castlingRights, epFile);
				move.setBeginSquare(beginSquare);
				move.setMovingPieceType(pieceType);
				move.finishNormalMove(targetSquare, PieceType.NONE);
				
				if (!walker.processMove(move))
					return false;				
			}			
		}
		
		return true;
	}
	
	private boolean generateLongPawnMove(final int file) {
		final int onTurn = position.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);
		final int beginSquare = BoardConstants.getPawnInitialSquare(oppositeColor, file);
		final int targetSquare = BoardConstants.getEpSquare(oppositeColor, file);
		
		return expandMove(PieceType.PAWN, beginSquare, targetSquare);
	}

	@Override
	public void generateMoves() {
		final int epFile = position.getEpFile();
		
		if (epFile == File.NONE) {
	    	for (int figure = PieceType.FIGURE_FIRST; figure < PieceType.FIGURE_LAST; figure++) {
	    		if (!generateFigureMoves(figure))
	    			return;
	    	}
	    	
	    	generatePawnMoves();
		}
		else {
			generateLongPawnMove(epFile);
		}
	}

	@Override
	public void setPosition(final Position position) {
		this.position = position;
	}

	@Override
	public void setWalker(final IMoveWalker walker) {
		this.walker = walker;
	}
}
