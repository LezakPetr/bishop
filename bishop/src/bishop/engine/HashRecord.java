package bishop.engine;

import bishop.base.Move;

public final class HashRecord {

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
		
	public void setEvaluationAndType (final NodeEvaluation nodeEvaluation, final int currentDepth) {
		evaluation = nodeEvaluation.getEvaluation();
		assert (evaluation >= Evaluation.MIN && evaluation <= Evaluation.MAX);
		
		if (evaluation > nodeEvaluation.getBeta())
			type = HashRecordType.LOWER_BOUND;
		else {
			if (evaluation < nodeEvaluation.getAlpha())
				type = HashRecordType.UPPER_BOUND;
			else
				type = HashRecordType.VALUE;
		}
		
		// Normalize mate evaluation to current position
		evaluation = normalizeMateEvaluation (evaluation, currentDepth);
	}
	
	private static final int normalizeMateEvaluation(final int evaluation, final int currentDepth) {
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
}
