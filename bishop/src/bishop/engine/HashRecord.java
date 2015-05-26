package bishop.engine;

public final class HashRecord {

	private int horizon;
	private int evaluation;
	private int type;
	private int compressedBestMove;
	
	
	public int getHorizon() {
		return horizon;
	}
	
	public void setHorizon(int horizon) {
		this.horizon = horizon;
	}
	
	public int getEvaluation() {
		return evaluation;
	}

	public int getNormalizedEvaluation(final int depth) {
		return normalizeMateEvaluation(evaluation, -depth);
	}

	public void setEvaluation(final int evaluation) {
		this.evaluation = evaluation;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(final int type) {
		this.type = type;
	}
	
	public int getCompressedBestMove() {
		return compressedBestMove;
	}

	public void setCompressedBestMove(final int compressedBestMove) {
		this.compressedBestMove = compressedBestMove;
	}
		
	public void setEvaluationAndType (final NodeEvaluation nodeEvaluation, final int currentDepth) {
		evaluation = nodeEvaluation.getEvaluation();
		
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

}
