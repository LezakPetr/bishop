package bishop.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import bishop.engine.GameStage;
import bishop.tables.*;
import utils.IAssignable;
import utils.ICopyable;

import bishop.engine.Evaluation;

/**
 * Representation of chess position with additional information that affects
 * generation of moves. 
 * @author Ing. Petr Ležák
 */
public final class Position implements IPosition, ICopyable<Position>, IAssignable<Position> {

	// Primary piece information - the source of truth
	private final long[] pieceTypeMasks = new long[PieceType.LAST];   // Which squares are occupied by given piece types (no matter of color)
	private final long[] colorOccupancy = new long[Color.LAST];   // Which squares are occupied by some piece with given color
	
	// Cached piece information
	private long occupancy;   // Which squares are occupied by some piece

	private int onTurn;   // Color of player on turn
	private final CastlingRights castlingRights = new CastlingRights();   // Rights for castling
	private int epFile;   // File where pawn advanced by two squares in last move (or File.NONE)

	private final IPositionCaching caching;
	private final Move epCheckMove = new Move();

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
		
		final long moveMask = beginSquareMask | targetSquareMask;
				
		pieceTypeMasks[movingPieceType] ^= moveMask;
		colorOccupancy[onTurn] ^= moveMask;
		occupancy ^= moveMask;
		
		caching.movePiece (onTurn, movingPieceType, beginSquare, targetSquare);

		if (capturedPieceType != PieceType.NONE) {
			pieceTypeMasks[capturedPieceType] ^= targetSquareMask;
			colorOccupancy[oppositeColor] ^= targetSquareMask;
			occupancy ^= targetSquareMask;
			
			caching.removePiece (oppositeColor, capturedPieceType, targetSquare);
		}

