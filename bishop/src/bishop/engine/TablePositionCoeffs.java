package bishop.engine;

import java.util.Arrays;
import java.util.stream.IntStream;

import bishop.base.*;
import math.IVector;
import math.Vectors;

public class TablePositionCoeffs {
	
	private static final int PIECE_TYPE_SHIFT = 6;
	private static final int COLOR_SHIFT = 9;
	private static final int TOTAL_BITS = 1 + 3 + 6;

	private static final int RANK_FEATURE_COUNT = 4;
	private static final int FILE_FEATURE_COUNT = 5;

	private final int[] coeffIndices;
	
	private static int getIndex (final int color, final int pieceType, final int square) {
		return (color << COLOR_SHIFT) + (pieceType << PIECE_TYPE_SHIFT) + square;
	}

	public TablePositionCoeffs(final CoeffRegistry registry) {
		coeffIndices = new int[1 << TOTAL_BITS];
		fillCoeffIndices(registry);
	}
	
	private void fillCoeffIndices(final CoeffRegistry registry) {
		registry.enterCategory("table_position");
		
		Arrays.fill(coeffIndices, (short) -1);
		
		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
			final long board = BoardConstants.getPieceAllowedSquares(pieceType);

			// Features will be coefficients of 2D polynomial with zero in the centre of the board
			final int[][] featureIndices = new int[FILE_FEATURE_COUNT][RANK_FEATURE_COUNT];

			for (int i = 0; i < FILE_FEATURE_COUNT; i++) {
				for (int j = 0; j < RANK_FEATURE_COUNT; j++) {
					featureIndices[i][j] = registry.addFeature();
				}
			}

			for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
				final int whiteSquare = loop.getNextSquare();
				final int file = Square.getFile(whiteSquare);
				final int rank = Square.getRank(whiteSquare);

				final double t = (file - File.MIDDLE) / File.MIDDLE;
				final double s = (rank - Rank.MIDDLE) / Rank.MIDDLE;

				final IVector features = Vectors.sparse(registry.getFeatureCount());

				for (int i = 0; i < FILE_FEATURE_COUNT; i++) {
					for (int j = 0; j < RANK_FEATURE_COUNT; j++) {
						features.setElement(
								featureIndices[i][j],
								Math.pow(t, i) * Math.pow(s, j)
						);
					}
				}

				final String coeffName = PieceType.toChar(pieceType, false) + "_" + Square.toString(whiteSquare);
				final int coeff = registry.addCoeffWithFeatures(coeffName, features.freeze());
				final int whiteIndex = getIndex(Color.WHITE, pieceType, whiteSquare);
				coeffIndices[whiteIndex] = coeff;
				
				final int blackSquare = Square.getOppositeSquare(whiteSquare);
				final int blackndex = getIndex(Color.BLACK, pieceType, blackSquare);
				coeffIndices[blackndex] = coeff;
			}
		}
		
		registry.leaveCategory();
	}

	public int getCoeff(final int color, final int pieceType, final int square) {
		final int index = getIndex(color, pieceType, square);
		
		return coeffIndices[index];
	}


}
