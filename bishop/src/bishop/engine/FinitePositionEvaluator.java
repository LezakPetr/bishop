package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;

public final class FinitePositionEvaluator {
	
	private RepeatedPositionRegister repeatedPositionRegister;
	private TablebasePositionEvaluator tablebaseEvaluator;
	private PawnEndingTableRegister pawnEndingTableRegister = new PawnEndingTableRegister(null);
	private PieceTypeEvaluations pieceTypeEvaluations;
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
		final boolean isDeadPosition = DrawChecker.isDeadPosition(position);

		if (isDeadPosition) {
			evaluation = Evaluation.DRAW;

			return true;
		}

		final boolean isRepetition = repeatedPositionRegister.isDrawByRepetition(position, depth);

		if (isRepetition) {
			evaluation = Evaluation.DRAW_BY_REPETITION;

			return true;
		}
		
		// Tablebase
		if (tablebaseEvaluator != null && (depth <= 1 || horizon > 0)) {
			final int tablebaseEvaluation = tablebaseEvaluator.evaluatePosition(position, depth);
			
			if (tablebaseEvaluation != Evaluation.UNKNOWN) {
				evaluation = tablebaseEvaluation;
				
				return true;
			}
		}

		// Pawn ending
		if (depth > 3 && horizon > 3 * ISearchEngine.HORIZON_GRANULARITY && !position.getMaterialHash().hasFigure()) {
			final PawnEndingKey key = new PawnEndingKey(
					position.getPiecesMask(Color.WHITE, PieceType.PAWN),
					position.getPiecesMask(Color.BLACK, PieceType.PAWN)
			);

			if (key.estimateComplexity() < 300) {
				final PawnEndingTable table = pawnEndingTableRegister.getTable(key);
				final int onTurn = position.getOnTurn();
				final int kingOnTurnSquare = position.getKingPosition(onTurn);
				final int kingNotOnTurnSquare = position.getKingPosition(Color.getOppositeColor(onTurn));
				final int classification = table.getClassification(kingOnTurnSquare, kingNotOnTurnSquare, onTurn);

				switch (classification) {
					case Classification.WIN:
						evaluation = pieceTypeEvaluations.getPieceTypeEvaluation(PieceType.QUEEN);
						return true;

					case Classification.LOSE:
						evaluation = -pieceTypeEvaluations.getPieceTypeEvaluation(PieceType.QUEEN);
						return true;

					case Classification.DRAW:
						evaluation = Evaluation.DRAW;
						return true;
				}
			}
		}
		
		return false;
	}
		
	public void setRepeatedPositionRegister (final RepeatedPositionRegister register) {
		this.repeatedPositionRegister = register;
	}
	
	public void setTablebaseEvaluator (final TablebasePositionEvaluator evaluator) {
		this.tablebaseEvaluator = evaluator;

		pawnEndingTableRegister = new PawnEndingTableRegister(tablebaseEvaluator);
	}

	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations) {
		this.pieceTypeEvaluations = pieceTypeEvaluations;
	}

	public int getEvaluation() {
		return evaluation;
	}
}
