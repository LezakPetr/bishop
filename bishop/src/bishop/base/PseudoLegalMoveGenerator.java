package bishop.base;

import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PawnMoveTable;

public final class PseudoLegalMoveGenerator extends PseudoLegalMoveGeneratorBase {

	public static final int MAX_MOVES_IN_POSITION = 321;
	
	private boolean reduceMovesInCheck;
	private boolean generateOnlyChecks;
	
	private int onTurn;
	private int oppositeColor;
	private int oppositeKingSquare;
	
	private long orthogonalCheckingMask;
	private long diagonalCheckingMask;
	private long indirectCheckingBlockers;
	
    // Generates moves of some figure.
    // Returns if generation should continue.
    private boolean generateShortMovingFigureMoves(final int figure, final long possibleDestinationSquares) {
    	final long beginSquareMask = position.getPiecesMask(onTurn, figure);
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(figure);

    	// Loop through all squares with our figure
    	for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final long unfilteredTargetSquareMask = FigureAttackTable.getItem(figure, beginSquare) & possibleDestinationSquares;
    		final boolean indirectCheck = BitBoard.containsSquare (indirectCheckingBlockers, beginSquare);
    		final long targetSquareMask = (indirectCheck) ? unfilteredTargetSquareMask : unfilteredTargetSquareMask & FigureAttackTable.getItem(figure, oppositeKingSquare);

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
    private boolean generateBishopMoves(final long possibleDestinationSquares) {
    	final long beginSquareMask = position.getPiecesMask(onTurn, PieceType.BISHOP);
    	final long occupancy = position.getOccupancy();
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(PieceType.BISHOP);

    	// Loop through all squares with our figure
    	for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, beginSquare, occupancy);
    		final long unfilteredTargetSquareMask = LineAttackTable.getAttackMask(diagonalIndex) & possibleDestinationSquares;
    		final boolean indirectCheck = BitBoard.containsSquare (indirectCheckingBlockers, beginSquare);
    		final long targetSquareMask = (indirectCheck) ? unfilteredTargetSquareMask : unfilteredTargetSquareMask & diagonalCheckingMask;

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
    private boolean generateRookMoves(final long possibleDestinationSquares) {
    	final long beginSquareMask = position.getPiecesMask(onTurn, PieceType.ROOK);
    	final long occupancy = position.getOccupancy();
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(PieceType.ROOK);

    	// Loop through all squares with our figure
    	for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, beginSquare, occupancy);
    		final long unfilteredTargetSquareMask = LineAttackTable.getAttackMask(orthogonalIndex) & possibleDestinationSquares;
    		final boolean indirectCheck = BitBoard.containsSquare (indirectCheckingBlockers, beginSquare);
    		final long targetSquareMask = (indirectCheck) ? unfilteredTargetSquareMask : unfilteredTargetSquareMask & orthogonalCheckingMask;

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
    private boolean generateQueenMoves(final long possibleDestinationSquares) {
    	final long beginSquareMask = position.getPiecesMask(onTurn, PieceType.QUEEN);
    	final long occupancy = position.getOccupancy();
    	
    	final BitLoop beginSquareLoop = new BitLoop();
    	final BitLoop targetSquareLoop = new BitLoop();
    	
    	move.setMovingPieceType(PieceType.QUEEN);

    	// Loop through all squares with our figure
    	for (beginSquareLoop.init(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
    		final int beginSquare = beginSquareLoop.getNextSquare();
    		move.setBeginSquare(beginSquare);
    		
    		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, beginSquare, occupancy);
    		final long diagonalMask = LineAttackTable.getAttackMask(diagonalIndex);

    		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, beginSquare, occupancy);
    		final long orthogonalMask = LineAttackTable.getAttackMask(orthogonalIndex);
    		
    		final long unfilteredTargetSquareMask = (diagonalMask | orthogonalMask) & possibleDestinationSquares;
    		final boolean indirectCheck = BitBoard.containsSquare (indirectCheckingBlockers, beginSquare);
    		final long targetSquareMask = (indirectCheck) ? unfilteredTargetSquareMask : unfilteredTargetSquareMask & (diagonalCheckingMask | orthogonalCheckingMask);
    		
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

    // Generates moves of pawns.
    // Returns if generation should continue.
    private boolean generatePawnMoves(final long possibleDestinationSquares) {
    	long beginSquareMask = position.getPiecesMask(onTurn, PieceType.PAWN);
    	
    	if (generateOnlyChecks) {
    		final long promotionRankMask = BoardConstants.getRankMask(BoardConstants.getPawnPromotionRank(onTurn));
    		final long directCheckingTargetSquares = PawnAttackTable.getItem(oppositeColor, oppositeKingSquare) | promotionRankMask;
    		final long directCheckingSourceSquares;
    		
    		if (onTurn == Color.WHITE)
    			directCheckingSourceSquares = (directCheckingTargetSquares >>> File.LAST) | ((directCheckingTargetSquares & Rank.R4) >>> (2*File.LAST));
    		else
    			directCheckingSourceSquares = (directCheckingTargetSquares << File.LAST) | ((directCheckingTargetSquares & Rank.R5) << (2*File.LAST));
    		
    		beginSquareMask &= (directCheckingSourceSquares | indirectCheckingBlockers);
    	}
    	
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

    	// If no castling is permitted skip all tests
    	if (!castlingRights.isRightForColor(onTurn))
    		return true;

    	// Check if king is in check
    	final int kingPosition = position.getKingPosition(onTurn);

    	if (position.isSquareAttacked(oppositeColor, kingPosition))
    		return true;

    	// Try both castlings
    	move.setMovingPieceType(PieceType.KING);
		move.setBeginSquare(kingPosition);

    	for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
    		if (isCastlingPossible(position, castlingType)) {
  				final int kingTargetSquare = BoardConstants.getCastlingKingTargetSquare(onTurn, castlingType);
  				move.finishCastling(kingTargetSquare);

  				if (!walker.processMove(move))
  					return false;
  	  		}
    	}

    	return true;
    }
    
    /**
     * This method checks if it is possible to do given castling in given position.
     * This method does NOT check:
     * - if the king is attacked (this is common to both castlings so it is faster to check it outside
     *     of this method)
     * - if  the king would be attacked after the move
     * @param position position
     * @param castlingType type of castling
     * @return if the castling is possible
     */
    public static boolean isCastlingPossible(final Position position, final int castlingType) {
    	final CastlingRights castlingRights = position.getCastlingRights();
    	final long occupancy = position.getOccupancy();
    	final int onTurn = position.getOnTurn();

  		if (castlingRights.isRight(onTurn, castlingType) && (occupancy & BoardConstants.getCastlingMiddleSquareMask(onTurn, castlingType)) == 0) {
  			// We should test square that king goes across. This is same square as rook destination square.
  			final int testSquare = BoardConstants.getCastlingKingMiddleSquare(onTurn, castlingType);
  			final int oppositeColor = Color.getOppositeColor(onTurn);
  			
  			if (!position.isSquareAttacked (oppositeColor, testSquare))
  				return true;
  		}
  		
  		return false;    	
    }
    
    /**
     * Setting to true means that the generator will not generate moves that cannot stop check. 
     * @param reduce true in case that moves will be reduced in case of check
     */
    public void setReduceMovesInCheck(final boolean reduce) {
    	this.reduceMovesInCheck = reduce;
    }
    
    /**
     * Setting to true means that the generator doesn't have to generate moves that are not checks.
     * So the generator generates all the checking moves and (possibly) some non-checking ones. 
     */
    public void setGenerateOnlyChecks(final boolean onlyChecks) {
    	this.generateOnlyChecks = onlyChecks;
    }

    
    private long calculatePossibleTargetSquares() {
    	final long notOwnSquares = ~position.getColorOccupancy(onTurn);

    	if (!reduceMovesInCheck)
    		return notOwnSquares;

    	final int notOnTurn = Color.getOppositeColor(onTurn);
    	final int kingSquare = position.getKingPosition(onTurn);
    	final long checkingPieces = position.getAttackingPieces(notOnTurn, kingSquare);
    	
    	if (checkingPieces == BitBoard.EMPTY)
    		return notOwnSquares;   // No check
    	
		final int checkCount = BitBoard.getSquareCount(checkingPieces);
		
		if (checkCount == 1) {
			final int checkingSquare = BitBoard.getFirstSquare(checkingPieces);
			
			return (checkingPieces | BetweenTable.getItem(kingSquare, checkingSquare)) & notOwnSquares;
		}
		else
			return BitBoard.EMPTY;   // Double check => king must move
    }
    
    private void updateCheckingMasksOnlyChecks() {
    	final int kingSquare = position.getKingPosition(oppositeColor);
    	
    	// Direct check
		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, kingSquare, position.getOccupancy());
		diagonalCheckingMask = LineAttackTable.getAttackMask(diagonalIndex);

		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, kingSquare, position.getOccupancy());
		orthogonalCheckingMask = LineAttackTable.getAttackMask(orthogonalIndex);
		
