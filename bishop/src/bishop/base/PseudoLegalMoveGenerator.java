package bishop.base;

import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PawnMoveTable;

public final class PseudoLegalMoveGenerator extends PseudoLegalMoveGeneratorBase {

	public static final int MAX_MOVES_IN_POSITION = 321;
	
	
    // Generates moves of some figure.
    // Returns if generation should continue.
    private boolean generateFigureMoves(final int figure) {
    	final boolean isShortMovingFigure = PieceType.isShortMovingFigure(figure);
    	final int onTurn = position.getOnTurn();
    	final long beginSquareMask = position.getPiecesMask(onTurn, figure);
    	final long occupancy = position.getOccupancy();
    	final long notOwnSquares = ~position.getColorOccupancy(onTurn);
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(figure);

    	// Loop through all squares with our figure
    	for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final long targetSquareMask = FigureAttackTable.getItem(figure, beginSquare) & notOwnSquares;

    		// Loop through all target squares
    		for (targetSquareLoop.init(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
    			final int targetSquare = targetSquareLoop.getNextSquare();

    			if (isShortMovingFigure || (BetweenTable.getItem(beginSquare, targetSquare) & occupancy) == 0) {
    				final int capturedPieceType = position.getPieceTypeOnSquare(targetSquare);

    				// Make and process move
    				move.finishNormalMove(targetSquare, capturedPieceType);

    				if (!walker.processMove(move))
    					return false;
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
    	final long beginSquareMask = position.getPiecesMask(onTurn, PieceType.PAWN);
    	final long occupancy = position.getOccupancy();
    	final long opponentSquares = position.getColorOccupancy(oppositeColor);
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(PieceType.PAWN);

    	// Loop through all squares with our pawns
    	for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final long moveMask = PawnMoveTable.getItem(onTurn, beginSquare) & ~occupancy;
    		final long captureMask = PawnAttackTable.getItem(onTurn, beginSquare) & opponentSquares;
    		final long targetSquareMask = moveMask | captureMask;

    		// Loop through all target squares
    		for (targetSquareLoop.init(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
    			final int targetSquare = targetSquareLoop.getNextSquare();

    			if ((BetweenTable.getItem(beginSquare, targetSquare) & occupancy) == 0) {
    				final int capturedPieceType = position.getPieceTypeOnSquare(targetSquare);

    				// Make and process move
    				if ((BoardConstants.RANK_18_MASK & BitBoard.getSquareMask(targetSquare)) != 0) {
    					// Pawn promotion
    					for (int promotionFigure = PieceType.PROMOTION_FIGURE_FIRST; promotionFigure < PieceType.PROMOTION_FIGURE_LAST; promotionFigure++) {
    						move.finishPromotion(targetSquare, capturedPieceType, promotionFigure);

    						if (!walker.processMove(move))
    							return false;
    					}
    				}
    				else {
    					// Normal move
    					move.finishNormalMove (targetSquare, capturedPieceType);

						if (!walker.processMove(move))
							return false;
    				}
    			}
    		}
    	}

    	return true;
    }

    // Generates castling moves.
    // Returns if generation should continue.
    private boolean generateCastlingMoves() {
    	final CastlingRights castlingRights = position.getCastlingRights();
    	final int onTurn = position.getOnTurn();
    	final int oppositeColor = Color.getOppositeColor(onTurn);

    	// If no castling is permitted skip all tests
    	if (!castlingRights.isRightForColor(onTurn))
    		return true;

    	// Check if king is in check
    	final int kingPosition = position.getKingPosition(onTurn);

    	if (position.isSquareAttacked(oppositeColor, kingPosition))
    		return true;

    	final long occupancy = position.getOccupancy();

    	// Try both castlings
    	move.setMovingPieceType(PieceType.KING);
		move.setBeginSquare(kingPosition);

    	for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
  	  		if (castlingRights.isRight(onTurn, castlingType) && (occupancy & BoardConstants.getCastlingMiddleSquareMask(onTurn, castlingType)) == 0) {
  	  			// We should test square that king goes across. This is same square as rook destination square.
  	  			final int testSquare = BoardConstants.getCastlingKingMiddleSquare(onTurn, castlingType);

  	  			if (!position.isSquareAttacked (oppositeColor, testSquare)) {
  	  				final int kingTargetSquare = BoardConstants.getCastlingKingTargetSquare(onTurn, castlingType);
  	  				move.finishCastling(kingTargetSquare);

  	  				if (!walker.processMove(move))
  	  					return false;
  	  			}
  	  		}
    	}

    	return true;
    }

    /**
     * Generates all legal moves in the position.
     * Calls walker for each generated move.
     */
    public void generateMoves() {
    	final int castlingRightIndex = position.getCastlingRights().getIndex();
    	final int epFile = position.getEpFile();

    	move.initialize(castlingRightIndex, epFile);
    	
    	// Generate figure moves, starting with king - this speeds up legal move checking 
    	for (int figure = PieceType.FIGURE_FIRST; figure < PieceType.FIGURE_LAST; figure++) {
    		if (!generateFigureMoves(figure))
    			return;
    	}
    	
    	if (!generatePawnMoves())
    		return;

    	if (!generateCastlingMoves())
    		return;

    	generateEnPassantMoves();
    }
}
