package bishop.engine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import bishop.base.*;
import utils.IntArrayBuilder;
import utils.IoUtils;

public class TableMaterialEvaluator implements IMaterialEvaluator {

	private static final int PIECE_BITS = 2;
	public static final int MAX_PIECE_COUNT = 1 << PIECE_BITS;
	
	private static final int[][] PIECE_OFFSETS = {
		new IntArrayBuilder(PieceType.LAST, 1)
	        .put(PieceType.KNIGHT, 4)
	        .put(PieceType.BISHOP, 8)
	        .put(PieceType.ROOK, 12)
	        .put(PieceType.QUEEN, 0)
	        .build(),
		new IntArrayBuilder(PieceType.LAST, 1)
	        .put(PieceType.KNIGHT, 10)
	        .put(PieceType.BISHOP, 14)
	        .put(PieceType.ROOK, 2)
	        .put(PieceType.QUEEN, 6)
	        .build()
	};
	
	private static final int COUNT_MASK = (1 << PIECE_BITS) - 1;
	
	// Mask of higher bits of counted figures. If there is 1 bit in the material hash
	// it means that some figure is present more times than possible so the hash is not in the table. 
	private static final long EXCEEDING_MASK = 0xF3CF3CF3CF3CL;
	
	private static final long MAGIC_MASK = 0x0C30C30C30C3L;
	private static final long MAGIC_COEFF = 0x0001000100010000L;
	private static final int MAGIC_SHIFT = 48;
	
	public static final int TABLE_BITS = PIECE_BITS * Color.LAST * PieceType.PROMOTION_FIGURE_COUNT;
	public static final int TABLE_SIZE = 1 << TABLE_BITS;
	
	private final IMaterialEvaluator baseEvaluator;
	private final int[] table = new int[TABLE_SIZE];
	
	public TableMaterialEvaluator (final IMaterialEvaluator baseEvaluator) {
		this.baseEvaluator = baseEvaluator;
	}
	
	@Override
	public int evaluateMaterial(final IMaterialHashRead materialHash) {
		final int index = getIndexForMaterialHash (materialHash);
		
		if (index >= 0) {
			final int pawnDiff = materialHash.getPieceCount(Color.WHITE, PieceType.PAWN) - materialHash.getPieceCount(Color.BLACK, PieceType.PAWN);
			
			return table[index] + PieceTypeEvaluations.PAWN_EVALUATION * pawnDiff;
		}
		else
			return baseEvaluator.evaluateMaterial(materialHash);
	}
	
	public static int getIndexForMaterialHash(final IMaterialHashRead materialHash) {
		final long hash = materialHash.getHash();
		
		if ((hash & EXCEEDING_MASK) != 0)
			return -1;
		
		return (int) (((hash & MAGIC_MASK) * MAGIC_COEFF) >>> MAGIC_SHIFT);
	}
	
	public void read (final InputStream stream) throws IOException {
		for (int i = 0; i < TABLE_SIZE; i++)
			table[i] = (int) IoUtils.readSignedNumberBinary(stream, Evaluation.BYTES);
	}
	
	public void read (final File file) throws IOException {
		try (InputStream stream = new FileInputStream(file)) {
			read (stream);
		}
	}

	public void write (final OutputStream stream) throws IOException {
		for (int i = 0; i < TABLE_SIZE; i++)
			IoUtils.writeNumberBinary(stream, table[i], Evaluation.BYTES);
	}

	public void write (final File file) throws IOException {
		try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
			write (stream);
		}
	}

	public static MaterialHash getMaterialHashForIndex(final int index) {
		final MaterialHash hash = new MaterialHash();
		
		for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final int offset = PIECE_OFFSETS[color][pieceType];
				final int count = (index >>> offset) & COUNT_MASK;
				hash.addPiece(color, pieceType, count);
			}
		}
		
		return hash;
	}

	public void setEvaluationForIndex(final int index, final int evaluation) {
		table[index] = evaluation;
	}

}