		// Update castling rights
		if (!castlingRights.isEmpty() && (moveMask & CastlingRights.AFFECTED_SQUARES) != 0) {
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
		final long moveMask = beginSquareMask | targetSquareMask;
				
		pieceTypeMasks[movingPieceType] ^= moveMask;
		colorOccupancy[onTurn] ^= moveMask;
		occupancy ^= moveMask;
		
		caching.movePiece(onTurn, movingPieceType, targetSquare, beginSquare);

		if (capturedPieceType != PieceType.NONE) {
			pieceTypeMasks[capturedPieceType] ^= targetSquareMask;
			colorOccupancy[oppositeColor] ^= targetSquareMask;
			occupancy ^= targetSquareMask;
			
			caching.addPiece(oppositeColor, capturedPieceType, targetSquare);
		}

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
		
		final long moveMask = beginSquareMask | targetSquareMask;
		
		pieceTypeMasks[PieceType.PAWN] ^= beginSquareMask;
		pieceTypeMasks[promotionPieceType] ^= targetSquareMask;
		colorOccupancy[onTurn] ^= moveMask;
		occupancy ^= moveMask;
		
		caching.removePiece(onTurn, PieceType.PAWN, beginSquare);
		caching.addPiece(onTurn, promotionPieceType, targetSquare);
		
		if (capturedPieceType != PieceType.NONE) {
			pieceTypeMasks[capturedPieceType] ^= targetSquareMask;
			colorOccupancy[oppositeColor] ^= targetSquareMask;
			occupancy ^= targetSquareMask;
			
			caching.removePiece(oppositeColor, capturedPieceType, targetSquare);
		}			

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
		final long moveMask = beginSquareMask | targetSquareMask;
		
		pieceTypeMasks[PieceType.PAWN] ^= beginSquareMask;
		pieceTypeMasks[promotionPieceType] ^= targetSquareMask;
		colorOccupancy[onTurn] ^= moveMask;
		occupancy ^= moveMask;

		caching.addPiece(onTurn, PieceType.PAWN, beginSquare);
		caching.removePiece(onTurn, promotionPieceType, targetSquare);

		if (capturedPieceType != PieceType.NONE) {
			pieceTypeMasks[capturedPieceType] ^= targetSquareMask;
			colorOccupancy[oppositeColor] ^= targetSquareMask;
			occupancy ^= targetSquareMask;
			
			caching.addPiece(oppositeColor, capturedPieceType, targetSquare);
		}

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

		final CastlingConstants castlingConstants = CastlingConstants.of(onTurn, castlingType);

		final long kingChanges = castlingConstants.getKingChangeMask();
		final long rookChanges = castlingConstants.getRookChangeMask();
		final long occupancyChanges = kingChanges ^ rookChanges;

		final int rookBeginSquare = castlingConstants.getRookBeginSquare();
		final int rookTargetSquare = castlingConstants.getRookTargetSquare();
		
		caching.movePiece(onTurn, PieceType.KING, beginSquare, targetSquare);
		caching.movePiece(onTurn, PieceType.ROOK, rookBeginSquare, rookTargetSquare);

		pieceTypeMasks[PieceType.KING] ^= kingChanges;
		pieceTypeMasks[PieceType.ROOK] ^= rookChanges;
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

		final CastlingConstants castlingConstants = CastlingConstants.of(onTurn, castlingType);

		final long kingChanges = castlingConstants.getKingChangeMask();
		final long rookChanges = castlingConstants.getRookChangeMask();
		final long occupancyChanges = kingChanges ^ rookChanges;

		final int rookBeginSquare = castlingConstants.getRookBeginSquare();
		final int rookTargetSquare = castlingConstants.getRookTargetSquare();
		
		caching.movePiece(onTurn, PieceType.KING, targetSquare, beginSquare);
		caching.movePiece(onTurn, PieceType.ROOK, rookTargetSquare, rookBeginSquare);

		pieceTypeMasks[PieceType.KING] ^= kingChanges;
		pieceTypeMasks[PieceType.ROOK] ^= rookChanges;
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
		
		final long moveMask = beginSquareMask | targetSquareMask;
		final long changeMask = moveMask | epSquareMask;
		
		pieceTypeMasks[PieceType.PAWN] ^= changeMask;
		colorOccupancy[onTurn] ^= moveMask;
		colorOccupancy[oppositeColor] ^= epSquareMask;
		occupancy ^= changeMask;
		
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
		final long moveMask = beginSquareMask | targetSquareMask;
		final long changeMask = moveMask | epSquareMask;
		
		pieceTypeMasks[PieceType.PAWN] ^= changeMask;
		colorOccupancy[onTurn] ^= moveMask;
		colorOccupancy[oppositeColor] ^= epSquareMask;
		occupancy ^= changeMask;
		
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
			final int color = piece.getColor();
			final int oppositeColor = Color.getOppositeColor(color);
			
			colorOccupancy[color] |= squareMask;
			colorOccupancy[oppositeColor] &= ~squareMask;
			occupancy |= squareMask;
			
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				if (pieceType == piece.getPieceType())
					pieceTypeMasks[pieceType] |= squareMask;
				else
					pieceTypeMasks[pieceType] &= ~squareMask;
			}
		}
		else {
			for (int color = Color.FIRST; color < Color.LAST; color++)
				colorOccupancy[color] &= ~squareMask;
			
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++)
				pieceTypeMasks[pieceType] &= ~squareMask;
			
			occupancy &= ~squareMask;
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
		occupancy = BitBoard.EMPTY;
		Arrays.fill(colorOccupancy, BitBoard.EMPTY);
		Arrays.fill(pieceTypeMasks, BitBoard.EMPTY);

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
		return pieceTypeMasks[type] & colorOccupancy[color];
	}

	/**
	 * Returns mask of given piece regardless of their color.
	 * @param type piece type
	 * @return mask of given piece type
	 */
	public long getBothColorPiecesMask(final int type) {
		return pieceTypeMasks[type];
	}

	/**
	 * Returns mask of promotion figures with given color.
	 * @param color color of figures
	 * @return mask of promotion figures with given color
	 */
	public long getPromotionFigureMask(final int color) {
		return colorOccupancy[color] & ~(pieceTypeMasks[PieceType.KING] | pieceTypeMasks[PieceType.PAWN]);
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
		// Speedup (especially in endings) - if there is no piece by color occupancy that
		// can attack the square then return false.
		if ((getColorOccupancy(color) & SuperAttackTable.getItem(square)) == 0)
			return false;

		// Short move figures
		if ((getPiecesMask(color, PieceType.KING) & FigureAttackTable.getItem (PieceType.KING, square)) != 0)
			return true;

		if ((getPiecesMask(color, PieceType.KNIGHT) & FigureAttackTable.getItem (PieceType.KNIGHT, square)) != 0)
			return true;

		// Pawn
		final int oppositeColor = Color.getOppositeColor(color);

		if ((getPiecesMask(color, PieceType.PAWN) & PawnAttackTable.getItem(oppositeColor, square)) != 0)
			return true;

		// Long move figures
		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, square, occupancy);
		final long orthogonalMask = LineAttackTable.getAttackMask(orthogonalIndex);
		
		if ((getPiecesMask(color, PieceType.ROOK) & orthogonalMask) != 0)
			return true;

		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, square, occupancy);
		final long diagonalMask = LineAttackTable.getAttackMask(diagonalIndex);

		if ((getPiecesMask(color, PieceType.BISHOP) & diagonalMask) != 0)
			return true;
		
		final long queenMask = orthogonalMask | diagonalMask;
		
		if ((getPiecesMask(color, PieceType.QUEEN) & queenMask) != 0)
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
		// Speedup (especially in endings) - if there is no piece by color occupancy that
		// can attack the square then return false.
		if ((getColorOccupancy(color) & SuperAttackTable.getItem(square)) == 0)
			return BitBoard.EMPTY;

		long attackingPieceMask = BitBoard.EMPTY;
		
		// Short move figures
		attackingPieceMask |= pieceTypeMasks[PieceType.KING] & FigureAttackTable.getItem (PieceType.KING, square);
		attackingPieceMask |= pieceTypeMasks[PieceType.KNIGHT] & FigureAttackTable.getItem (PieceType.KNIGHT, square);

		// Pawn
		final int oppositeColor = Color.getOppositeColor(color);
		attackingPieceMask |= pieceTypeMasks[PieceType.PAWN] & PawnAttackTable.getItem(oppositeColor, square);

		// Long move figures
		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, square, occupancy);
		final long orthogonalMask = LineAttackTable.getAttackMask(orthogonalIndex);
		
		attackingPieceMask |= pieceTypeMasks[PieceType.ROOK] & orthogonalMask;

		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, square, occupancy);
		final long diagonalMask = LineAttackTable.getAttackMask(diagonalIndex);

		attackingPieceMask |= pieceTypeMasks[PieceType.BISHOP] & diagonalMask;
		
		final long queenMask = orthogonalMask | diagonalMask;
		attackingPieceMask |= pieceTypeMasks[PieceType.QUEEN] & queenMask;

		return attackingPieceMask & colorOccupancy[color];
	}
	
	/**
	 * Returns count of pieces with given color that attacks given square.
	 * @param color color of attacking pieces
	 * @param square checked square
	 * @return count of attacking pieces
	 */
	public int getCountOfAttacks (final int color, final int square) {
		final long attackingPieceMask = getAttackingPieces(color, square);
				
		return BitBoard.getSquareCountSparse(attackingPieceMask);
	}

	/**
	 * Finds type of piece on given square and returns it.
	 * @param square searched square
	 * @return type of piece on given square or PieceType.NONE
	 */
	public int getPieceTypeOnSquare (final int square) {
		final long mask = BitBoard.getSquareMask(square);
		
		if ((occupancy & mask) == 0)
			return PieceType.NONE;

		if ((pieceTypeMasks[PieceType.PAWN] & mask) != 0)
			return PieceType.PAWN;

		if ((pieceTypeMasks[PieceType.KNIGHT] & mask) != 0)
			return PieceType.KNIGHT;

		if ((pieceTypeMasks[PieceType.BISHOP] & mask) != 0)
			return PieceType.BISHOP;

		if ((pieceTypeMasks[PieceType.ROOK] & mask) != 0)
			return PieceType.ROOK;

		if ((pieceTypeMasks[PieceType.QUEEN] & mask) != 0)
			return PieceType.QUEEN;

		if ((pieceTypeMasks[PieceType.KING] & mask) != 0)
			return PieceType.KING;

		throw new RuntimeException("Corrupted position on square " + square);
	}

	/**
	 * Makes given move.
	 * @param move move
	 */
	public void makeMove (final Move move) {
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
	}

	/**
	 * Undos given move.
	 * @param move move
	 */
	public void undoMove (final Move move) {
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
	}
	
	/**
	 * Returns position of king with given color.
	 * @param color color of king
	 * @return square where is king with given color
	 */
	public int getKingPosition (final int color) {
		final long kingMask = getPiecesMask(color, PieceType.KING);
				
		return BitBoard.getFirstSquare(kingMask);
	}

	/**
	 * This method updates cached data.
	 * It must be called after manual changing of position (other than by makeMove).
	 */
	public void refreshCachedData() {
		updatePiecesMasks();
		updateCaches();
	}

	private void updatePiecesMasks() {
		occupancy = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			occupancy |= colorOccupancy[color];
	}
	
	public long calculateHash() {
		long hash = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				for (BitLoop loop = new BitLoop(getPiecesMask(color, pieceType)); loop.hasNextSquare(); ) {
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
	
	private void updateCaches() {
		caching.refreshCache(this);
	}

	/**
	 * Assigns given original position into this.
	 * @param orig original position
	 */
	@Override
	public void assign (final Position orig) {
		System.arraycopy(orig.pieceTypeMasks, 0, this.pieceTypeMasks, 0, PieceType.LAST);
		System.arraycopy(orig.colorOccupancy, 0, this.colorOccupancy, 0, Color.LAST);
		this.occupancy = orig.occupancy;

		this.onTurn = orig.onTurn;
		this.castlingRights.assign (orig.castlingRights);
		this.epFile = orig.epFile;
		this.caching.assign(orig.caching);
	}

	/**
	 * Assigns given original position into this.
	 * @param orig original position
	 */
	public void assign (final IPosition orig) {
		clearPosition();

		for (int square = Square.FIRST; square < Square.LAST; square++) {
			this.setSquareContent(square, orig.getSquareContent (square));
		}

		this.onTurn = orig.getOnTurn();
		this.castlingRights.assign (orig.getCastlingRights());
		this.epFile = orig.getEpFile();

		refreshCachedData();
	}
	
	/**
	 * Returns deep copy of this position.
	 */
	public Position copy() {
		final Position position = new Position(caching instanceof NullPositionCaching);
		position.setCombinedPositionEvaluationTable(caching.getCombinedPositionEvaluationTable());
		position.setPieceTypeEvaluations(caching.getPieceTypeEvaluations());
		position.assign(this);
		
		return position;
	}

	/**
	 * Decides if there is check in the position.
	 * @return true if there is check, false if not
	 */
	public boolean isCheck() {
		final int kingPosition = getKingPosition(onTurn);

		return isSquareAttacked(Color.getOppositeColor(onTurn), kingPosition);
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
		
		if (!Arrays.equals (this.pieceTypeMasks, pos.pieceTypeMasks))
			return false;
		
		if (!Arrays.equals (this.colorOccupancy, pos.colorOccupancy))
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
	 * Returns table position evaluation for given game stage.
	 * @param gameStage game stage
	 * @return evaluation from the white side
	 */
	public int getTablePositionEvaluation (final int gameStage) {
		return caching.getTablePositionEvaluation(gameStage);
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
				final long pieceMask = getPiecesMask(color, pieceType);
				
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
			final int pieceType = getPieceTypeOnSquare(square);
			
			if (pieceType == PieceType.NONE) {
				if ((occupancy & squareMask) != 0)
					throw new RuntimeException("Corrupted NONE pieceType");
			}
			else {
				final long whiteMask = getPiecesMask(Color.WHITE, pieceType);
				final long blackMask = getPiecesMask(Color.BLACK, pieceType);
				
				if (((whiteMask | blackMask) & squareMask) == 0)
					throw new RuntimeException("Corrupted pieceType");
			}
		}
		
		// Hash
		final long oldHash = getHash();
		final MaterialHash oldMaterialHash = getMaterialHash().copy();
		final long oldCombinedEvaluation = caching.getCombinedEvaluation();
		final int oldMaterialEvaluation = caching.getMaterialEvaluation();
		final int oldGameStageUnbound = caching.getGameStageUnbound();

		updateCaches();
		
		if (getHash() != oldHash)
			throw new RuntimeException("Hash was corrupted");

		if (!getMaterialHash().equals(oldMaterialHash))
			throw new RuntimeException("Material hash was corrupted");

		if (caching.getCombinedEvaluation() != oldCombinedEvaluation)
			throw new RuntimeException("Combined evaluation was corrupted");

		if (caching.getMaterialEvaluation() != oldMaterialEvaluation)
			throw new RuntimeException("Material evaluation was corrupted");

		if (caching.getGameStageUnbound() != oldGameStageUnbound)
			throw new RuntimeException("Game stage unbound was corrupted");
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

		final int oppositeColor = Color.getOppositeColor(onTurn);
    	final int epSquare = BoardConstants.getEpSquare(oppositeColor, epFile);
		final long possibleBeginSquares = BoardConstants.getConnectedPawnSquareMask(epSquare) & getPiecesMask(onTurn, PieceType.PAWN);

		for (BitLoop loop = new BitLoop(possibleBeginSquares); loop.hasNextSquare(); ) {
			final int beginSquare = loop.getNextSquare();

			epCheckMove.initialize(CastlingRights.FIRST_INDEX, epFile);
			epCheckMove.setMovingPieceType(PieceType.PAWN);
			epCheckMove.setBeginSquare (beginSquare);
			epCheckMove.finishEnPassant (BoardConstants.getEpTargetSquare(oppositeColor, epFile));

			makeEnPassantMove(epCheckMove);

			final boolean isCheck = isKingNotOnTurnAttacked();

			undoEnPassantMove(epCheckMove);

			if (!isCheck)
				return true;
		}

    	return false;
    }

	public IMaterialHashRead getMaterialHash() {
    	return caching.getMaterialHash();
    }
    
	public MaterialHash calculateMaterialHash() {
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

		return isSquareAttacked(onTurn, kingPos);
	}

	@Override
	public int getPieceCount(final int color, final int pieceType) {
		final long pieceMask = getPiecesMask(color, pieceType);
		
		return BitBoard.getSquareCount(pieceMask);
	}

	@Override
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table) {
		caching.setCombinedPositionEvaluationTable(table);
	}

	@Override
	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations) {
		caching.setPieceTypeEvaluations (pieceTypeEvaluations);
	}

	@Override
	public int getMaterialEvaluation() {
		return caching.getMaterialEvaluation();
	}

	@Override
	public int getGameStage() {
		return Math.min(caching.getGameStageUnbound(), GameStage.MAX);
	}
}
