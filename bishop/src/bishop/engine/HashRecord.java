package bishop.engine;

import bishop.base.Move;
import bishop.base.PieceTypeEvaluations;

public final class HashRecord {
	
	public static final int MAX_NORMAL_EVALUATION = 20 * PieceTypeEvaluations.PAWN_EVALUATION;
	public static final int MIN_NORMAL_EVALUATION = -MAX_NORMAL_EVALUATION;

	private int horizon;
	private int evaluation;
	private int type;
	private int compressedBestMove;
	
	
	public int getHorizon() {
		return horizon;
	}
	
	public void setHorizon(final int horizon) {
		assert horizon >= 0;
		
		this.horizon = horizon;
	}
	
	public int getEvaluation() {
		return evaluation;
	}

	public int getNormalizedEvaluation(final int depth) {
		return normalizeMateEvaluation(evaluation, -depth);
	}

	public void setEvaluation(final int evaluation) {
		assert (evaluation >= Evaluation.MIN && evaluation <= Evaluation.MAX);
		
		this.evaluation = evaluation;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(final int type) {
		assert (type >= HashRecordType.FIRST && type < HashRecordType.LAST);
		
		this.type = type;
	}
	
	public int getCompressedBestMove() {
		return compressedBestMove;
	}

	public void setCompressedBestMove(final int compressedBestMove) {
		assert (compressedBestMove >= Move.FIRST_COMPRESSED_MOVE && compressedBestMove < Move.LAST_COMPRESSED_MOVE);
		
		this.compressedBestMove = compressedBestMove;
	}
		
	public void setEvaluationAndType (final int evaluation, final int alpha, final int beta, final int currentDepth) {
		assert (evaluation >= Evaluation.MIN && evaluation <= Evaluation.MAX);
		
		if (evaluation > beta)
			type = HashRecordType.LOWER_BOUND;
		else {
			if (evaluation < alpha)
				type = HashRecordType.UPPER_BOUND;
			else
				type = HashRecordType.VALUE;
		}
		
		// Normalize mate evaluation to current position
		this.evaluation = normalizeMateEvaluation (evaluation, currentDepth);
	}
	
	private static int normalizeMateEvaluation(final int evaluation, final int currentDepth) {
		if (evaluation > Evaluation.MATE_MIN)
			return evaluation + currentDepth;

		if (evaluation < -Evaluation.MATE_MIN)
			return evaluation - currentDepth;
		
		return evaluation;
	}

	public boolean equals(final Object obj) {
		if (!(obj instanceof HashRecord))
			return false;
		
		final HashRecord cmp = (HashRecord) obj;
		
		return this.horizon == cmp.horizon && this.evaluation == cmp.evaluation && this.compressedBestMove == cmp.compressedBestMove && this.type == cmp.type;
	}
	
	@Override
	public String toString() {
		return "Horizon = " + horizon + ", evaluation = " + evaluation + ", bestMove = " + compressedBestMove + ", type = " + type;
	}

	public boolean canBeStored() {
		if (type == HashRecordType.LAST)
			return false;
		
		return (evaluation >= MIN_NORMAL_EVALUATION && evaluation <= MAX_NORMAL_EVALUATION) ||
				evaluation >= Evaluation.MATE_MIN ||
				evaluation <= -Evaluation.MATE_MIN;
	}

	public void assign(final HashRecord orig) {
		this.horizon = orig.horizon;
		this.evaluation = orig.evaluation;
		this.type = orig.type;
		this.compressedBestMove = orig.compressedBestMove;
	}

	public void clear() {
		this.horizon = 0;
		this.evaluation = 0;
		this.type = HashRecordType.INVALID;
		this.compressedBestMove = Move.NONE_COMPRESSED_MOVE;
	}
	
	public boolean isBetterThan (final HashRecord that, final int expectedHorizon) {
		// Compare by validity
		if (that.type == HashRecordType.INVALID)
			return true;
		
		if (this.type == HashRecordType.INVALID)
			return false;

		// Different horizons
		if (this.horizon != that.horizon) {
			if (this.horizon == expectedHorizon)
				return false;

			if (that.horizon == expectedHorizon)
				return true;

			return this.horizon > that.horizon;
		}

		// Same horizons
		return this.type == HashRecordType.VALUE;
	}
}
