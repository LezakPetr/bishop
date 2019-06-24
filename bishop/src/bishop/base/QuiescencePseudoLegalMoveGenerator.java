package bishop.base;

import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PawnMoveTable;

public final class QuiescencePseudoLegalMoveGenerator extends PseudoLegalMoveGeneratorBase {

	private boolean generateChecks;
	private long allowedTargetSquares = BitBoard.FULL;
	
	// Generates moves of king.
    // Returns if generation should continue.
    private boolean generateKingMoves(final long possibleTargetSquares) {
    	final int onTurn = position.getOnTurn();

    	// Loop through all squares with our figure
		final int beginSquare = position.getKingPosition(onTurn);
		final long targetSquareMask = FigureAttackTable.getItem(PieceType.KING, beginSquare) & possibleTargetSquares;

		move.setMovingPieceType(PieceType.KING);
		move.setBeginSquare(beginSquare);

		// Loop through all target squares
		for (BitLoop targetSquareLoop = new BitLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
			final int targetSquare = targetSquareLoop.getNextSquare();
			final int capturedPieceType = position.getPieceTypeOnSquare(targetSquare);

			// Make and process move
			move.finishNormalMove(targetSquare, capturedPieceType);

			if (!walker.processMove(move))
				return false;
		}

    	return true;
    }

