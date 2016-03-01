package bishop.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import bishop.engine.Evaluation;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PieceHashTable;

/**
 * Representation of chess position with additional information that affects
 * generation of moves. 
 * @author Ing. Petr Ležák
 */
public final class Position implements IPosition {

	// Primary piece information - the source of truth
	private final byte[] pieceTypeOnSquares = new byte[Square.LAST];
	private final long[] colorOccupancy = new long[Color.LAST];   // Which squares are occupied by some piece with given color
	
	// Cached piece information
	private final long[] pieces = new long[PieceType.LAST * Color.LAST];   // Which squares are occupied by given pieces
	private long occupancy;   // Which squares are occupied by some piece

	private int onTurn;   // Color of player on turn
	private final CastlingRights castlingRights = new CastlingRights();   // Rights for castling
	private int epFile;   // File where pawn advanced by two squares in last move (or File.NONE)

	private IPositionCaching caching;
	
	public static final Position INITIAL_POSITION = createInitialPosition();
	
	/**
	 * Default constructor - creates empty position.
	 */
	public Position() {
		this(false);
	}
	
	public Position(final boolean nullCaching) {
		if (nullCaching)
			caching = new NullPositionCaching(this);
		else
			caching = new PositionCachingImpl();
		
		clearPosition();
	}
	
	/**
	 * Returns index into array 'pieces'.
	 * @param color color of piece
	 * @param pieceType type of piece
	 * @return index
	 */
	private static int getPieceMaskIndex (final int color, final int pieceType) {
		return (pieceType << 1) + color;
	}

	/**
	 * Makes given normal move.
	 * @param move move
	 */
	private void makeNormalMove (final Move move) {
		final int movingPieceType = move.getMovingPieceType();

		final int beginSquare = move.getBeginSquare();
		final long beginSquareMask = BitBoard.getSquareMask(beginSquare);

		final int targetSquare = move.getTargetSquare();
		final long targetSquareMask = BitBoard.getSquareMask(targetSquare);

		final int capturedPieceType = move.getCapturedPieceType();
		final int oppositeColor = Color.getOppositeColor (onTurn);
		
		final long movingPieceChange = beginSquareMask | targetSquareMask;
		
		pieceTypeOnSquares[beginSquare] = PieceType.NONE;
		pieceTypeOnSquares[targetSquare] = (byte) movingPieceType;
		
		pieces[getPieceMaskIndex(onTurn, movingPieceType)] ^= movingPieceChange;
		colorOccupancy[onTurn] ^= movingPieceChange;
		occupancy &= ~beginSquareMask;
		
		caching.movePiece (onTurn, movingPieceType, beginSquare, targetSquare);

		if (capturedPieceType != PieceType.NONE) {
			pieces[getPieceMaskIndex (oppositeColor, capturedPieceType)] &= ~targetSquareMask;
			colorOccupancy[oppositeColor] &= ~targetSquareMask;
			
			caching.removePiece (oppositeColor, capturedPieceType, targetSquare);
		}
		else
			occupancy |= targetSquareMask;

		// Update castling rights
		if (!castlingRights.isEmpty() && (movingPieceChange & CastlingRights.AFFECTED_SQUARES) != 0) {
			final int origCastlingRightIndex = castlingRights.getIndex();
			
			castlingRights.updateAfterSquareChange (beginSquare);
			castlingRights.updateAfterSquareChange (targetSquare);
			
			caching.changeCastlingRights (origCastlingRightIndex, castlingRights.getIndex());
		}

		// On turn
		onTurn = oppositeColor;
		caching.swapOnTurn();

		// Update EP file
		final int origEpFile = epFile;
				
		if (movingPieceType == PieceType.PAWN && Math.abs(targetSquare - beginSquare) == 2 * File.LAST) {
			epFile = Square.getFile(beginSquare);   // Needed by method isEnPassantPossible
			
			if (!isEnPassantPossible())
				epFile = File.NONE;
		}
		else
			epFile = File.NONE;
		
		caching.changeEpFile (origEpFile, epFile);
	}

	/**
	 * Makes given normal move.
	 * @param move move
	 */
	private void undoNormalMove (final Move move) {
		final int movingPieceType = move.getMovingPieceType();

		final int beginSquare = move.getBeginSquare();
		final long beginSquareMask = BitBoard.getSquareMask(beginSquare);

		final int targetSquare = move.getTargetSquare();
		final long targetSquareMask = BitBoard.getSquareMask(targetSquare);

		final int capturedPieceType = move.getCapturedPieceType();
		final int oppositeColor = onTurn;
		
		// On turn
		onTurn = Color.getOppositeColor (oppositeColor);
		caching.swapOnTurn();
		
		// Piece masks
		final long movingPieceChange = beginSquareMask | targetSquareMask;
		
		pieceTypeOnSquares[beginSquare] = (byte) movingPieceType;
		pieceTypeOnSquares[targetSquare] = (byte) capturedPieceType;
		
		pieces[getPieceMaskIndex (onTurn, movingPieceType)] ^= movingPieceChange;
		colorOccupancy[onTurn] ^= movingPieceChange;
		occupancy |= beginSquareMask;
		
		caching.movePiece(onTurn, movingPieceType, targetSquare, beginSquare);

		if (capturedPieceType != PieceType.NONE) {
			pieces[getPieceMaskIndex (oppositeColor, capturedPieceType)] |= targetSquareMask;
			colorOccupancy[oppositeColor] |= targetSquareMask;
			
			caching.addPiece(oppositeColor, capturedPieceType, targetSquare);
		}
		else
			occupancy &= ~targetSquareMask;

		// Update castling rights
		final int prevCastlingRightIndex = move.getPreviousCastlingRigthIndex();
		
		if (prevCastlingRightIndex != castlingRights.getIndex()) {
			caching.changeCastlingRights(castlingRights.getIndex(), prevCastlingRightIndex);
			castlingRights.setIndex (prevCastlingRightIndex);
		}

		// Update EP file
		final int prevEpFile = move.getPreviousEpFile();
		
		if (prevEpFile != epFile) {
			caching.changeEpFile(epFile, prevEpFile);
			epFile = prevEpFile;
		}
	}

