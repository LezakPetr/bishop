package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.Color;
import bishop.base.IPieceCounts;
import bishop.base.PieceType;
import bishop.base.Position;

public final class PositionEvaluatorSwitch implements IPositionEvaluator, IPieceCounts {
	
	private final MiddleGamePositionEvaluator middleGameEvaluator;
	private final GeneralMatingPositionEvaluator generalMatingEvaluator;
	private final EndingPositionEvaluator endingEvaluator;
	private final DrawPositionEvaluator drawEvaluator;
	private final PawnStructureCache pawnStructureCache;
	
	private final int[] colorCounts;
	private final int[][] pieceCounts;
	
	private final boolean[] hasMatingMaterial;
	
	private Position position;
	private IPositionEvaluator currentEvaluator;
	private IPositionEvaluation tacticalEvaluation;
	private IPositionEvaluation positionalEvaluation;

	
	public PositionEvaluatorSwitch(final PositionEvaluatorSwitchSettings settings, final Supplier<IPositionEvaluation> evaluationFactory) {
		pawnStructureCache = new PawnStructureCache();
		
		middleGameEvaluator = new MiddleGamePositionEvaluator(settings.getMiddleGameEvaluatorSettings(), pawnStructureCache, evaluationFactory);
		generalMatingEvaluator = new GeneralMatingPositionEvaluator(evaluationFactory);
		drawEvaluator = new DrawPositionEvaluator(evaluationFactory);
		endingEvaluator = new EndingPositionEvaluator(pawnStructureCache, evaluationFactory);
		
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
	
	@Override
	public IPositionEvaluation evaluateTactical (final Position position, final AttackCalculator attackCalculator) {
		this.position = position;
		
		calculatePieceCounts();
		calculateHasMatingMaterial();
		selectCurrentEvaluator();
		
		tacticalEvaluation = currentEvaluator.evaluateTactical(position, attackCalculator);
		this.position = null;
		
		return tacticalEvaluation;
	}

	@Override
	public IPositionEvaluation evaluatePositional (final AttackCalculator attackCalculator) {
		positionalEvaluation = currentEvaluator.evaluatePositional(attackCalculator);
		
		return positionalEvaluation;
	}
	
	@Override
	public int getMaterialEvaluationShift() {
		return currentEvaluator.getMaterialEvaluationShift();
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
