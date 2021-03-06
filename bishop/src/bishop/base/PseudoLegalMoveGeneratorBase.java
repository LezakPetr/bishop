package bishop.base;

import java.util.Arrays;

public abstract class PseudoLegalMoveGeneratorBase implements IMoveGenerator {
	
	protected Position position;
	protected IMoveWalker walker;
	protected final Move move = new Move();
	private final boolean[] pieceTypesToGenerate = new boolean[PieceType.LAST];
	
	
	public PseudoLegalMoveGeneratorBase() {
		Arrays.fill(pieceTypesToGenerate, true);
	}

	public MoveGeneratorType getGeneratorType() {
		return MoveGeneratorType.DIRECT;
	}
	
    // Generates en-passant moves.
    // Returns if generation should continue.
    protected final boolean generateEnPassantMoves() {
    	final int epFile = position.getEpFile();

    	if (epFile == File.NONE)
    		return true;

		final int onTurn = position.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);
    	final long pawnMask = position.getPiecesMask (onTurn, PieceType.PAWN);
		final int epSquare = BoardConstants.getEpSquare(oppositeColor, epFile);
		final long possibleBeginSquares = BoardConstants.getConnectedPawnSquareMask(epSquare) & pawnMask;

		move.setMovingPieceType(PieceType.PAWN);

    	for (BitLoop loop = new BitLoop(possibleBeginSquares); loop.hasNextSquare(); ) {
			final int beginSquare = loop.getNextSquare();

			move.setBeginSquare(beginSquare);
			move.finishEnPassant (BoardConstants.getEpTargetSquare(oppositeColor, epFile));

			if (!walker.processMove(move))
				return false;
    	}

    	return true;
    }

    /**
     * Sets position for move generation.
     * Given object may be used - it may or may not be copied.
     * @param position position where moves will be generated
     */
    public void setPosition(final Position position) {
    	this.position = position;
    }

    /**
     * Sets walker that will be called when move is generated.
     * @param walker walker of moves
     */
    public void setWalker (final IMoveWalker walker) {
    	this.walker = walker;
    }
    
	/**
	 * Instructs the generator to (not) generate moves of given piece.
	 * This is optional, the generator can generate moves of all pieces.
	 * @param pieceType piece type
	 * @param generate generate or not
	 */
    @Override
	public void setGenerateMovesOfPiece (final int pieceType, final boolean generate) {
		this.pieceTypesToGenerate[pieceType] = generate;
	}
	
	public boolean getGenerateMovesOfPiece (final int pieceType) {
		return pieceTypesToGenerate[pieceType];
	}

}
