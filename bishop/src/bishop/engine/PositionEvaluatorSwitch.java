package bishop.engine;

import java.io.PrintWriter;

import parallel.Parallel;
import bishop.base.Color;
import bishop.base.IMaterialEvaluator;
import bishop.base.IPieceCounts;
import bishop.base.PieceType;
import bishop.base.Position;

public final class PositionEvaluatorSwitch implements IPositionEvaluator, IPieceCounts {
	
	private final MiddleGamePositionEvaluator middleGameEvaluator;
	private final GeneralMatingPositionEvaluator generalMatingEvaluator;
	private final EndingPositionEvaluator endingEvaluator;
	private final DrawPositionEvaluator drawEvaluator;
	
	private final int[] colorCounts;
	private final int[][] pieceCounts;
	
	private final boolean[] hasMatingMaterial;
	
	private Position position;
	private IPositionEvaluator currentEvaluator;
	private int evaluation;

	
	public PositionEvaluatorSwitch(final PositionEvaluatorSwitchSettings settings, final IMaterialEvaluator materialEvaluator) {
		middleGameEvaluator = new MiddleGamePositionEvaluator(settings.getMiddleGameEvaluatorSettings(), materialEvaluator);
		generalMatingEvaluator = new GeneralMatingPositionEvaluator(materialEvaluator);
		drawEvaluator = new DrawPositionEvaluator(materialEvaluator);
		endingEvaluator = new EndingPositionEvaluator(settings.getEndingPositionEvaluatorSettings(), materialEvaluator);
		
		colorCounts = new int[Color.LAST];
		pieceCounts = new int[Color.LAST][PieceType.LAST];
		hasMatingMaterial = new boolean[Color.LAST];
	}
	
	private void calculatePieceCounts() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			int currentColorCount = 0;
			
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final int count = position.getPieceCount(color, pieceType);
				
				pieceCounts[color][pieceType] = count;
				currentColorCount += count;
			}
			
			colorCounts[color] = currentColorCount;
		}
	}
	
	private void selectCurrentEvaluator() {
		currentEvaluator = null;
		
		// Alone king
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int matedColor = Color.getOppositeColor(color);
			
			if (colorCounts[matedColor] == 1) {
				final int pawnCount = pieceCounts[color][PieceType.PAWN];
				
				if (pawnCount == 0 && hasMatingMaterial[color]) {
					currentEvaluator = generalMatingEvaluator;
					return;
				}
				
				currentEvaluator = endingEvaluator;
				return;
			}
		}
		
		if (!hasMatingMaterial[Color.WHITE] && !hasMatingMaterial[Color.BLACK]) {
			currentEvaluator = drawEvaluator;
			return;
		}
		
		if (isEnding())
			currentEvaluator = endingEvaluator;
		else
			currentEvaluator = middleGameEvaluator;
	}
	
	private boolean isEnding() {
		int count = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int[] colorPieceCounts = pieceCounts[color];
			
			count += 4*colorPieceCounts[PieceType.QUEEN];
			count += 2*colorPieceCounts[PieceType.ROOK];
			count += colorPieceCounts[PieceType.BISHOP];
			count += colorPieceCounts[PieceType.KNIGHT];
		}
		
		return count <= 8;
	}
	
	/**
	 * Returns evaluation of given position.
	 * @param position position to evaluate
	 * @return evaluation from view of white side
	 */
	public int evaluatePosition (final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		this.position = position;
		
		calculatePieceCounts();
		calculateHasMatingMaterial();
		selectCurrentEvaluator();
		
		evaluation = currentEvaluator.evaluatePosition(position, alpha, beta, attackCalculator);
		this.position = null;
		
		return evaluation;
	}

	private void calculateHasMatingMaterial() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			hasMatingMaterial[color] = DrawChecker.hasMatingMaterial(this, color);
		}		
	}
		
	public void writeLog (final PrintWriter writer) {
		currentEvaluator.writeLog(writer);
	}

	@Override
	public int getPieceCount(final int color, final int pieceType) {
		return pieceCounts[color][pieceType];
	}
}
