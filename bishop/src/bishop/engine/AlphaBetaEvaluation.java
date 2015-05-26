package bishop.engine;

public final class AlphaBetaEvaluation {
	
	private int alpha;
	private int beta;
	private int evaluation;
	private int horizon;
	
	public AlphaBetaEvaluation() {
		clear();
	}
	
	public void clear() {
		alpha = Evaluation.MIN;
		beta = Evaluation.MAX;
		alpha = Evaluation.MIN;
		setHorizon(-1);
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(final int alpha) {
		this.alpha = alpha;
	}

	public int getBeta() {
		return beta;
	}

	public void setBeta(final int beta) {
		this.beta = beta;
	}

	public int getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(final int evaluation) {
		this.evaluation = evaluation;
	}

	public void setHorizon(final int horizon) {
		this.horizon = horizon;
	}

	public int getHorizon() {
		return horizon;
	}
	
}
