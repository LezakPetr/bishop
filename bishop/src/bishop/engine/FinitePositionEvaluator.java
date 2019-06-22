package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;

public final class FinitePositionEvaluator {

	private static final int MAX_PAWN_ENDING_EVALUATOR_COMPLEXITY = 500;
	private static final int MIN_PAWN_ENDING_EVALUATOR_HORIZON = 6;
	private static final int MIN_PAWN_EVALUATOR_DEPTH = 1;

	private RepeatedPositionRegister repeatedPositionRegister;
	private TablebasePositionEvaluator tablebaseEvaluator;
	private PawnEndingTableRegister pawnEndingTableRegister = new PawnEndingTableRegister();
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

		// Alone king
		final int onTurn = position.getOnTurn();
		final IMaterialHashRead materialHash = position.getMaterialHash();

		if (materialHash.isAloneKing(onTurn) && alpha > Evaluation.DRAW) {
			evaluation = Evaluation.DRAW;
			return true;
		}

		final int oppositeColor = Color.getOppositeColor(onTurn);

		if (materialHash.isAloneKing(oppositeColor) && beta < Evaluation.DRAW) {
			evaluation = Evaluation.DRAW;
			return true;
		}

		// Dead positions
		final boolean isDeadPosition = DrawChecker.isDeadPosition(position);

		if (isDeadPosition) {
			evaluation = Evaluation.DRAW;

			return true;
		}

		// Repeated positions
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
		if (depth > MIN_PAWN_EVALUATOR_DEPTH && horizon > MIN_PAWN_ENDING_EVALUATOR_HORIZON && !materialHash.hasFigure()) {
			final PawnEndingKey key = new PawnEndingKey(
					position.getPiecesMask(Color.WHITE, PieceType.PAWN),
					position.getPiecesMask(Color.BLACK, PieceType.PAWN)
			);

			if (key.estimateComplexity() < MAX_PAWN_ENDING_EVALUATOR_COMPLEXITY) {
				final PawnEndingTable table = pawnEndingTableRegister.getTable(key);
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

		pawnEndingTableRegister = new PawnEndingTableRegister();
	}

	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations) {
		this.pieceTypeEvaluations = pieceTypeEvaluations;
	}

	public int getEvaluation() {
		return evaluation;
	}
}
