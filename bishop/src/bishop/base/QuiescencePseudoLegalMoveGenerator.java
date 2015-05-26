package bishop.base;

import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PawnMoveTable;

public final class QuiescencePseudoLegalMoveGenerator extends PseudoLegalMoveGeneratorBase {

	private boolean generateChecks;
	
	// Generates moves of some short moving figure.
    // Returns if generation should continue.
    private boolean generateShortMovingFigureMoves(final int figure, final long additionalTargetSquares) {
    	final int onTurn = position.getOnTurn();
    	final long beginSquareMask = position.getPiecesMask(onTurn, figure);
    	
    	if (beginSquareMask == 0)
    		return true;
    	
    	final int oppositeColor = Color.getOppositeColor(onTurn);
    	final long opponentSquares = position.getColorOccupancy(oppositeColor);
    	final long possibleTargetSquares = opponentSquares | additionalTargetSquares;
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(figure);

    	// Loop through all squares with our figure
    	for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final long targetSquareMask = FigureAttackTable.getItem(figure, beginSquare) & possibleTargetSquares;

    		// Loop through all target squares
    		for (targetSquareLoop.init(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
    			final int targetSquare = targetSquareLoop.getNextSquare();
    			final int capturedPieceType = position.getPieceTypeOnSquare(targetSquare);

				// Make and process move
				move.finishNormalMove(targetSquare, capturedPieceType);

				if (!walker.processMove(move))
					return false;
    		}
    	}

    	return true;
    }
    
	// Generates moves of some figure.
    // Returns if generation should continue.
    private boolean generateLongMovingFigureMoves(final int figure, final long additionalTargetSquares) {
    	final int onTurn = position.getOnTurn();
    	final long beginSquareMask = position.getPiecesMask(onTurn, figure);
    	
    	if (beginSquareMask == 0)
    		return true;

    	final int oppositeColor = Color.getOppositeColor(onTurn);
    	final long occupancy = position.getOccupancy();
    	final long opponentSquares = position.getColorOccupancy(oppositeColor);
    	final long possibleTargetSquares = opponentSquares | additionalTargetSquares;
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(figure);

    	// Loop through all squares with our figure
    	for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final long targetSquareMask = FigureAttackTable.getItem(figure, beginSquare) & possibleTargetSquares;

    		// Loop through all target squares
    		for (targetSquareLoop.init(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
    			final int targetSquare = targetSquareLoop.getNextSquare();

    			if ((BetweenTable.getItem(beginSquare, targetSquare) & occupancy) == 0) {
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
    	final long pawnMask = position.getPiecesMask(onTurn, PieceType.PAWN);
    	
    	if (pawnMask == 0)
    		return true;
    	
    	final int oppositeColor = Color.getOppositeColor(onTurn);
    	final long occupancy = position.getOccupancy();
    	final long opponentSquares = position.getColorOccupancy(oppositeColor);
    	
    	final long moveBeginMask = (onTurn == Color.WHITE) ? BoardConstants.RANK_7_MASK : BoardConstants.RANK_2_MASK;
    	final long captureBeginMask = BoardConstants.getPawnsAttackedSquares (oppositeColor, opponentSquares);
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(PieceType.PAWN);

    	// Promotion
    	for (beginSquareLoop.init(pawnMask & moveBeginMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final long targetSquareMask = PawnMoveTable.getItem(onTurn, beginSquare) & ~occupancy;
    		final int targetSquare = BitBoard.getFirstSquare(targetSquareMask);
    		
    		if (targetSquare != Square.NONE) {
				if ((BetweenTable.getItem(beginSquare, targetSquare) & occupancy) == 0) {
					final int capturedPieceType = position.getPieceTypeOnSquare(targetSquare);
	
					// Make and process move
					for (int promotionFigure = PieceType.PROMOTION_FIGURE_FIRST; promotionFigure < PieceType.PROMOTION_FIGURE_LAST; promotionFigure++) {
						move.finishPromotion(targetSquare, capturedPieceType, promotionFigure);
	
						if (!walker.processMove(move))
							return false;
					}
	    		}
    		}
    	}
    	
    	// Captures
    	for (beginSquareLoop.init(pawnMask & captureBeginMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final long targetSquareMask = PawnAttackTable.getItem(onTurn, beginSquare) & opponentSquares;

    		// Loop through all target squares
    		for (targetSquareLoop.init(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
    			final int targetSquare = targetSquareLoop.getNextSquare();
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

    	return true;
    }
    
    /**
     * Generates all legal moves in the position.
     * Calls walker for each generated move.
     */
    @Override
    public void generateMoves() {
    	final int castlingRightIndex = position.getCastlingRights().getIndex();
    	final int epFile = position.getEpFile();

    	move.initialize(castlingRightIndex, epFile);
    	
    	final long knightCheckSquares;
    	final long orthogonalCheckSquares;
    	final long diagonalCheckSquares;
    	
    	if (generateChecks) {
    		final int onTurn = position.getOnTurn();
    		final int notOnTurn = Color.getOppositeColor(onTurn);
    		final int kingSquare = position.getKingPosition(notOnTurn);
    		final long occupancy = position.getOccupancy();
    		final long ownOccupancy = position.getColorOccupancy(onTurn);
    				
    		knightCheckSquares = FigureAttackTable.getItem(PieceType.KNIGHT, kingSquare) & ~ownOccupancy;
    		
    		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, kingSquare, occupancy);
    		orthogonalCheckSquares = LineAttackTable.getAttackMask(orthogonalIndex) & ~ownOccupancy;

    		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, kingSquare, occupancy);
    		diagonalCheckSquares = LineAttackTable.getAttackMask(diagonalIndex) & ~ownOccupancy;
    	}
    	else {
    		knightCheckSquares = BitBoard.EMPTY;
    		orthogonalCheckSquares = BitBoard.EMPTY;
    		diagonalCheckSquares = BitBoard.EMPTY;
    	}
    	

    	if (!generateShortMovingFigureMoves(PieceType.KING, BitBoard.EMPTY))
    		return;

    	if (!generateShortMovingFigureMoves(PieceType.KNIGHT, knightCheckSquares))
    		return;

    	if (!generateLongMovingFigureMoves(PieceType.QUEEN, orthogonalCheckSquares | diagonalCheckSquares))
    		return;

    	if (!generateLongMovingFigureMoves(PieceType.ROOK, orthogonalCheckSquares))
    		return;

    	if (!generateLongMovingFigureMoves(PieceType.BISHOP, diagonalCheckSquares))
    		return;

    	if (!generatePawnMoves())
    		return;

    	generateEnPassantMoves();
    }

    public void setGenerateChecks (final boolean generate) {
    	this.generateChecks = generate;
    }
}
