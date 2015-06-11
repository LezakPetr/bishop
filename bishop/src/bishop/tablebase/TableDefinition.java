package bishop.tablebase;

import java.util.LinkedList;
import java.util.List;
import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.MaterialHash;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Square;
import bishop.base.Symmetry;
import bishop.tables.EmptyEpMaskTable;
import bishop.tables.SquareSymmetryTable;
import bishop.tables.SymmetryTable;

/**
 * Immutable definition of one table.
 * Main purpose of this class is to define mapping between positions and table
 * indices. For this purpose table is divided into chunks - groups of positions
 * with same king positions and (in case of EP chunks) EP files. This mapping
 * also takes into account a symmetry of the board to reduce number of
 * table indices.
 * The mapping is calculated on the fly and is not stored in the table. This means
 * that the mapping cannot be changed because otherwise it destroys the table.
 * 
 * Order of chunks:
 * First there are normal (non-EP) chunks. White king squares are traversed from
 * A1 to H8 by ranks and for each white king square black king squares are
 * traversed from A1 to H8 by ranks. For each legal pair of king squares with
 * symmetry defined as identity there is one chunk.
 * Then for tables with EP there are EP chunks. White king squares are traversed
 * from A1 to H8 by ranks and for each white king square black king squares are
 * traversed from A1 to H8 by ranks. For each legal pair of king squares with
 * symmetry defined as identity EP files are traversed from A to H
 * and for every legal file there is one chunk. Legal file means that there are
 * no kings on second, third and fourth rank on the file.
 * 
 * @author Ing. Petr Ležák
 */
public class TableDefinition {
	
	private static final int[] PIECE_TYPE_SEQUENCE = {
		PieceType.PAWN, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.QUEEN
	};
	
	private final int version;   // Version of the table format
	private final int onTurn;   // Side on turn
	private final CombinationDefinition[] definitionArray;   // Array of combination definitions
	private final int pieceCount;   // Number of pieces
	private final Chunk[][] normalChunkArray;  // Array of normal chunks; indices: whiteKinkSquare, blackKingSquare
	private final Chunk[][][][] epChunkArray;  // Array of EP chunks; indices: whiteKingSquare, blackKingSquare, targetEpFile, prevFilePawn
	
	private final Chunk[] chunkArray;   // Array of all chunks (normal + EP); index: chunkIndex
	private final long itemCount;   // Number of items (stored results)
	
	private final boolean epPossible;   // If En-passant is possible in the table
	private final boolean isPawn;   // If there is at least one pawn
	
	private final SymmetryTable symmetryTable;   // Used symmetry table
	
	public static final int MAX_GROUP_SIZE = Symmetry.LAST;

	private static final int PREV_EP_FILE_PAWN_COUNT = 2;
	
	/**
	 * Creates table definition for given material and side on turn.
	 * @param version version of the table format
	 * @param onTurn side on turn
	 * @param definitionArray array of combination definitions
	 */
	private TableDefinition(final int version, final int onTurn, final CombinationDefinition[] definitionArray) {
		this.version = version;
		this.onTurn = onTurn;
		this.definitionArray = definitionArray;
		this.normalChunkArray = new Chunk[Square.LAST][];
		this.epPossible = calculateEpPossible();
		this.epChunkArray = (epPossible) ? new Chunk[Square.LAST][][][] : null;
		
		this.isPawn = calcIsPawn();
		symmetryTable = selectSymmetryTable();
		
		final List<Chunk> chunkList = new LinkedList<Chunk>();
		this.itemCount = fillChunks(chunkList);
		this.chunkArray = chunkList.toArray(new Chunk[chunkList.size()]);
		
		this.pieceCount = calcPieceCount();
	}
	
	/**
	 * Creates table definition for given material hash.
	 * @param version version of the table format
	 * @param materialHash material hash
	 */
	public TableDefinition(final int version, final MaterialHash materialHash) {
		this (version, materialHash.getOnTurn(), calculateDefinitionArray(materialHash));
	}
	