	/**
	 * Makes given promotion move.
	 * @param move move
	 */
	private void makePromotionMove (final Move move) {
		final int beginSquare = move.getBeginSquare();
		final long beginSquareMask = BitBoard.getSquareMask(beginSquare);

		final int targetSquare = move.getTargetSquare();
		final long targetSquareMask = BitBoard.getSquareMask(targetSquare);

		final int capturedPieceType = move.getCapturedPieceType();
		final int promotionPieceType = move.getPromotionPieceType();
		final int oppositeColor = Color.getOppositeColor (onTurn);
		
		pieceTypeOnSquares[beginSquare] = PieceType.NONE;
		pieceTypeOnSquares[targetSquare] = (byte) promotionPieceType;

		pieces[getPieceMaskIndex (onTurn, PieceType.PAWN)] &= ~beginSquareMask;
		colorOccupancy[onTurn] &= ~beginSquareMask;
		occupancy &= ~beginSquareMask;

		pieces[getPieceMaskIndex (onTurn, promotionPieceType)] |= targetSquareMask;
		colorOccupancy[onTurn] |= targetSquareMask;
		
		caching.removePiece(onTurn, PieceType.PAWN, beginSquare);
		caching.addPiece(onTurn, promotionPieceType, targetSquare);
		
		if (capturedPieceType != PieceType.NONE) {
			pieces[getPieceMaskIndex (oppositeColor, capturedPieceType)] &= ~targetSquareMask;
			colorOccupancy[oppositeColor] &= ~targetSquareMask;

			caching.removePiece(oppositeColor, capturedPieceType, targetSquare);
		}
		else
			occupancy |= targetSquareMask;

		// Update castling rights - only by target square (begin square cannot change rights)
		final int origCastlingRightIndex = castlingRights.getIndex();
		castlingRights.updateAfterSquareChange (targetSquare);
		caching.changeCastlingRights(origCastlingRightIndex, castlingRights.getIndex());
		
		caching.changeEpFile(epFile, File.NONE);
		epFile = File.NONE;
		
		onTurn = oppositeColor;
		caching.swapOnTurn();
	}

	/**
	 * Undos given promotion move.
	 * @param move move
	 */
	private void undoPromotionMove (final Move move) {
		final int beginSquare = move.getBeginSquare();
		final long beginSquareMask = BitBoard.getSquareMask(beginSquare);

		final int targetSquare = move.getTargetSquare();
		final long targetSquareMask = BitBoard.getSquareMask(targetSquare);

		final int capturedPieceType = move.getCapturedPieceType();
		final int promotionPieceType = move.getPromotionPieceType();
		final int oppositeColor = onTurn;
		
		// On turn
		onTurn = Color.getOppositeColor (oppositeColor);
		caching.swapOnTurn();

		// Piece masks
		pieceTypeOnSquares[beginSquare] = PieceType.PAWN;
		pieceTypeOnSquares[targetSquare] = (byte) capturedPieceType;

		pieces[getPieceMaskIndex (onTurn, PieceType.PAWN)] |= beginSquareMask;
		colorOccupancy[onTurn] |= beginSquareMask;
		occupancy |= beginSquareMask;

		pieces[getPieceMaskIndex (onTurn, promotionPieceType)] &= ~targetSquareMask;
		colorOccupancy[onTurn] &= ~targetSquareMask;

		caching.addPiece(onTurn, PieceType.PAWN, beginSquare);
		caching.removePiece(onTurn, promotionPieceType, targetSquare);

		if (capturedPieceType != PieceType.NONE) {
			pieces[getPieceMaskIndex (oppositeColor, capturedPieceType)] |= targetSquareMask;
			colorOccupancy[oppositeColor] |= targetSquareMask;
			
			caching.addPiece(oppositeColor, capturedPieceType, targetSquare);
		}
		else
			occupancy &= ~targetSquareMask;

		// Update castling rights
		final int prevCastlingRightIndex = move.getPreviousCastlingRigthIndex();
		
		if (prevCastlingRightIndex != castlingRights.getIndex()) {
			caching.changeCastlingRights(castlingRights.getIndex(), prevCastlingRightIndex);
			castlingRights.setIndex (prevCastlingRightIndex);
		}
		
		// EP file
		epFile = move.getPreviousEpFile();
		caching.changeEpFile(File.NONE, epFile);
	}

