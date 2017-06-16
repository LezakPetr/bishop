package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.PieceType;
import bishop.base.Position;

public final class PositionEvaluatorSwitch implements IPositionEvaluator {
	
	private final MiddleGamePositionEvaluator middleGameEvaluator;
	private final GeneralMatingPositionEvaluator generalMatingEvaluator;
	private final EndingPositionEvaluator endingEvaluator;
	private final DrawPositionEvaluator drawEvaluator;
	private final PawnStructureCache pawnStructureCache;

	private IMaterialHashRead materialHash;
	
	private final boolean[] hasMatingMaterial;
	
	private IPositionEvaluator currentEvaluator;
	private IPositionEvaluation tacticalEvaluation;
	private IPositionEvaluation positionalEvaluation;

	
	public PositionEvaluatorSwitch(final PositionEvaluatorSwitchSettings settings, final Supplier<IPositionEvaluation> evaluationFactory) {
		pawnStructureCache = new PawnStructureCache();
		
		middleGameEvaluator = new MiddleGamePositionEvaluator(settings.getMiddleGameEvaluatorSettings(), pawnStructureCache, evaluationFactory);
		generalMatingEvaluator = new GeneralMatingPositionEvaluator(evaluationFactory);
		drawEvaluator = new DrawPositionEvaluator(evaluationFactory);
		endingEvaluator = new EndingPositionEvaluator(pawnStructureCache, evaluationFactory);
		
		hasMatingMaterial = new boolean[Color.LAST];
	}
		
	private void selectCurrentEvaluator() {
		currentEvaluator = null;
		
		// Alone king
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int matedColor = Color.getOppositeColor(color);
			
			if (materialHash.isAloneKing(matedColor)) {
				final int pawnCount = materialHash.getPieceCount(color, PieceType.PAWN);
				
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
			count += 4 * materialHash.getPieceCount(color, PieceType.QUEEN);
			count += 2 * materialHash.getPieceCount(color, PieceType.ROOK);
			count += materialHash.getPieceCount(color, PieceType.BISHOP);
			count += materialHash.getPieceCount(color, PieceType.KNIGHT);
		}
		
		return count <= 8;
	}
	
	@Override
	public IPositionEvaluation evaluateTactical (final Position position, final AttackCalculator attackCalculator) {
		this.materialHash = position.getMaterialHash();
		
		calculateHasMatingMaterial();
		selectCurrentEvaluator();
		
		tacticalEvaluation = currentEvaluator.evaluateTactical(position, attackCalculator);
		
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
			hasMatingMaterial[color] = DrawChecker.hasMatingMaterial(materialHash, color);
		}		
	}
		
	public void writeLog (final PrintWriter writer) {
		currentEvaluator.writeLog(writer);
	}

}