	/**
	 * Calculates combination definition array from material hash.
	 * Array contains white pieces first and then black pieces.
	 * Pieces of same color are sorted from strongest to weakest.
	 * Kings and pieces with zero count are excluded.
	 * @param materialHash material hash
	 * @return array of combination definitions
	 */
	private static CombinationDefinition[] calculateDefinitionArray(final MaterialHash materialHash) {
		final List<CombinationDefinition> definitionList = new LinkedList<CombinationDefinition>();
		final int onTurn = materialHash.getOnTurn();
		final int[] colorSequence = {Color.getOppositeColor(onTurn), onTurn};
		
		for (int pieceType: PIECE_TYPE_SEQUENCE) {
			for (int color: colorSequence) {
				final int count = materialHash.getPieceCount(color, pieceType);
				
				if (count > 0) {
					final Piece piece = Piece.withColorAndType(color, pieceType);
					final CombinationDefinition combinationDefinition = new CombinationDefinition(piece, count);
					
					definitionList.add(combinationDefinition);
				}
			}
		}
		
		return definitionList.toArray(new CombinationDefinition[definitionList.size()]);
	}

	/**
	 * Calculates if EP is possible with given material.
	 * EP is possible if both sides have at least one pawn.
	 * @return true if EP is possible, false if not
	 */
	private boolean calculateEpPossible() {
		boolean whitePawnFound = false;
		boolean blackPawnFound = false;
		
		for (CombinationDefinition definition: definitionArray) {
			final Piece piece = definition.getPiece();
			
			if (piece.getPieceType() == PieceType.PAWN) {
				if (piece.getColor() == Color.WHITE)
					whitePawnFound = true;
				else
					blackPawnFound = true;
			}
		}
		
		return whitePawnFound && blackPawnFound;
	}
	
