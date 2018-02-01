package bishop.engine;

import java.io.PrintWriter;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.PieceType;
import bishop.base.Position;

public final class PositionEvaluatorSwitch implements IPositionEvaluator {
	
	private final GeneralPositionEvaluator generalPositionEvaluator;
	private final MatingPositionEvaluator generalMatingEvaluator;
	private final DrawPositionEvaluator drawEvaluator;
	private final PawnStructureCache pawnStructureCache;

	private IMaterialHashRead materialHash;
	
	private final boolean[] hasMatingMaterial;
	
	private IPositionEvaluator currentEvaluator;
	private IPositionEvaluation evaluation;

	
	public PositionEvaluatorSwitch(final PositionEvaluatorSwitchSettings settings, final IPositionEvaluation evaluation) {
		this.evaluation = evaluation;
		
		pawnStructureCache = new PawnStructureCache();
		
		generalPositionEvaluator = new GeneralPositionEvaluator(settings.getGeneralEvaluatorSettings(), pawnStructureCache, evaluation);
		generalMatingEvaluator = new MatingPositionEvaluator(evaluation);
		drawEvaluator = new DrawPositionEvaluator(evaluation);
		
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
	public void evaluate (final Position position, final AttackCalculator attackCalculator) {
		this.materialHash = position.getMaterialHash();
		
		calculateHasMatingMaterial();
		selectCurrentEvaluator();
		
		currentEvaluator.evaluate(position, attackCalculator);
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

	@Override
	public IPositionEvaluation getEvaluation() {
		return evaluation;
	}

}
