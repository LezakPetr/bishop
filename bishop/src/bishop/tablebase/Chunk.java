package bishop.tablebase;

import java.util.Arrays;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Symmetry;
import bishop.tables.BetweenTable;
import bishop.tables.EmptyEpMaskTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.SquareSymmetryTable;
import bishop.tables.SymmetryTable;

/**
 * Chunk is a immutable mapping between positions and table indices. 
 * Only positions with same position of kings and EP files are placed in one chunk.
 * The mapping is calculated on the fly and is not stored in the table. This means
 * that the mapping cannot be changed because otherwise it destroys the table.
 *
 * @author Ing. Petr Ležák
 */
public class Chunk {
	
	private final int onTurn;   // Side on turn
	private final int whiteKingSquare;   // Position of white king
	private final int blackKingSquare;   // Position of black king
	private final int epFile;   // EP file or File.NONE in case of normal chunk
	private final boolean hasPrevEpFilePawn;   // If there is a pawn on file preceding EP file
	private final SquareCombination[] combinationArray;   // Array of square combinations
	private final long[] multiplicatorArray;   // Array of coefficients for combination indices
	private final long beginIndex;   // First table index
	private final long endIndex;   // Index after last table index
	private final long[][] fixedPawnMasks;   // Mask of pawns that are same for all items of chunk; index: symmetry, color
	
	/**
	 * Creates chunk.
	 * @param version version of the table format
	 * @param onTurn side on turn
	 * @param whiteKingSquare position of white king
	 * @param blackKingSquare position of black king
	 * @param epFile EP file or File.NONE in case of normal chunk
	 * @param definitionArray array of combination definitions 
	 * @param beginIndex first table index
	 */
	public Chunk(final int version, final int onTurn, final int whiteKingSquare, final int blackKingSquare, final int epFile, final boolean hasPrevEpFilePawn, final CombinationDefinition[] definitionArray, final long beginIndex) {
		this.onTurn = onTurn;
		this.whiteKingSquare = whiteKingSquare;
		this.blackKingSquare = blackKingSquare;
		this.epFile = epFile;
		this.hasPrevEpFilePawn = hasPrevEpFilePawn;
		
		this.fixedPawnMasks = new long[Symmetry.LAST][Color.LAST];
		fillFixedPawnMasks();

		this.combinationArray = new SquareCombination[definitionArray.length];
		this.multiplicatorArray = new long[definitionArray.length];
		this.beginIndex = beginIndex;
		
		final long resultCount = initializeCombinations(definitionArray);
		this.endIndex = beginIndex + resultCount;
	}
	
	/**
	 * Fills masks of fixed pawns.
	 * They are pawns fixed by EP.
	 */
	private void fillFixedPawnMasks() {
		if (epFile != File.NONE) {
			for (int symmetry = Symmetry.FIRST; symmetry < Symmetry.LAST; symmetry++) {
				final int notOnTurn = Color.getOppositeColor(onTurn);
		
				final int epSquare = BoardConstants.getEpSquare(notOnTurn, epFile);
				final long epMask = BitBoard.getSquareMask(epSquare);
				fixedPawnMasks[symmetry][notOnTurn] = SquareSymmetryTable.transformBoard (symmetry, epMask);
				
				if (hasPrevEpFilePawn)
					fixedPawnMasks[symmetry][onTurn] = SquareSymmetryTable.transformBoard (symmetry, BoardConstants.getPrevEpFileMask(notOnTurn, epFile));
				else
					fixedPawnMasks[symmetry][onTurn] = SquareSymmetryTable.transformBoard (symmetry, BoardConstants.getNextEpFileMask(notOnTurn, epFile));
			}
		}
	}

	/**
	 * Fills combinationArray and multiplicatorArray.
	 * Last item in the array is traversed first.
	 * In future we may change the order of definitions so pawns are traversed 
	 * last, it will be better for PreviousSymbolProbabilityModelSelector.
	 * @param definitionArray array of combination definitions
	 * @return index after last table index
	 */
	public long initializeCombinations(final CombinationDefinition[] definitionArray) {
		long multiplicator = 1;
		
		for (int i = definitionArray.length-1; i >= 0 ; i--) {
			final CombinationDefinition definition = definitionArray[i];
			final long squareMask = getAllowedSquareMask (definition);
			final CombinationDefinition updatedDefinition;
			
			if (definition.getPiece().getPieceType() == PieceType.PAWN && epFile != File.NONE && definition.getCount() >= 1)
				updatedDefinition = definition.getWithoutSomePieces(1);
			else
				updatedDefinition = definition;
			
			final SquareCombinationKey squareCombinationKey = new SquareCombinationKey(updatedDefinition, squareMask);
			final SquareCombination combination = SquareCombinationRegistrar.getInstance().getDefinition(squareCombinationKey);
			
			combinationArray[i] = combination;
			multiplicatorArray[i] = multiplicator;
			
			multiplicator *= combination.getCount();
		}
		
		return multiplicator;
	}
	
