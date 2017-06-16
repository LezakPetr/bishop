package bishop.engine;

import bishop.base.IMaterialHashRead;
import bishop.base.Position;

public final class FinitePositionEvaluator {
	
	private RepeatedPositionRegister repeatedPositionRegister;
	private TablebasePositionEvaluator tablebaseEvaluator;
	private int evaluation;
	
	
	public boolean evaluate (final Position position, final int depth, final int horizon, final int alpha, final int beta) {
		// Mate depth pruning
		final int mateEvaluation = Evaluation.getMateEvaluation(depth);
		
		if (alpha > Evaluation.MATE_MIN && mateEvaluation < alpha) {
			evaluation = mateEvaluation;
			
			return true;
		}
		
		if (beta < -Evaluation.MATE_MIN && -mateEvaluation > beta) {
			evaluation = -mateEvaluation;
			
			return true;
		}
		
		// Repeated positions
		final boolean isRepetition = repeatedPositionRegister.isDrawByRepetition(position, depth);
		final boolean isDeadPosition = DrawChecker.isDeadPosition(position);

		if (isRepetition || isDeadPosition) {
			evaluation = Evaluation.DRAW;

			return true;
		}
		
		// Tablebase
		if (tablebaseEvaluator != null && (depth <= 1 || horizon > 0)) {
			final IMaterialHashRead materialHash = position.getMaterialHash();
			
			if (tablebaseEvaluator.canEvaluate(materialHash)) {
				final int whiteEvaluation = tablebaseEvaluator.evaluatePosition(position, depth);
				evaluation = Evaluation.getRelative(whiteEvaluation, position.getOnTurn());
				
				return true;
			}
		}
		
		return false;
	}
		
	public void setRepeatedPositionRegister (final RepeatedPositionRegister register) {
		this.repeatedPositionRegister = register;
	}
	
	public void setTablebaseEvaluator (final TablebasePositionEvaluator evaluator) {
		this.tablebaseEvaluator = evaluator;
	}
	
	public int getEvaluation() {
		return evaluation;
	}
}