	// Generates moves of knight.
	// Returns if generation should continue.
	private boolean generateKnightMoves(final long possibleTargetSquares) {
		final int onTurn = position.getOnTurn();
		final long beginSquareMask = position.getPiecesMask(onTurn, PieceType.KNIGHT);

		if (beginSquareMask == 0)
			return true;

		move.setMovingPieceType(PieceType.KNIGHT);

		// Loop through all squares with our figure
		for (BitLoop beginSquareLoop = new BitLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
			final int beginSquare = beginSquareLoop.getNextSquare();
			move.setBeginSquare(beginSquare);

			final long targetSquareMask = FigureAttackTable.getItem(PieceType.KNIGHT, beginSquare) & possibleTargetSquares;

			// Loop through all target squares
			for (BitLoop targetSquareLoop = new BitLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
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

	// Generates moves of bishop.
    // Returns if generation should continue.
    private boolean generateBishopMoves(final long possibleTargetSquares) {
    	final int onTurn = position.getOnTurn();
    	final long beginSquareMask = position.getPiecesMask(onTurn, PieceType.BISHOP);
    	
    	if (beginSquareMask == 0)
    		return true;

    	final long occupancy = position.getOccupancy();

    	move.setMovingPieceType(PieceType.BISHOP);

    	// Loop through all squares with our figure
    	for (BitLoop beginSquareLoop = new BitLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		
    		if ((FigureAttackTable.getItem(PieceType.BISHOP, beginSquare) & possibleTargetSquares) != 0) {
	    		move.setBeginSquare(beginSquare);
	    		
	    		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, beginSquare, occupancy);
	    		final long targetSquareMask = LineAttackTable.getAttackMask(diagonalIndex) & possibleTargetSquares;
	
	    		// Loop through all target squares
	    		for (BitLoop targetSquareLoop = new BitLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
	    			final int targetSquare = targetSquareLoop.getNextSquare();
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
    
	// Generates moves of rook.
    // Returns if generation should continue.
    private boolean generateRookMoves(final long possibleTargetSquares) {
    	final int onTurn = position.getOnTurn();
    	final long beginSquareMask = position.getPiecesMask(onTurn, PieceType.ROOK);
    	
    	if (beginSquareMask == 0)
    		return true;

    	final long occupancy = position.getOccupancy();

    	move.setMovingPieceType(PieceType.ROOK);

    	// Loop through all squares with our figure
    	for (BitLoop beginSquareLoop = new BitLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		
    		if ((FigureAttackTable.getItem(PieceType.ROOK, beginSquare) & possibleTargetSquares) != 0) {
	    		move.setBeginSquare(beginSquare);
	    		
	    		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, beginSquare, occupancy);
	    		final long targetSquareMask = LineAttackTable.getAttackMask(orthogonalIndex) & possibleTargetSquares;
	
	    		// Loop through all target squares
	    		for (BitLoop targetSquareLoop = new BitLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
	    			final int targetSquare = targetSquareLoop.getNextSquare();
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

	// Generates moves of queen.
    // Returns if generation should continue.
    private boolean generateQueenMoves(final long possibleTargetSquares) {
    	final int onTurn = position.getOnTurn();
    	final long beginSquareMask = position.getPiecesMask(onTurn, PieceType.QUEEN);
    	
    	if (beginSquareMask == 0)
    		return true;

    	final long occupancy = position.getOccupancy();
    	
    	move.setMovingPieceType(PieceType.QUEEN);

    	// Loop through all squares with our figure
    	for (BitLoop beginSquareLoop = new BitLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		
    		if ((FigureAttackTable.getItem(PieceType.QUEEN, beginSquare) & possibleTargetSquares) != 0) {
	    		move.setBeginSquare(beginSquare);
	    		
	    		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, beginSquare, occupancy);
	    		final long diagonalMask = LineAttackTable.getAttackMask(diagonalIndex);
	
	    		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, beginSquare, occupancy);
	    		final long orthogonalMask = LineAttackTable.getAttackMask(orthogonalIndex);
	    		
	    		final long targetSquareMask = (diagonalMask | orthogonalMask) & possibleTargetSquares;
	
	    		// Loop through all target squares
	    		for (BitLoop targetSquareLoop = new BitLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
	    			final int targetSquare = targetSquareLoop.getNextSquare();
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

    	final long moveBeginMask = (onTurn == Color.WHITE) ?
				pawnMask & BoardConstants.RANK_7_MASK & (~occupancy >>> File.COUNT) :
				pawnMask & BoardConstants.RANK_2_MASK & (~occupancy << File.COUNT);

    	move.setMovingPieceType(PieceType.PAWN);

    	// Promotion
    	for (BitLoop beginSquareLoop = new BitLoop(moveBeginMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final int targetSquare = beginSquare + BoardConstants.getPawnSquareOffset(onTurn);
    		final int capturedPieceType = position.getPieceTypeOnSquare(targetSquare);

			// Make and process move
			for (int promotionFigure = PieceType.PROMOTION_FIGURE_FIRST; promotionFigure < PieceType.PROMOTION_FIGURE_LAST; promotionFigure++) {
				move.finishPromotion(targetSquare, capturedPieceType, promotionFigure);

				if (!walker.processMove(move))
					return false;
			}
    	}
    	
    	// Captures
		final long opponentSquares = position.getColorOccupancy(oppositeColor);
		final long captureBeginMask = pawnMask & BoardConstants.getPawnsAttackedSquares (oppositeColor, opponentSquares);

		for (BitLoop beginSquareLoop = new BitLoop(captureBeginMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final long targetSquareMask = PawnAttackTable.getItem(onTurn, beginSquare) & opponentSquares;

    		// Loop through all target squares
    		for (BitLoop targetSquareLoop = new BitLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
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

		final int onTurn = position.getOnTurn();
		final int notOnTurn = Color.getOppositeColor(onTurn);

    	final long knightCheckSquares;
    	final long orthogonalCheckSquares;
    	final long diagonalCheckSquares;
    	
    	if (generateChecks) {
    		final int kingSquare = position.getKingPosition(notOnTurn);
    		final long occupancy = position.getOccupancy();
			final long opponentOrEmptySquares = ~position.getColorOccupancy(onTurn);

			knightCheckSquares = FigureAttackTable.getItem(PieceType.KNIGHT, kingSquare) & opponentOrEmptySquares;
    		
    		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, kingSquare, occupancy);
    		orthogonalCheckSquares = LineAttackTable.getAttackMask(orthogonalIndex) & opponentOrEmptySquares;

    		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, kingSquare, occupancy);
    		diagonalCheckSquares = LineAttackTable.getAttackMask(diagonalIndex) & opponentOrEmptySquares;
    	}
    	else {
    		knightCheckSquares = BitBoard.EMPTY;
    		orthogonalCheckSquares = BitBoard.EMPTY;
    		diagonalCheckSquares = BitBoard.EMPTY;
    	}

    	final long opponentSquares = position.getColorOccupancy(notOnTurn);

		final long allowedOpponentSquares = opponentSquares & allowedTargetSquares;

		if (!generateKingMoves(allowedOpponentSquares))
    		return;

    	if (!generateKnightMoves(allowedOpponentSquares | knightCheckSquares))
    		return;

    	if (!generateQueenMoves(allowedOpponentSquares | orthogonalCheckSquares | diagonalCheckSquares))
    		return;

    	if (!generateRookMoves(allowedOpponentSquares | orthogonalCheckSquares))
    		return;

    	if (!generateBishopMoves(allowedOpponentSquares | diagonalCheckSquares))
    		return;

    	if (!generatePawnMoves())
    		return;

    	generateEnPassantMoves();
    }

    public void setGenerateChecks (final boolean generate) {
    	this.generateChecks = generate;
    }

    public void setAllowedTargetSquares(final long allowedTargetSquares) {
    	this.allowedTargetSquares = allowedTargetSquares;
	}
}
