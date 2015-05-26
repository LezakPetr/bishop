package bishop.tablebaseGenerator;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.interpreter.Bytecode;
import bishop.interpreter.OperatorRecord;
import bishop.interpreter.Operators;

public class BytecodeGenerator {
	
	
	private static class DistRecord {
		public DistRecord(final Piece from, final Piece to) {
			this.pieces = new Piece[] { from, to };
		}
		
		public final Piece[] pieces;
	}
	
	private int prefixMultiplier;
	private String prefix;
	
	private final MaterialHash materialHash;
	private final Piece[] pieces;
	private final DistRecord[] distRecords;
	private long maxCombinations;
	
	private static final byte[] VARIATION_OPCODES = {
		-1,
		Operators.OPCODE_MIN_EDGE_DIST,
		Operators.OPCODE_MIN_RANK,
		Operators.OPCODE_FIRST_SQUARE,
		Operators.OPCODE_MAX_RANK,
		Operators.OPCODE_LAST_SQUARE
	};
	
	public static final int ITEM_NONE = 0;
	public static final int DIST_ITEM = 1;
	public static final int PIECE_ITEM_MIN_EDGE_DIST = 1;
	public static final int PIECE_ITEM_MIN_RANK = 2;
	public static final int PIECE_ITEM_FIRST_SQUARE = 3;
	public static final int PIECE_ITEM_MAX_RANK = 4;
	public static final int PIECE_ITEM_LAST_SQUARE = 5;
	
	private static final int DIST_VARIATION_COUNT = 2;
	private static final int DIST_VARIATION_MULTIPLIER = 6;
	
	private static final int[] VARIATION_MULTIPLIERS = {
		1, 3, 8, 64, 8, 64
	};
	
	public BytecodeGenerator(final MaterialHash hash) {
		this.materialHash = hash;
		this.pieces = initializePieces();
		this.distRecords = createDistRecords();
		
		calculatePrefix();
		
		// Max combinations
		maxCombinations = 2300;
	}
	
	private void calculatePrefix() {
		final int[] bishopCount = new int[Color.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			bishopCount[color] =  materialHash.getPieceCount(color, PieceType.BISHOP);			
		}
		
		final StringBuilder builder = new StringBuilder();
		prefixMultiplier = 1;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			if (bishopCount[color] >= 2) {
				final Piece piece = Piece.withColorAndType(color, PieceType.BISHOP);
				
				for (int squareColor = Color.FIRST; squareColor < Color.LAST; squareColor++) {
					writePiece (builder, piece);
					
					builder.append(BoardConstants.getSquareColorMask(squareColor));
					builder.append(' ');
					writeOperator (builder, Operators.OPCODE_AND);
					builder.append(0);
					builder.append(' ');
					writeOperator (builder, Operators.OPCODE_NOT_EQUAL);
				}
				
				writeOperator (builder, Operators.OPCODE_XOR);
				appendSum (builder, prefixMultiplier);
				
				prefixMultiplier *= 10;
			}
		}
		
		if (bishopCount[Color.WHITE] == 1 && bishopCount[Color.BLACK] == 1) {
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final Piece piece = Piece.withColorAndType(color, PieceType.BISHOP);
				
				writePiece (builder, piece);
				
				builder.append(BoardConstants.WHITE_SQUARE_MASK);
				builder.append(' ');
				writeOperator (builder, Operators.OPCODE_AND);
				builder.append(0);
				builder.append(' ');
				writeOperator (builder, Operators.OPCODE_NOT_EQUAL);
			}
			
			writeOperator (builder, Operators.OPCODE_XOR);
			appendSum (builder, prefixMultiplier);
			