	/**
	 * Makes given castling move.
	 * @param move move
	 */
	private void makeCastlingMove (final Move move) {
		final int beginSquare = move.getBeginSquare();
		final int targetSquare = move.getTargetSquare();
		final int oppositeColor = Color.getOppositeColor (onTurn);

		final int castlingType = (beginSquare > targetSquare) ? CastlingType.LONG : CastlingType.SHORT;

		final long kingChanges = BoardConstants.getCastlingKingChangeMask(onTurn, castlingType);
		final long rookChanges = BoardConstants.getCastlingRookChangeMask(onTurn, castlingType);
		final long occupancyChanges = kingChanges ^ rookChanges;
		
		final int rookBeginSquare = BoardConstants.getCastlingRookBeginSquare(onTurn, castlingType);
		final int rookTargetSquare = BoardConstants.getCastlingRookTargetSquare(onTurn, castlingType);
		
		pieceTypeOnSquares[beginSquare] = PieceType.NONE;
		pieceTypeOnSquares[targetSquare] = PieceType.KING;
		pieceTypeOnSquares[rookBeginSquare] = PieceType.NONE;
		pieceTypeOnSquares[rookTargetSquare] = PieceType.ROOK;

		caching.movePiece(onTurn, PieceType.KING, beginSquare, targetSquare);
		caching.movePiece(onTurn, PieceType.ROOK, rookBeginSquare, rookTargetSquare);

		pieces[getPieceMaskIndex (onTurn, PieceType.KING)] ^= kingChanges;
		pieces[getPieceMaskIndex (onTurn, PieceType.ROOK)] ^= rookChanges;
		colorOccupancy[onTurn] ^= occupancyChanges;
		occupancy ^= occupancyChanges;

		// Castling rights
		final int origCastlingRightIndex = castlingRights.getIndex();
		castlingRights.dropRightsForColor (onTurn);
		caching.changeCastlingRights(origCastlingRightIndex, castlingRights.getIndex());

		// EP file
		caching.changeEpFile(epFile, File.NONE);
		epFile = File.NONE;
		
		// On turn
		onTurn = oppositeColor;
		caching.swapOnTurn();
	}

	/**
	 * Undo given castling move.
	 * @param move move
	 */
	private void undoCastlingMove (final Move move) {
		final int beginSquare = move.getBeginSquare();
		final int targetSquare = move.getTargetSquare();
		final int oppositeColor = onTurn;
		
		// On turn
		onTurn = Color.getOppositeColor (oppositeColor);
		caching.swapOnTurn();

		// Piece masks
		final int castlingType = (beginSquare > targetSquare) ? CastlingType.LONG : CastlingType.SHORT;

		final long kingChanges = BoardConstants.getCastlingKingChangeMask(onTurn, castlingType);
		final long rookChanges = BoardConstants.getCastlingRookChangeMask(onTurn, castlingType);
		final long occupancyChanges = kingChanges ^ rookChanges;
		
		final int rookBeginSquare = BoardConstants.getCastlingRookBeginSquare(onTurn, castlingType);
		final int rookTargetSquare = BoardConstants.getCastlingRookTargetSquare(onTurn, castlingType);
		
		pieceTypeOnSquares[beginSquare] = PieceType.KING;
		pieceTypeOnSquares[targetSquare] = PieceType.NONE;
		pieceTypeOnSquares[rookBeginSquare] = PieceType.ROOK;
		pieceTypeOnSquares[rookTargetSquare] = PieceType.NONE;

		caching.movePiece(onTurn, PieceType.KING, targetSquare, beginSquare);
		caching.movePiece(onTurn, PieceType.ROOK, rookTargetSquare, rookBeginSquare);

		pieces[getPieceMaskIndex (onTurn, PieceType.KING)] ^= kingChanges;
		pieces[getPieceMaskIndex (onTurn, PieceType.ROOK)] ^= rookChanges;
		colorOccupancy[onTurn] ^= occupancyChanges;
		occupancy ^= occupancyChanges;

		// Update castling rights
		final int prevCastlingRightIndex = move.getPreviousCastlingRigthIndex();
		caching.changeCastlingRights(prevCastlingRightIndex, castlingRights.getIndex());
		castlingRights.setIndex (prevCastlingRightIndex);
		
		// EP file
		epFile = move.getPreviousEpFile();		
		caching.changeEpFile(File.NONE, epFile);
	}

	/**
	 * Makes given en-passant move.
	 * @param move move
	 */
	private void makeEnPassantMove (final Move move) {
		final int beginSquare = move.getBeginSquare();
		final long beginSquareMask = BitBoard.getSquareMask(beginSquare);

		final int targetSquare = move.getTargetSquare();
		final long targetSquareMask = BitBoard.getSquareMask(targetSquare);

		final int oppositeColor = Color.getOppositeColor (onTurn);
		final int epSquare = Square.onFileRank(epFile, Square.getRank (beginSquare));
		final long epSquareMask = BitBoard.getSquareMask(epSquare);
		
		pieceTypeOnSquares[beginSquare] = PieceType.NONE;
		pieceTypeOnSquares[epSquare] = PieceType.NONE;
		pieceTypeOnSquares[targetSquare] = PieceType.PAWN;

		pieces[getPieceMaskIndex (onTurn, PieceType.PAWN)] &= ~beginSquareMask;
		colorOccupancy[onTurn] &= ~beginSquareMask;
		occupancy &= ~beginSquareMask;

		pieces[getPieceMaskIndex (onTurn, PieceType.PAWN)] |= targetSquareMask;
		colorOccupancy[onTurn] |= targetSquareMask;
		occupancy |= targetSquareMask;

		pieces[getPieceMaskIndex (oppositeColor, PieceType.PAWN)] &= ~epSquareMask;
		colorOccupancy[oppositeColor] &= ~epSquareMask;
		occupancy &= ~epSquareMask;
		
		caching.movePiece(onTurn, PieceType.PAWN, beginSquare, targetSquare);
		caching.removePiece(oppositeColor, PieceType.PAWN, epSquare);
		
		// EP file
		caching.changeEpFile(epFile, File.NONE);
		epFile = File.NONE;
		
		// On turn
		onTurn = oppositeColor;
		caching.swapOnTurn();
	}