	/**
	 * Calculates if there is at least one pawn in the material.
	 * @return true if there is pawn, false if not
	 */
	private boolean calcIsPawn() {
		for (CombinationDefinition definition: definitionArray) {
			final Piece piece = definition.getPiece();
			
			if (piece.getPieceType() == PieceType.PAWN) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Selects symmetry table.
	 * @return symmetry table
	 */
	private SymmetryTable selectSymmetryTable() {
		return (isPawn) ? SymmetryTable.PAWN_TABLE : SymmetryTable.FULL_TABLE;
	}
	
	/**
	 * Decides if chunk with given king positions should be generated.
	 * Chunks should be generated if king positions are legal and defines
	 * identity symmetry.
	 * @param whiteKingSquare position of white king
	 * @param blackKingSquare position of black king
	 * @return true if chunk should be generated, false if not
	 */
	private boolean shouldGenerateChunk (final int whiteKingSquare, final int blackKingSquare) {
		final boolean kingsOk = BoardConstants.getKingSquareDistance(whiteKingSquare, blackKingSquare) > 1;
		final boolean isIdentity = symmetryTable.getSymmetry(whiteKingSquare, blackKingSquare) == Symmetry.IDENTITY;
				
		return kingsOk && isIdentity;
	}
	
	/**
	 * Checks if there some identity symmetry defined for given white king square.
	 * @param whiteKingSquare position of white king
	 * @return true if black king position exists that defines identity symmetry
	 */
	private boolean isSomeIdentity (final int whiteKingSquare) {
		for (int blackKingSquare = Square.FIRST; blackKingSquare < Square.LAST; blackKingSquare++) {
			if (symmetryTable.getSymmetry(whiteKingSquare, blackKingSquare) == Symmetry.IDENTITY)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Adds normal (non-EP) chunks into chunk list and stores them into
	 * normalChunkArray. Method must also calculate item indices.
	 * @param chunkList target chunk list
	 * @param beginIndex begin chunk index (item index on first chunk) 
	 * @return end chunk index (item index after last chunk)
	 */
	private long fillNormalChunks (final List<Chunk> chunkList, final long beginIndex) {
		long lastIndex = beginIndex;
		
		for (int whiteKingSquare = Square.FIRST; whiteKingSquare < Square.LAST; whiteKingSquare++) {
			if (isSomeIdentity(whiteKingSquare)) {
				normalChunkArray[whiteKingSquare] = new Chunk[Square.LAST];
				
				for (int blackKingSquare = Square.FIRST; blackKingSquare < Square.LAST; blackKingSquare++) {
					if (shouldGenerateChunk(whiteKingSquare, blackKingSquare)) {
						// No EP
						final Chunk noEpChunk = new Chunk(version, onTurn, whiteKingSquare, blackKingSquare, File.NONE, false, definitionArray, lastIndex);
						lastIndex = noEpChunk.getEndIndex();
						
						chunkList.add(noEpChunk);
						normalChunkArray[whiteKingSquare][blackKingSquare] = noEpChunk;
					}
				}
			}
		}
		
		return lastIndex;
	}
	
	/**
	 * Adds EP chunks into chunk list and stores them into
	 * epChunkArray. Method must also calculate item indices.
	 * @param chunkList target chunk list
	 * @param beginIndex begin chunk index (item index on first chunk) 
	 * @return end chunk index (item index after last chunk)
	 */
	private long fillEpChunks (final List<Chunk> colorChunkList, final long beginIndex) {
		long lastIndex = beginIndex;
		
		for (int whiteKingSquare = Square.FIRST; whiteKingSquare < Square.LAST; whiteKingSquare++) {
			if (isSomeIdentity(whiteKingSquare)) {
				epChunkArray[whiteKingSquare] = new Chunk[Square.LAST][][];

				for (int blackKingSquare = Square.FIRST; blackKingSquare < Square.LAST; blackKingSquare++) {
					lastIndex = fillEpChunksForKings(colorChunkList, lastIndex, whiteKingSquare, blackKingSquare);
				}
			}
		}
		
		return lastIndex;
	}

	/**
	 * Adds EP chunks for given king positions into chunk list and stores them
	 * into epChunkArray. Method must also calculate item indices.
	 * @param whiteKingSquare position of white king
	 * @param blackKingSquare position of black king
	 * @param chunkList target chunk list
	 * @param beginIndex begin chunk index (item index on first chunk) 
	 * @return end chunk index (item index after last chunk)
	 */
	private long fillEpChunksForKings(final List<Chunk> chunkList, final long beginIndex, final int whiteKingSquare, final int blackKingSquare) {
		long lastIndex = beginIndex;
		
		if (shouldGenerateChunk(whiteKingSquare, blackKingSquare)) {
			final int oppositeColor = Color.getOppositeColor(onTurn);
			final Chunk[][] currentChunkArray = new Chunk[File.LAST][PREV_EP_FILE_PAWN_COUNT];
			epChunkArray[whiteKingSquare][blackKingSquare] = currentChunkArray;
			
			final long kingsMask = BitBoard.getSquareMask(whiteKingSquare) | BitBoard.getSquareMask(blackKingSquare);
			
			for (int epFile = File.FIRST; epFile < File.LAST; epFile++) {
				for (int prevEpFilePawn = 0; prevEpFilePawn < PREV_EP_FILE_PAWN_COUNT; prevEpFilePawn++) {
					final boolean hasPrevEpFilePawn = Utils.intToBool(prevEpFilePawn);
					
					if ((hasPrevEpFilePawn && epFile > File.FA) || (!hasPrevEpFilePawn && epFile < File.FH)) {
						final int epSquare = Square.onFileRank(epFile, BoardConstants.getEpRank(oppositeColor));
						final long freeMask = EmptyEpMaskTable.getItem(oppositeColor, epFile) | BitBoard.getSquareMask(epSquare);
						
						if ((freeMask & kingsMask) == 0) {
							final Chunk epChunk = new Chunk(version, onTurn, whiteKingSquare, blackKingSquare, epFile, hasPrevEpFilePawn, definitionArray, lastIndex);
							lastIndex = epChunk.getEndIndex();
							
							chunkList.add(epChunk);
							currentChunkArray[epFile][prevEpFilePawn] = epChunk;
						}
					}
				}
			}
		}
		
		return lastIndex;
	}
	
	/**
	 * Adds EP chunks for given king positions into chunk list and stores them
	 * into normalChunkArray. Method must also calculate item indices.
	 * @param whiteKingSquare position of white king
	 * @param blackKingSquare position of black king
	 * @param chunkList target chunk list
	 * @param beginIndex begin chunk index (item index on first chunk) 
	 * @return end chunk index (item index after last chunk)
	 */
	private long fillChunks(final List<Chunk> chunkList) {
		long lastIndex = 0;
		
		lastIndex = fillNormalChunks(chunkList, lastIndex);
		
		if (epPossible) {
			lastIndex = fillEpChunks(chunkList, lastIndex);
		}
		
		return lastIndex;
	}
	
	/**
	 * Returns number of chunks.
	 * @return number of chunks
	 */
	public int getChunkCount() {
		return chunkArray.length;
	}
	
	/**
	 * Returns chunk at given index.
	 * @param index index of chunk
	 * @return chunk at index
	 */
	public Chunk getChunkAt(final int index) {
		return chunkArray[index];
	}
	
	/**
	 * Returns combination definition at given index.
	 * @param index index of combination definition
	 * @return combination definition
	 */
	public CombinationDefinition getCombinationDefinitionAt (final int index) {
		return definitionArray[index];
	}
	
	/**
	 * Returns number of combination definitions.
	 * @return number of combination definitions
	 */
	public int getCombinationDefinitionCount() {
		return definitionArray.length;
	}
	
	/**
	 * Calculates piece count.
	 * @return number of pieces
	 */
	private int calcPieceCount() {
		int count = 2;
		
		for (CombinationDefinition definition: definitionArray) {
			count += definition.getCount();
		}
		
		return count;
	}

	/**
	 * Returns piece count.
	 * @return number of pieces
	 */
	public int getPieceCount() {
		return pieceCount;
	}

	/**
	 * Returns count of table indices (size of the table).
	 * @return count of table indices
	 */
	public long getTableIndexCount() {
		return itemCount;
	}
	
	/**
	 * Returns count different (possibly illegal) positions.
	 * @return count of positions
	 */
	public long getPositionCount() {
		long count = 1;
		
		for (int i = 0; i < pieceCount; i++) {
			count *= Square.LAST;
		}
		
		return count;
	}

	/**
	 * Calculates table index of given position.
	 * @param position position
	 * @return item index or -1 in some illegal positions
	 */
	public long calculateTableIndex (final Position position) {
		final int whiteKingSquare = position.getKingPosition(Color.WHITE);
		final int blackKingSquare = position.getKingPosition(Color.BLACK);
		
		final int symmetry = symmetryTable.getSymmetry(whiteKingSquare, blackKingSquare);
		
		return calculateTableIndexForSymmetry(position, symmetry);
	}

	/**
	 * Calculates table index of given position using given symmetry.
	 * @param position position
	 * @param symmetry symmetry that should be used
	 * @return item index or -1 in some illegal positions
	 *  or positions with different symmetry
	 */
	private long calculateTableIndexForSymmetry(final Position position, final int symmetry) {
		final int whiteKingSquare = position.getKingPosition(Color.WHITE);
		final int blackKingSquare = position.getKingPosition(Color.BLACK);
		
		final int transformedWhiteKingSquare = SquareSymmetryTable.getItem(symmetry, whiteKingSquare);
		final int transformedBlackKingSquare = SquareSymmetryTable.getItem(symmetry, blackKingSquare);

		final int epFile = position.getEpFile();
		Chunk chunk = null;
		
		if (epFile == File.NONE) {
			final Chunk[] chunkRow = normalChunkArray[transformedWhiteKingSquare];
			
			if (chunkRow != null)
				chunk = chunkRow[transformedBlackKingSquare];
		}
		else {
			final Chunk[][][] chunkRow = epChunkArray[transformedWhiteKingSquare];
			
			if (chunkRow != null) {
				final Chunk[][] chunksForKings = chunkRow[transformedBlackKingSquare];
				
				if (chunksForKings != null) {
					final int onTurn = position.getOnTurn();
					final int notOnTurn = Color.getOppositeColor(onTurn);
					final int transformedEpFile;
					final long prevEpFileMask;
					
					if (symmetry == Symmetry.IDENTITY) {
						transformedEpFile = epFile;
						prevEpFileMask = BoardConstants.getPrevEpFileMask(notOnTurn, epFile);
					}
					else {
						transformedEpFile = File.FH - epFile;
						prevEpFileMask = BoardConstants.getNextEpFileMask(notOnTurn, epFile);
					}
					
					final boolean hasPrevEpFilePawn = (position.getPiecesMask(onTurn, PieceType.PAWN) & prevEpFileMask) != 0;
					
					chunk = chunksForKings[transformedEpFile][Utils.boolToInt(hasPrevEpFilePawn)];
				}
			}
		}
		
		if (chunk != null)
			return chunk.calculateTableIndex (position, symmetry);
		else
			return -1;
	}
	
	/**
	 * Calculates item index from chunk index and combination indices.
	 * @param chunkIndex index of chunk
	 * @param combinationIndices indices of combinations
	 * @return item index or -1 in some illegal positions
	 */
	public long calculateTableIndex (final int chunkIndex, final int[] combinationIndices) {
		final Chunk chunk = getChunkAt(chunkIndex);
		
		return chunk.calculateIndex (combinationIndices);
	}
	
	/**
	 * Calculates all table indices that can represent given position.
	 * Some small number of positions may be represented by more table indices
	 * under different symmetries. 
	 * @param position position to calculate
	 * @param groupIndices array of size MAX_GROUP_SIZE that will be filled with table indices
	 * @return number of different table indices (valid elements of groupIndices array)
	 */
	public int calculateIndexGroup (final Position position, final long[] groupIndices) {
		final int whiteKingSquare = position.getKingPosition(Color.WHITE);
		final int blackKingSquare = position.getKingPosition(Color.BLACK);
		
		if (isPawn) {
			final int symmetry = symmetryTable.getSymmetry(whiteKingSquare, blackKingSquare);
			final long index = calculateTableIndexForSymmetry(position, symmetry);		
			groupIndices[0] = index;
			
			return 1;
		}
		else {
			int count = 0;
			
			for (int symmetry = Symmetry.FIRST; symmetry < Symmetry.LAST; symmetry++) {
				long index = calculateTableIndexForSymmetry(position, symmetry);
				
				for (int i = 0; i < count; i++) {
					if (groupIndices[i] == index) {
						index = -1;
						break;
					}
				}
				
				if (index >= 0) {
					groupIndices[count] = index;
					count++;
				}
			}
			
			return count;
		}
	}

	/**
	 * Returns material hash of material in the table.
	 * @return material hash
	 */
	public MaterialHash getMaterialHash() {
		final MaterialHash hash = new MaterialHash();
		
		for (CombinationDefinition definition: definitionArray) {
			final Piece piece = definition.getPiece();
			final int color = piece.getColor();
			final int pieceType = piece.getPieceType();
			
			hash.addPiece(color, pieceType, definition.getCount());
		}
		
		hash.setOnTurn(onTurn);
		
		return hash;
	}

	/**
	 * Returns color of side on turn.
	 * @return color of side on turn
	 */
	public int getOnTurn() {
		return onTurn;
	}

	/**
	 * Returns count of given pieces in the table.
	 * @param piece piece to check
	 * @return number of pieces
	 */
	public int getGivenPieceCount(final Piece piece) {
		int count = 0;
		
		for (CombinationDefinition definition: definitionArray) {
			if (definition.getPiece() == piece) {
				count += definition.getCount();
			}
		}
		
		return count;
	}

	/**
	 * Checks if given position has same count of pieces as stored in the table.
	 * @param position verified position
	 * @return true if position has same count of pieces, false if not
	 */
	public boolean hasSameCountOfPieces (final Position position) {
		final int pieceCount = BitBoard.getSquareCount(position.getOccupancy());
		
		return getPieceCount() == pieceCount;
	}

	/**
	 * Returns index of chunk containing given table index.
	 * @param tableIndex table index
	 * @return chunk index
	 */
	public int getChunkAtTableIndex(final long tableIndex) {
		for (int i = 0; i < chunkArray.length; i++) {
			if (tableIndex < chunkArray[i].getEndIndex()) {
				return i;
			}
		}
		
		return chunkArray.length;
	}
	
	/**
	 * Checks if EP is possible in this table.
	 * @return true if EP is possible in this table, false if not
	 */
	public boolean isEpPossible() {
		return epPossible;
	}

}
