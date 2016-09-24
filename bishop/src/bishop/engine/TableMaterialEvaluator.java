package bishop.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import bishop.base.Color;
import bishop.base.IMaterialEvaluator;
import bishop.base.IPieceCounts;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import utils.IntArrayBuilder;
import utils.IoUtils;

public class TableMaterialEvaluator implements IMaterialEvaluator {

	private static final int[] PIECE_MULTIPLIERS = new IntArrayBuilder(PieceType.LAST)
	        .put(PieceType.KING, 1)
	        .put(PieceType.KNIGHT, 3)
	        .put(PieceType.BISHOP, 3)
	        .put(PieceType.ROOK, 3)
	        .put(PieceType.QUEEN, 2)
	        .build();
	
	private static final int TABLE_SIZE = Arrays.stream(PIECE_MULTIPLIERS).map((x) -> x*x).reduce(1, (x, y) -> x*y);
	
	private final IMaterialEvaluator baseEvaluator;
	private final short[] table = new short[TABLE_SIZE];
	
	public TableMaterialEvaluator (final TableMaterialEvaluator baseEvaluator) {
		this.baseEvaluator = baseEvaluator;
	}
	
	@Override
	public int evaluateMaterial(final IPieceCounts pieceCounts) {
		final int index = calculateIndex (pieceCounts);
		
		if (index >= 0) {
			final int pawnDiff = pieceCounts.getPieceCount(Color.WHITE, PieceType.PAWN) - pieceCounts.getPieceCount(Color.BLACK, PieceType.PAWN);
			
			return table[index] + PieceTypeEvaluations.PAWN_EVALUATION * pawnDiff;
		}
		else
			return baseEvaluator.evaluateMaterial(pieceCounts);
	}

	private int calculateIndex(final IPieceCounts pieceCounts) {
		int index = 0;
		
		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
			final int multiplier = PIECE_MULTIPLIERS[pieceType];
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final int count = pieceCounts.getPieceCount(color, pieceType);
				
				index = index * multiplier + count;
			}
		}
		
		return index;
	}
	
	public void read (final InputStream stream) throws IOException {
		for (int i = 0; i < TABLE_SIZE; i++)
			table[i] = (short) IoUtils.readSignedNumberBinary(stream, IoUtils.SHORT_BYTES);
	}
	
	public void read (final File file) throws IOException {
		try (InputStream stream = new FileInputStream(file)) {
			read (stream);
		}
	}

	public void write (final OutputStream stream) throws IOException {
		for (int i = 0; i < TABLE_SIZE; i++)
			IoUtils.writeNumberBinary(stream, table[i], IoUtils.SHORT_BYTES);
	}

	public void write (final File file) throws IOException {
		try (OutputStream stream = new FileOutputStream(file)) {
			write (stream);
		}
	}

}