	/**
	 * Undos given en-passant move.
	 * @param move move
	 */
	private void undoEnPassantMove (final Move move) {
		final int beginSquare = move.getBeginSquare();
		final long beginSquareMask = BitBoard.getSquareMask(beginSquare);

		final int targetSquare = move.getTargetSquare();
		final long targetSquareMask = BitBoard.getSquareMask(targetSquare);
		final int oppositeColor = onTurn;
		
		// On turn
		onTurn = Color.getOppositeColor (oppositeColor);
		caching.swapOnTurn();
		
		// EP file
		epFile = move.getPreviousEpFile();
		caching.changeEpFile(File.NONE, epFile);
		
		final int epSquare = Square.onFileRank(epFile, Square.getRank (beginSquare));
		final long epSquareMask = BitBoard.getSquareMask(epSquare);

		// Piece masks
		pieceTypeOnSquares[beginSquare] = PieceType.PAWN;
		pieceTypeOnSquares[epSquare] = PieceType.PAWN;
		pieceTypeOnSquares[targetSquare] = PieceType.NONE;

		pieces[getPieceMaskIndex (onTurn, PieceType.PAWN)] |= beginSquareMask;
		colorOccupancy[onTurn] |= beginSquareMask;
		occupancy |= beginSquareMask;

		pieces[getPieceMaskIndex (onTurn, PieceType.PAWN)] &= ~targetSquareMask;
		colorOccupancy[onTurn] &= ~targetSquareMask;
		occupancy &= ~targetSquareMask;

		pieces[getPieceMaskIndex (oppositeColor, PieceType.PAWN)] |= epSquareMask;
		colorOccupancy[oppositeColor] |= epSquareMask;
		occupancy |= epSquareMask;
		
		caching.movePiece(onTurn, PieceType.PAWN, targetSquare, beginSquare);
		caching.addPiece(oppositeColor, PieceType.PAWN, epSquare);
	}
	
	/**
	 * Makes given null move.
	 * @param move move
	 */
	private void makeNullMove (final Move move) {
		// EP file
		caching.changeEpFile(epFile, File.NONE);
		epFile = File.NONE;
		
		// On turn
		onTurn = Color.getOppositeColor(onTurn);
		caching.swapOnTurn();
	}

	/**
	 * Makes given null move.
	 * @param move move
	 */
	private void undoNullMove (final Move move) {
		// On turn
		onTurn = Color.getOppositeColor (onTurn);
		caching.swapOnTurn();
		
		// EP file
		epFile = move.getPreviousEpFile();
		caching.changeEpFile(File.NONE, epFile);
	}

	/**
	 * Returns color of player on turn.
	 * @return color of player on turn
	 */
	public int getOnTurn() {
		return onTurn;
	}

	/**
	 * Sets color of player on turn.
	 * @param onTurn color of player on turn
	 */
	public void setOnTurn (final int onTurn) {
		if (!Color.isValid(onTurn))
			throw new RuntimeException("Invalid color given as onTurn");

		this.onTurn = onTurn;
	}

	/**
	 * Returns EP file.
	 * @return file where pawn was advanced by two squares in last move (or File.NONE) 
	 */
	public int getEpFile() {
		return epFile;
	}

	/**
	 * Sets EP file.
	 * @param file file where pawn was advanced by two squares in last move (or File.NONE)
	 */
	public void setEpFile (final int file) {
		if (!File.isValid (file) && file != File.NONE)
			throw new RuntimeException("Invalid file given as epFile");

		this.epFile = file;
	}

	/**
	 * Returns piece on given square.
	 * If square if empty method returns null.
	 * @param square coordinate of square
	 * @return piece on given square or null
	 */
	public Piece getSquareContent (final int square) {
		final long squareMask = BitBoard.getSquareMask(square);

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			if ((colorOccupancy[color] & squareMask) != 0) {
				final int pieceType = getPieceTypeOnSquare(square);
				
				return Piece.withColorAndType(color, pieceType);
			}
		}
		
