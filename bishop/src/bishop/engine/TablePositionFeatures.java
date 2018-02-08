package bishop.engine;

import java.util.Arrays;

import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Square;
import math.EquationSystemSolver;

public class TablePositionFeatures {
	
	private static final int PIECE_TYPE_SHIFT = 6;
	private static final int COLOR_SHIFT = 9;
	private static final int TOTAL_BITS = 1 + 3 + 6;
	
	private final short[] featureIndices;
	
	private static int getIndex (final int color, final int pieceType, final int square) {
		return (color << COLOR_SHIFT) + (pieceType << PIECE_TYPE_SHIFT) + square;
	}

	public TablePositionFeatures(final FeatureRegistry registry) {
		featureIndices = new short[1 << TOTAL_BITS];
		fillCoeffIndices(registry);
	}
	

	private void fillCoeffIndices(final FeatureRegistry registry) {
		registry.enterCategory("table_position");
		
		Arrays.fill(featureIndices, (short) -1);
		
		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
			final long board = BoardConstants.getPieceAllowedSquares(pieceType);
			
			for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
				final int whiteSquare = loop.getNextSquare();
				final String coeffName = PieceType.toChar(pieceType, false) + "_" + Square.toString(whiteSquare);
				final short coeff = registry.add(coeffName);
				final int whiteIndex = getIndex(Color.WHITE, pieceType, whiteSquare);
				featureIndices[whiteIndex] = coeff;
				
				final int blackSquare = Square.getOppositeSquare(whiteSquare);
				final int blackndex = getIndex(Color.BLACK, pieceType, blackSquare);
				featureIndices[blackndex] = coeff;
			}
		}
		
		registry.leaveCategory();
	}

	public int getCoeff(final int color, final int pieceType, final int square) {
		final int index = getIndex(color, pieceType, square);
		
		return featureIndices[index];
	}

	public void addZeroSumEquation(final EquationSystemSolver equationSolver) {
		final double[] rightSides = new double[equationSolver.getRightSideCount()];
		
		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
			final long board = BoardConstants.getPieceAllowedSquares(pieceType);
			final double[] equationCoeffs = new double[PositionEvaluationFeatures.LAST];
			
			for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
				final int square = loop.getNextSquare();
				final int feature = getCoeff(Color.WHITE, pieceType, square);
				
				equationCoeffs[feature] = 1.0;
			}
			
			equationSolver.addEquation(equationCoeffs, rightSides, 1.0);
		}
	}

}