	/**
	 * Calculates squares that are combined in given combination definition.
	 * Pawns are disallowed on first and eight rank.
	 * All pieces are disallowed on king squares and fixed pawn squares.
	 * If chunk is EP then all pieces are disallowed on two squares below
	 * EP square. Pawns are also disallowed on square left to EP square
	 * (this pawn is controlled by hasPrevEpFilePawn).
	 * All pieces are also disallowed on squares where they will cause near check.
	 * @param definition combination definition
	 * @return mask of squares that are combined
	 */
	private long getAllowedSquareMask(final CombinationDefinition definition) {
		final Piece piece = definition.getPiece();
		final int pieceType = piece.getPieceType();
		final int notOnTurn = Color.getOppositeColor(onTurn);
		
		long mask = (pieceType == PieceType.PAWN) ? ~BoardConstants.RANK_18_MASK : BitBoard.FULL;

		if (epFile != File.NONE) {
			mask &= ~EmptyEpMaskTable.getItem(notOnTurn, epFile);
			
			if (pieceType == PieceType.PAWN && piece.getColor() == onTurn) {
				mask &= ~BoardConstants.getPrevEpFileMask(notOnTurn, epFile);
			}
		}

		mask &= ~BitBoard.getSquareMask(whiteKingSquare);
		mask &= ~BitBoard.getSquareMask(blackKingSquare);
		mask &= ~getNearCheckSquares(definition);
				
		for (int pawnColor = Color.FIRST; pawnColor < Color.LAST; pawnColor++)
			mask &= ~fixedPawnMasks[Symmetry.IDENTITY][pawnColor];
		
		return mask;
	}
	
	/**
	 * Returns squares where given piece causes near check. It is a check
	 * that cannot be blocked by another piece. Only pieces on turn
	 * can cause near check.
	 * @param definition definition of combination
	 * @return mask of squares where piece causes near check
	 */
	private long getNearCheckSquares(final CombinationDefinition definition) {
		final Piece piece = definition.getPiece();
		
		if (piece.getColor() != onTurn)
			return BitBoard.EMPTY;
		
		final int oppositeColor = Color.getOppositeColor(onTurn);
		final int kingSquare = (oppositeColor == Color.WHITE) ? whiteKingSquare : blackKingSquare;
		
		final int pieceType = piece.getPieceType();
		
		if (pieceType == PieceType.PAWN) {
			return PawnAttackTable.getItem(oppositeColor, kingSquare);
		}
		else {
			final long movesMask = FigureAttackTable.getItem(pieceType, kingSquare);
			long nearCheckMask = 0;
			
			for (BitLoop loop = new BitLoop(movesMask); loop.hasNextSquare(); ) {
				final int square = loop.getNextSquare();
				
				if (BetweenTable.getItem(square, kingSquare) == 0)
					nearCheckMask |= BitBoard.getSquareMask(square);
			}
			
			return nearCheckMask;
		}
	}

	/**
	 * Returns first index.
	 * @return first index
	 */
	public long getBeginIndex() {
		return beginIndex;
	}

	/**
	 * Returns index after last index.
	 * @return index after last index
	 */
	public long getEndIndex() {
		return endIndex;
	}

	public long calculateTableIndex(final Position position, final int symmetry) {
		final int[] combinationIndices = new int[combinationArray.length];
		
		for (int i = 0; i < combinationArray.length; i++) {
			final SquareCombination combination = combinationArray[i];
			final Piece piece = combination.getKey().getDefinition().getPiece();
			long piecesMask = position.getPiecesMask(piece.getColor(), piece.getPieceType());
			
			if (piece.getPieceType() == PieceType.PAWN)
				piecesMask &= ~fixedPawnMasks[symmetry][piece.getColor()];
			
			combinationIndices[i] = combination.calculateIndex (piecesMask, symmetry);
		}
		
		return calculateIndex (combinationIndices);
	}
	
	public long calculateIndex(final int[] combinationIndices) {
		long index = beginIndex;
		
		for (int i = 0; i < combinationArray.length; i++) {
			final int combinationIndex = combinationIndices[i];
			
			if (combinationIndex < 0)
				return -1;
			
			index += combinationIndex * getMultiplicatorAt (i);
		}
		
		return index;
	}
	
	public int getSquareCombinationCount() {
		return combinationArray.length;
	}
	
	public SquareCombination getSquareCombinationAt (final int index) {
		return combinationArray[index];
	}

	public long getMultiplicatorAt (final int index) {
		return multiplicatorArray[index];
	}

	public int getWhiteKingSquare() {
		return whiteKingSquare;
	}

	public int getBlackKingSquare() {
		return blackKingSquare;
	}

	public int getOnTurn() {
		return onTurn;
	}

	public int getEpFile() {
		return epFile;
	}

	public long getFixedPawnMask (final int color) {
		return fixedPawnMasks[Symmetry.IDENTITY][color];
	}
	
	/**
	 * Fills all fixed (non-combined) data from this chunk into position.
	 * It includes:
	 *  - side on turn
	 *  - king positions
	 *  - fixed pawns
	 *  - EP file
	 */
	public void fillFixedDataToPosition(final Position position) {
		position.setOnTurn(onTurn);
		
		final Piece whiteKing = Piece.withColorAndType(Color.WHITE, PieceType.KING);
		position.setSquareContent(whiteKingSquare, whiteKing);
		
		final Piece blackKing = Piece.withColorAndType(Color.BLACK, PieceType.KING);
		position.setSquareContent(blackKingSquare, blackKing);
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			position.setMoreSquaresContent(fixedPawnMasks[Symmetry.IDENTITY][color], Piece.withColorAndType(color, PieceType.PAWN));
		
		position.setEpFile(epFile);
	}
}