		return null;
	}

	/**
	 * Puts given piece to given square.
	 * @param square target square
	 * @param piece piece or null if square should be empty
	 */
	public void setSquareContent (final int square, final Piece piece) {
		final long squareMask = BitBoard.getSquareMask(square);
		
		if (piece != null) {
			pieceTypeOnSquares[square] = (byte) piece.getPieceType();
			
			final int color = piece.getColor();
			final int oppositeColor = Color.getOppositeColor(color);
			
			colorOccupancy[color] |= squareMask;
			colorOccupancy[oppositeColor] &= ~squareMask;
		}
		else {
			pieceTypeOnSquares[square] = PieceType.NONE;
			
			for (int color = Color.FIRST; color < Color.LAST; color++)
				colorOccupancy[color] &= ~squareMask;
		}
	}
	
	/**
	 * Puts given piece to given squares.
	 * @param mask target squares
	 * @param piece piece or null if square should be empty
	 */
	public void setMoreSquaresContent (final long mask, final Piece piece) {
		for (BitLoop loop = new BitLoop(mask); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			
			setSquareContent(square, piece);
		}
	}
	
	/**
	 * Makes the position empty and white on turn.
	 */
	public void clearPosition() {
		occupancy = 0;
		Arrays.fill(colorOccupancy, 0);
		Arrays.fill(pieces, 0);
		Arrays.fill(pieceTypeOnSquares, (byte) PieceType.NONE);

		onTurn = Color.WHITE;
		castlingRights.clearRights();
		epFile = File.NONE;
	}
	
	private void setInitialPiecesForColor (final int color, final int firstRank, final int secondRank) {
		// Figures
		setSquareContent(Square.onFileRank(File.FA, firstRank), Piece.withColorAndType(color, PieceType.ROOK));
		setSquareContent(Square.onFileRank(File.FB, firstRank), Piece.withColorAndType(color, PieceType.KNIGHT));
		setSquareContent(Square.onFileRank(File.FC, firstRank), Piece.withColorAndType(color, PieceType.BISHOP));
		setSquareContent(Square.onFileRank(File.FD, firstRank), Piece.withColorAndType(color, PieceType.QUEEN));
		setSquareContent(Square.onFileRank(File.FE, firstRank), Piece.withColorAndType(color, PieceType.KING));
		setSquareContent(Square.onFileRank(File.FF, firstRank), Piece.withColorAndType(color, PieceType.BISHOP));
		setSquareContent(Square.onFileRank(File.FG, firstRank), Piece.withColorAndType(color, PieceType.KNIGHT));
		setSquareContent(Square.onFileRank(File.FH, firstRank), Piece.withColorAndType(color, PieceType.ROOK));
		
		// Pawns
		final Piece pawn = Piece.withColorAndType(color, PieceType.PAWN);
		
		for (int file = File.FIRST; file < File.LAST; file++)
			setSquareContent(Square.onFileRank(file, secondRank), pawn);
	}
	
	public void setInitialPosition() {
		clearPosition(); 
		
		onTurn = Color.WHITE;
		castlingRights.setFullRights();
		epFile = File.NONE;
		
		setInitialPiecesForColor(Color.WHITE, Rank.R1, Rank.R2);
		setInitialPiecesForColor(Color.BLACK, Rank.R8, Rank.R7);
		
		refreshCachedData();
	}

	/**
	 * Returns mask of squares occupied by given piece.
	 * @param color color of piece
	 * @param type type of piece
	 * @return mask of squares
	 */
	public long getPiecesMask (final int color, final int type) {
		return pieces[getPieceMaskIndex (color, type)];
	}

	/**
	 * Returns mask of given piece regardless of their color.
	 * @param type piece type
	 * @return mask of given piece type
	 */
	public long getBothColorPiecesMask(final int type) {
		return getPiecesMask(Color.WHITE, type) | getPiecesMask(Color.BLACK, type);
	}
	
	/**
	 * Returns mask of all occupied squares.
	 * @return mask of squares where is some piece 
	 */
	public long getOccupancy() {
		return occupancy;
	}

	/**
	 * Returns mask of squares occupied by some piece with given color.
	 * @param color color of piece
	 * @return mask of squares occupied by some piece with given color
	 */
	public long getColorOccupancy (final int color) {
		return colorOccupancy[color];
	}

	/**
	 * Returns castling rights.
	 * @return castling rights, should not be modified
	 */
	public CastlingRights getCastlingRights() {
		return castlingRights;
	}

	/**
	 * Sets castling rights.
	 * @param rights
	 */
	public void setCastlingRights (final CastlingRights rights) {
		castlingRights.assign (rights);
	}

	/**
	 * Checks if given square is attacked by some piece with given color.
	 * @param color color of attacking pieces
	 * @param square checked square
	 * @return true if square is attacked, false if not
	 */
	public boolean isSquareAttacked (final int color, final int square) {
		// Short move figures
		if ((pieces[getPieceMaskIndex (color, PieceType.KING)] & FigureAttackTable.getItem (PieceType.KING, square)) != 0)
			return true;

		if ((pieces[getPieceMaskIndex (color, PieceType.KNIGHT)] & FigureAttackTable.getItem (PieceType.KNIGHT, square)) != 0)
			return true;

		// Pawn
		final int oppositeColor = Color.getOppositeColor(color);

		if ((pieces[getPieceMaskIndex (color, PieceType.PAWN)] & PawnAttackTable.getItem(oppositeColor, square)) != 0)
			return true;

		// Long move figures
		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, square, occupancy);
		final long orthogonalMask = LineAttackTable.getAttackMask(orthogonalIndex);
		
		if ((pieces[getPieceMaskIndex (color, PieceType.ROOK)] & orthogonalMask) != 0)
			return true;

		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, square, occupancy);
		final long diagonalMask = LineAttackTable.getAttackMask(diagonalIndex);

		if ((pieces[getPieceMaskIndex (color, PieceType.BISHOP)] & diagonalMask) != 0)
			return true;
		
		final long queenMask = orthogonalMask | diagonalMask;
		
		if ((pieces[getPieceMaskIndex (color, PieceType.QUEEN)] & queenMask) != 0)
			return true;
				
		return false;
	}

	/**
	 * Returns mask of pieces with given color that attacks given square.
	 * @param color color of attacking pieces
	 * @param square checked square
	 * @return mask of attacking pieces
	 */
	public long getAttackingPieces (final int color, final int square) {
		long attackingPieceMask = BitBoard.EMPTY;
		
		// Short move figures
		attackingPieceMask |= pieces[getPieceMaskIndex (color, PieceType.KING)] & FigureAttackTable.getItem (PieceType.KING, square);
		attackingPieceMask |= pieces[getPieceMaskIndex (color, PieceType.KNIGHT)] & FigureAttackTable.getItem (PieceType.KNIGHT, square);

		// Pawn
		final int oppositeColor = Color.getOppositeColor(color);
		attackingPieceMask |= pieces[getPieceMaskIndex (color, PieceType.PAWN)] & PawnAttackTable.getItem(oppositeColor, square);

		// Long move figures
		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, square, occupancy);
		final long orthogonalMask = LineAttackTable.getAttackMask(orthogonalIndex);
		
		attackingPieceMask |= pieces[getPieceMaskIndex (color, PieceType.ROOK)] & orthogonalMask;

		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, square, occupancy);
		final long diagonalMask = LineAttackTable.getAttackMask(diagonalIndex);

		attackingPieceMask |= pieces[getPieceMaskIndex (color, PieceType.BISHOP)] & diagonalMask;
		
		final long queenMask = orthogonalMask | diagonalMask;
		attackingPieceMask |= pieces[getPieceMaskIndex (color, PieceType.QUEEN)] & queenMask;
				
		return attackingPieceMask;
	}
	
	/**
	 * Returns count of pieces with given color that attacks given square.
	 * @param color color of attacking pieces
	 * @param square checked square
	 * @return count of attacking pieces
	 */
	public int getCountOfAttacks (final int color, final int square) {
		final long attackingPieceMask = getAttackingPieces(color, square);
				
		return BitBoard.getSquareCount(attackingPieceMask);
	}

	/**
	 * Finds type of piece on given square and returns it.
	 * @param square searched square
	 * @return type of piece on given square or PieceType.NONE
	 */
	public int getPieceTypeOnSquare (final int square) {
		return pieceTypeOnSquares[square];
	}

	/**
	 * Makes given move.
	 * @param move move
	 */
	public void makeMove (final Move move) {
		checkIntegrity();
		switch (move.getMoveType()) {
			case MoveType.NORMAL:
				makeNormalMove (move);
				break;
	
			case MoveType.PROMOTION:
				makePromotionMove (move);
				break;
	
			case MoveType.CASTLING:
				makeCastlingMove (move);
				break;
	
			case MoveType.EN_PASSANT:
				makeEnPassantMove (move);
				break;
				
			case MoveType.NULL:
				makeNullMove (move);
				break;

			default:
				// Bad move type
				throw new RuntimeException("Bad type of move");
		}
		
		checkIntegrity();
	}

	/**
	 * Undos given move.
	 * @param move move
	 */
	public void undoMove (final Move move) {
		checkIntegrity();
		switch (move.getMoveType()) {
			case MoveType.NORMAL:
				undoNormalMove (move);
				break;
	
			case MoveType.PROMOTION:
				undoPromotionMove (move);
				break;
	
			case MoveType.CASTLING:
				undoCastlingMove (move);
				break;
	
			case MoveType.EN_PASSANT:
				undoEnPassantMove (move);
				break;

			case MoveType.NULL:
				undoNullMove (move);
				break;

			default:
				// Bad move type
				throw new RuntimeException("Bad type of move");
		}
		checkIntegrity();
	}
	
	/**
	 * Returns position of king with given color.
	 * @param color color of king
	 * @return square where is king with given color
	 */
	public int getKingPosition (final int color) {
		final long kingMask = pieces[getPieceMaskIndex(color, PieceType.KING)];
				
		return BitBoard.getFirstSquare(kingMask);
	}

	/**
	 * This method updates cached data.
	 * It must be called after manual changing of position (other than by makeMove).
	 */
	public void refreshCachedData() {
		updatePiecesMasks();
		updateHash();
		updateMaterialEvaluation();
	}

	private void updatePiecesMasks() {
		occupancy = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			occupancy |= colorOccupancy[color];
			
		Arrays.fill(pieces, 0);
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final long squareMask = BitBoard.getSquareMask(square);
			final int pieceType = pieceTypeOnSquares[square];
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				if ((colorOccupancy[color] & squareMask) != 0) {
					pieces[getPieceMaskIndex(color, pieceType)] |= squareMask;
				}
			}
		}
	}
	
	public long calculateHash() {
		long hash = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				for (BitLoop loop = new BitLoop(pieces[getPieceMaskIndex (color, pieceType)]); loop.hasNextSquare(); ) {
					final int square = loop.getNextSquare();
					
					hash ^= PieceHashTable.getItem(color, pieceType, square);					
				}
			}
		}
		
		hash ^= HashConstants.getOnTurnHash(onTurn);
		hash ^= HashConstants.getEpFileHash(epFile);
		hash ^= HashConstants.getCastlingRightHash(castlingRights.getIndex());
		
		return hash;
	}
	
	private void updateHash() {
		caching.setHash (calculateHash());
	}
	
	public int calculateMaterialEvaluation() {
		int materialEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
				final long piecesMask = getPiecesMask(color, pieceType);
				
				materialEvaluation += BitBoard.getSquareCount (piecesMask) * PieceTypeEvaluations.getPieceEvaluation (color, pieceType);
			}
		}
		
		return materialEvaluation;
	}
			
	private void updateMaterialEvaluation() {
		caching.setMaterialEvaluation (calculateMaterialEvaluation());
	}

	/**
	 * Assigns given original position into this.
	 * @param orig original position
	 */
	public void assign (final Position orig) {
		System.arraycopy(orig.pieceTypeOnSquares, 0, this.pieceTypeOnSquares, 0, Square.LAST);
		System.arraycopy(orig.pieces, 0, this.pieces, 0, orig.pieces.length);
		System.arraycopy(orig.colorOccupancy, 0, this.colorOccupancy, 0, Color.LAST);
		this.occupancy = orig.occupancy;

		this.onTurn = orig.onTurn;
		this.castlingRights.assign (orig.castlingRights);
		this.epFile = orig.epFile;
		this.caching = orig.caching.copy();
	}
	
	/**
	 * Returns deep copy of this position.
	 */
	public Position copy() {
		final Position position = new Position();
		position.assign(this);
		
		return position;
	}

	/**
	 * Decides if there is check in the position.
	 * @return true if there is check, false if not
	 */
	public boolean isCheck() {
		return isSquareAttacked(Color.getOppositeColor(onTurn), getKingPosition(onTurn));
	}

	private static Position createInitialPosition() {
		final Position position = new Position();
		position.setInitialPosition();
		
		return position;
	}
	
	public boolean equals (final Object obj) {
		if (!(obj instanceof Position)) {
			return false;
		}
		
		final Position pos = (Position) obj;
		
		if (this.onTurn != pos.onTurn || this.epFile != pos.epFile)
			return false;
		
		if (!this.castlingRights.equals(pos.castlingRights))
			return false;
		
		if (!Arrays.equals (this.pieces, pos.pieces))
			return false;
		
		return true;
	}
	
	/**
	 * Returns hash code for use by collections.
	 */
	public int hashCode() {
		return (int) getHash();
	}
	
	/**
	 * Checks integrity of the position.
	 * This method may be called after refreshCachedData and some calls to makeMove. It checks
	 * if cached data and some another data are still correct. If not it throws an exception.
	 */
	public void checkIntegrity() {
		// Piece masks
		long expectedOccupancy = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			long expectedColorOccupancy = 0;
			
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final long pieceMask = this.pieces[getPieceMaskIndex (color, pieceType)];
				
				if ((pieceMask & expectedOccupancy) != 0)
					throw new RuntimeException("There are more pieces on one square");
				
				expectedOccupancy |= pieceMask;
				expectedColorOccupancy |= pieceMask;
			}
			
			if (this.colorOccupancy[color] != expectedColorOccupancy)
				throw new RuntimeException("Corrupted color occupancy");
		}
		
		if (this.occupancy != expectedOccupancy)
			throw new RuntimeException("Corrupted occupancy");
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final long squareMask = BitBoard.getSquareMask(square);
			final int pieceType = pieceTypeOnSquares[square];
			
			if (pieceType == PieceType.NONE) {
				if ((occupancy & squareMask) != 0)
					throw new RuntimeException("Corrupted NONE pieceType");
			}
			else {
				final long whiteMask = this.pieces[getPieceMaskIndex (Color.WHITE, pieceType)];
				final long blackMask = this.pieces[getPieceMaskIndex (Color.BLACK, pieceType)];
				
				if (((whiteMask | blackMask) & squareMask) == 0)
					throw new RuntimeException("Corrupted pieceType");
			}
		}
		
		// Hash
		final long oldHash = getHash();
		updateHash();
		
		if (getHash() != oldHash)
			throw new RuntimeException("Hash was corrupted");

		// Material evaluation
		final int oldMaterialEvaluation = getMaterialEvaluation();
		updateMaterialEvaluation();
		
		if (getMaterialEvaluation() != oldMaterialEvaluation)
			throw new RuntimeException("Material evaluation was corrupted");
	}
	
	/**
	 * Returns hash of this position.
	 * @return position hash
	 */
	public long getHash() {
		return caching.getHash();
	}

	/**
	 * Returns color of side with more pieces.
	 * @return color of side with more pieces
	 */
	public int getSideWithMorePieces() {
		final int whitePieceCount = BitBoard.getSquareCount(colorOccupancy[Color.WHITE]);
		final int blackPieceCount = BitBoard.getSquareCount(colorOccupancy[Color.BLACK]);
		
		return (whitePieceCount >= blackPieceCount) ? Color.WHITE : Color.BLACK;
	}

	/**
	 * Checks if EP is possible.
	 * @return true if EP is possible, false if not
	 */
    public boolean isEnPassantPossible() {
    	if (epFile == File.NONE)
    		return false;
    	
    	final long pawnMask = getPiecesMask (onTurn, PieceType.PAWN);

    	for (int direction = EpDirection.FIRST; direction < EpDirection.LAST; direction++) {
            final EpMoveRecord record = PieceMoveTables.getEpMoveRecord(onTurn, epFile, direction);
            
            if (record != null) {
            	final int beginSquare = record.getBeginSquare();
            
            	if ((pawnMask & BitBoard.getSquareMask (beginSquare)) != 0) {
            		final Move epCheckMove = new Move();
            		
            		epCheckMove.initialize(CastlingRights.FIRST_INDEX, epFile);
            		epCheckMove.setMovingPieceType(PieceType.PAWN);
            		epCheckMove.setBeginSquare (beginSquare);
            		epCheckMove.finishEnPassant (record.getTargetSquare());
            		
            		makeEnPassantMove(epCheckMove);
            		
                	final boolean isCheck = isKingNotOnTurnAttacked();
            		
            		undoEnPassantMove(epCheckMove);
            		
            		if (!isCheck)
            			return true;
            	}
            }
    	}

    	return false;
    }
    
    public int getMaterialEvaluation() {
    	return caching.getMaterialEvaluation();
    }
    
    public MaterialHash getMaterialHash() {
    	return new MaterialHash (this, onTurn);
    }
	
	public String toString() {
		final Fen fen = new Fen();
		fen.setPosition(this);
		
		final StringWriter writer = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(writer);
		fen.writeFen(printWriter);
		printWriter.flush();
		
		return writer.toString();
	}
	
	/**
	 * Checks if given pseudolegal move is legal.
	 * Temporary modifies this position.
	 * @param move move to check
	 * @return if move is legal
	 */
	public boolean isLegalMove(final Move move) {
		makeMove(move);

		final boolean isAttack = isKingNotOnTurnAttacked();
		
		undoMove(move);
			
		return !isAttack;
	}

	public boolean isKingNotOnTurnAttacked() {
		final int oppositeColor = Color.getOppositeColor(onTurn);
		final int kingPos = getKingPosition(oppositeColor);
		final boolean isCheck = isSquareAttacked(onTurn, kingPos);
		
		return isCheck;
	}
	
	public int getCheapestAttacker (final int color, final int square) {
		// Pawn
		final int oppositeColor = Color.getOppositeColor(color);
		final long attackingPawnMask = pieces[getPieceMaskIndex (color, PieceType.PAWN)] & PawnAttackTable.getItem(oppositeColor, square);

		if (attackingPawnMask != 0)
			return BitBoard.getFirstSquare(attackingPawnMask);

		// Knight
		final long attackingKnightMask = pieces[getPieceMaskIndex (color, PieceType.KNIGHT)] & FigureAttackTable.getItem (PieceType.KNIGHT, square);
		
		if (attackingKnightMask != 0)
			return BitBoard.getFirstSquare(attackingKnightMask);
		
		// Bishop
		final long attackingBishopMask = pieces[getPieceMaskIndex (color, PieceType.BISHOP)] & FigureAttackTable.getItem(PieceType.BISHOP, square);
		
		for (BitLoop loop = new BitLoop(attackingBishopMask); loop.hasNextSquare(); ) {
			final int testSquare = loop.getNextSquare();

			if ((BetweenTable.getItem(square, testSquare) & occupancy) == 0)
				return testSquare;
		}
		
		// Rook
		final long attackingRookMask = pieces[getPieceMaskIndex (color, PieceType.ROOK)] & FigureAttackTable.getItem(PieceType.ROOK, square);
		
		for (BitLoop loop = new BitLoop(attackingRookMask); loop.hasNextSquare(); ) {
			final int testSquare = loop.getNextSquare();

			if ((BetweenTable.getItem(square, testSquare) & occupancy) == 0)
				return testSquare;
		}

		// Queen
		final long attackingQueenMask = pieces[getPieceMaskIndex (color, PieceType.QUEEN)] & FigureAttackTable.getItem(PieceType.QUEEN, square);
		
		for (BitLoop loop = new BitLoop(attackingQueenMask); loop.hasNextSquare(); ) {
			final int testSquare = loop.getNextSquare();

			if ((BetweenTable.getItem(square, testSquare) & occupancy) == 0)
				return testSquare;
		}

		// King
		final long attackingKingMask = pieces[getPieceMaskIndex (color, PieceType.KING)] & FigureAttackTable.getItem (PieceType.KING, square);
				
		if (attackingKingMask != 0)
			return BitBoard.getFirstSquare(attackingKingMask);
		
		return Square.NONE;
	}

	public int getStaticExchangeEvaluation (final int color, final int square) {
		final int attackerSquare = getCheapestAttacker (color, square);
		
		if (attackerSquare != Square.NONE) {
			final int capturedPieceType = getPieceTypeOnSquare(square);
			
			if (capturedPieceType == PieceType.NONE)
				return 0;
			
			if (capturedPieceType == PieceType.KING)
				return Evaluation.MAX;
			
			final int attackerPieceType = getPieceTypeOnSquare(attackerSquare);
			final Move move = new Move();
			
			if (attackerPieceType == PieceType.NONE) {
				getCheapestAttacker(color, square);
			}
			
			move.initialize(castlingRights.getIndex(), epFile);
			move.setBeginSquare(attackerSquare);
			move.setMovingPieceType(attackerPieceType);
			
			if (Move.isPromotion(attackerPieceType, square)) {
				move.finishPromotion(square, capturedPieceType, PieceType.QUEEN);
			}
			else {
				move.finishNormalMove(square, capturedPieceType);
			}
			
			makeMove(move);	
			final int childEvaluation = getStaticExchangeEvaluation (Color.getOppositeColor(color), square);
			undoMove(move);

			return Math.max(0, PieceTypeEvaluations.getPieceTypeEvaluation(capturedPieceType) - childEvaluation);
		}
		else
			return 0;
	}

	@Override
	public int getPieceCount(final int color, final int pieceType) {
		final long pieceMask = getPiecesMask(color, pieceType);
		
		return BitBoard.getSquareCount(pieceMask);
	}
}
