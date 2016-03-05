package bishop.engine;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.Position;

public final class FinitePositionEvaluator {
	
	private RepeatedPositionRegister repeatedPositionRegister;
	private TablebasePositionEvaluator tablebaseEvaluator;
	private int depthAdvance;
	private int evaluation;
	
	
	public boolean evaluate (final Position position, final int depth, final int horizon, final int alpha, final int beta) {
		// Mate depth pruning
		final int advancedDepth = depth + depthAdvance;
		final int mateEvaluation = Evaluation.getMateEvaluation(advancedDepth);
		
		if (alpha > Evaluation.MATE_MIN && mateEvaluation < alpha) {
			evaluation = mateEvaluation;
			
			return true;
		}
		
		if (beta < -Evaluation.MATE_MIN && -mateEvaluation > beta) {
			evaluation = -mateEvaluation;
			
			return true;
		}
		
		// Repeated positions
		final boolean isRepetition = repeatedPositionRegister.isDrawByRepetition(position, advancedDepth);
		final boolean isDeadPosition = DrawChecker.isDeadPosition(position);

		if (isRepetition || isDeadPosition) {
			evaluation = Evaluation.DRAW;

			return true;
		}
		
		// Tablebase
		if (tablebaseEvaluator != null && (advancedDepth <= 1 || horizon > 0 * ISearchEngine.HORIZON_GRANULARITY)) {
			final MaterialHash materialHash = position.getMaterialHash();
			
			if (tablebaseEvaluator.canEvaluate(materialHash)) {
				final int whiteEvaluation = tablebaseEvaluator.evaluatePosition(position, advancedDepth);
				
				if (position.getOnTurn() == Color.WHITE)
					evaluation = whiteEvaluation;
				else
					evaluation = -whiteEvaluation;
				
				return true;
			}
		}
		
		return false;
	}
		
	public void setRepeatedPositionRegister (final RepeatedPositionRegister register) {
		this.repeatedPositionRegister = register;
	}
	
	public void setDepthAdvance (final int depthAdvance) {
		this.depthAdvance = depthAdvance;
	}
	
	public void setTablebaseEvaluator (final TablebasePositionEvaluator evaluator) {
		this.tablebaseEvaluator = evaluator;
	}
	
	public int getEvaluation() {
		return evaluation;
	}
}