			prefixMultiplier *= 10;
		}
		
		prefix = builder.toString();
	}
	
	private DistRecord[] createDistRecords() {
		final List<DistRecord> distRecordList = new LinkedList<DistRecord>();
		
		for (int fromIndex = 0; fromIndex < pieces.length; fromIndex++) {
			for (int toIndex = fromIndex + 1; toIndex < pieces.length; toIndex++) {
				distRecordList.add(new DistRecord(pieces[fromIndex], pieces[toIndex]));
			}
		}
		
		return distRecordList.toArray(new DistRecord[distRecordList.size()]);
	}

	private Piece[] initializePieces() {
		final List<Piece> pieceList = new LinkedList<Piece>();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				if (materialHash.getPieceCount(color, pieceType) > 0)
					pieceList.add(Piece.withColorAndType(color, pieceType));
			}
		}
		
		return pieceList.toArray(new Piece[pieceList.size()]);
	}
	
	public int getItemCount() {
		return pieces.length + distRecords.length;
	}
	
	public int getItemVariants(final int itemIndex) {
		if (itemIndex >= pieces.length)
			return DIST_VARIATION_COUNT;
		else {
			final Piece piece = pieces[itemIndex];
			final int pieceCount = materialHash.getPieceCount(piece.getColor(), piece.getPieceType());
			
			return (pieceCount > 1) ? 6 : 4;
		}
	}

	public Bytecode getBytecode(final int[] items) {
		final StringBuilder builder = new StringBuilder();
		builder.append(prefix);
		
		long multiplier = prefixMultiplier;
		
		for (int i = 0; i < pieces.length; i++) {
			final Piece piece = pieces[i];
			final int item = items[i];
			
			multiplier = writePieceExpression (builder, piece, item, multiplier);
		}
		
		for (int i = 0; i < distRecords.length; i++) {
			final DistRecord distRecord = distRecords[i];
			final int item = items[pieces.length + i];
			
			multiplier = writeDistExpression (builder, distRecord, item, multiplier);
		}
		
		if (multiplier == 1) {
			builder.append("0");
		}
		
		System.out.println (builder.toString());
		final PushbackReader reader = new PushbackReader(new StringReader(builder.toString()));
		
		try {
			return Bytecode.parse(reader);
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot parse bytecode", ex);
		}
	}

	private long writePieceExpression(final StringBuilder builder, final Piece piece, final int item, final long multiplier) {
		if (item > 0) {
			writePiece(builder, piece);
			
			final byte opcode = VARIATION_OPCODES[item];
			final OperatorRecord record = Operators.getRecordForOpcode(opcode);
			builder.append(record.getToken());
			
			builder.append(' ');
			appendSum(builder, multiplier);
			
			return multiplier * 100;
		}
		else
			return multiplier;
	}
	
	private static void writeOperator (final StringBuilder builder, final byte opcode) {
		builder.append(Operators.getRecordForOpcode(opcode).getToken());
		builder.append(' ');
	}

	private static void writePiece(final StringBuilder builder, final Piece piece) {
		builder.append(Color.getName(piece.getColor()));
		builder.append(' ');
		builder.append(PieceType.getName(piece.getPieceType()));
		builder.append(' ');
		writeOperator (builder, Operators.OPCODE_PIECES_MASK);
	}

	private void appendSum(final StringBuilder builder, final long multiplier) {
		if (multiplier > 1) {
			builder.append(multiplier);
			builder.append(" * + ");
		}
	}
	
	private long writeDistExpression(final StringBuilder builder, final DistRecord distRecord, final int item, final long multiplier) {
		if (item > 0) {
			for (Piece piece: distRecord.pieces)
				writePiece(builder, piece);

			writeOperator (builder, Operators.OPCODE_MIN_DIST);
			appendSum(builder, multiplier);
			
			return multiplier * 10;
		}
		else
			return multiplier;
	}
	
	public boolean isCorrectCombination (final int[] items) {
		long combinationCount = 1;
		
		for (int i = 0; i < pieces.length; i++) {
			final int item = items[i];
			combinationCount *= VARIATION_MULTIPLIERS[item];
		}
		
		for (int i = 0; i < distRecords.length; i++) {
			final int item = items[pieces.length + i];
			
			if (item != ITEM_NONE)
				combinationCount *= DIST_VARIATION_MULTIPLIER;
		}
		
		return combinationCount <= maxCombinations;
	}

	/**
	 * Returns array of items that can be reached from current item by incremental update.
	 * @param itemIndex item index
	 * @param item current item
	 * @return list of successors
	 */
	public List<Integer> getSuccessorsItems(final int itemIndex, final int item) {
		final List<Integer> successors = new LinkedList<Integer>();
		
		if (itemIndex >= pieces.length) {
			if (item == ITEM_NONE)
				successors.add(DIST_ITEM);
		}
		else {
			final Piece piece = pieces[itemIndex];
			final int pieceCount = materialHash.getPieceCount(piece.getColor(), piece.getPieceType());
			
			successors.add(PIECE_ITEM_MIN_EDGE_DIST);
			successors.add(PIECE_ITEM_MIN_RANK);
			
			if (pieceCount > 1) {
				successors.add(PIECE_ITEM_MAX_RANK);
				
				if (item != ITEM_NONE) {
					successors.add(PIECE_ITEM_FIRST_SQUARE);
					successors.add(PIECE_ITEM_LAST_SQUARE);
				}
			}
			else {
				if (item != ITEM_NONE) {
					successors.add(PIECE_ITEM_FIRST_SQUARE);
				}
			}
		}
		
		successors.remove((Integer) item);
		
		return successors;
	}
}