		// Indirect check
		final long orthogonalFullMask = FigureAttackTable.getItem(PieceType.ROOK, kingSquare);
		final long diagonalFullMask = FigureAttackTable.getItem(PieceType.BISHOP, kingSquare);
		
		final long rookMask = position.getBothColorPiecesMask(PieceType.ROOK);
		final long bishopMask = position.getBothColorPiecesMask(PieceType.BISHOP);
		final long queenMask = position.getBothColorPiecesMask(PieceType.QUEEN);
		final long ownPieces = position.getColorOccupancy(onTurn);
		
		final long potentialIndirectMask = ownPieces & (
				orthogonalFullMask & (rookMask | queenMask) |
				diagonalFullMask & (bishopMask | queenMask)
		);
		
		long blockers = BitBoard.EMPTY;
		
		for (BitLoop loop = new BitLoop(potentialIndirectMask); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			final long betweenMask = BetweenTable.getItem(kingSquare, square) & position.getOccupancy();
			
			if (BitBoard.getSquareCount(betweenMask) == 1)
				blockers |= betweenMask;
		}
		
		indirectCheckingBlockers = blockers & ownPieces;
    }
    
    private void updateCheckingMasksAllMoves() {
    	diagonalCheckingMask = BitBoard.FULL;
    	orthogonalCheckingMask = BitBoard.FULL;
    	indirectCheckingBlockers = BitBoard.FULL;
    }

    /**
     * Generates all legal moves in the position.
     * Calls walker for each generated move.
     */
    public void generateMoves() {
    	onTurn = position.getOnTurn();
    	oppositeColor = Color.getOppositeColor(onTurn);

    	final int castlingRightIndex = position.getCastlingRights().getIndex();
    	final int epFile = position.getEpFile();
    	final long notOwnSquares = ~position.getColorOccupancy(onTurn);

    	move.initialize(castlingRightIndex, epFile);

    	if (generateOnlyChecks)
    		updateCheckingMasksOnlyChecks();
    	else
    		updateCheckingMasksAllMoves();

    	final long possibleDestinationSquares = calculatePossibleTargetSquares();
    	    	
    	// Generate figure moves, starting with king - this speeds up legal move checking
    	if (!generateShortMovingFigureMoves(PieceType.KING, notOwnSquares))
    		return;
    	
    	if (!generateShortMovingFigureMoves(PieceType.KNIGHT, possibleDestinationSquares))
    		return;
    	
    	if (!generateQueenMoves(possibleDestinationSquares))
    		return;
    	
    	if (!generateRookMoves(possibleDestinationSquares))
    		return;
    	
    	if (!generateBishopMoves(possibleDestinationSquares))
    		return;
    	
    	if (!generatePawnMoves(possibleDestinationSquares))
    		return;

    	if (!generateCastlingMoves())
    		return;

    	generateEnPassantMoves();
    }
}
