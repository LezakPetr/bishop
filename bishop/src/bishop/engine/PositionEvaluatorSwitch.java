package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.PieceType;
import bishop.base.Position;

public final class PositionEvaluatorSwitch implements IPositionEvaluator {
	
	private final GeneralPositionEvaluator generalPositionEvaluator;
	private final MatingPositionEvaluator generalMatingEvaluator;
	private final DrawPositionEvaluator drawEvaluator;

	private IMaterialHashRead materialHash;
	
	private final boolean[] hasMatingMaterial;
	
	private IPositionEvaluator currentEvaluator;
	private IPositionEvaluation tacticalEvaluation;
	private IPositionEvaluation positionalEvaluation;

	
	public PositionEvaluatorSwitch(final PositionEvaluatorSwitchSettings settings, final Supplier<IPositionEvaluation> evaluationFactory) {
		generalPositionEvaluator = new GeneralPositionEvaluator(settings.getGeneralEvaluatorSettings(), evaluationFactory);
		generalMatingEvaluator = new MatingPositionEvaluator(evaluationFactory);
		drawEvaluator = new DrawPositionEvaluator(evaluationFactory);
		
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
				
				currentEvaluator = generalPositionEvaluator;
				return;
			}
		}
		
		if (!hasMatingMaterial[Color.WHITE] && !hasMatingMaterial[Color.BLACK]) {
			currentEvaluator = drawEvaluator;
			return;
		}
		
		currentEvaluator = generalPositionEvaluator;
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
