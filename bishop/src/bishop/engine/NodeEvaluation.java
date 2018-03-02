package bishop.engine;

public final class NodeEvaluation {
	
	private int evaluation;
	private int alpha;
	private int beta;
		
	
	public NodeEvaluation() {
		clear();
	}
	
	public void clear() {
		evaluation = Evaluation.MIN;
		alpha = Evaluation.MIN;
		beta = Evaluation.MAX;
	}

	/**
	 * Returns evaluation of the node.
	 * @return evaluation of the node
	 */
	public int getEvaluation() {
		return evaluation;
	}

	/**
	 * Sets evaluation of the node.
	 * @param evaluation evaluation of the node
	 */
	public void setEvaluation(final int evaluation) {
		this.evaluation = evaluation;
	}

	public NodeEvaluation copy() {
		final NodeEvaluation result = new NodeEvaluation();
		result.assign (this);
		
		return result;
	}

	/**
	 * Assigns content of given node evaluation into this node evaluation.
	 * @param orig original node evaluation
	 */
	public void assign(final NodeEvaluation orig) {
		this.evaluation = orig.evaluation;
		this.alpha = orig.alpha;
		this.beta = orig.beta;
	}

	/**
	 * Assigns opposite content of given node evaluation into this node evaluation.
	 * @param orig original node evaluation
	 */
	public void assignParent(final NodeEvaluation orig) {
		final int origAlpha = orig.alpha;
		final int origBeta = orig.beta;
			
		this.alpha = -origBeta;
		this.beta = -origAlpha;
		this.evaluation = -orig.evaluation;
	}
	
	/**
	 * Returns opposite evaluation.
	 * @return evaluation from view of opposite side
	 */
	public NodeEvaluation getParent() {
		final NodeEvaluation opposite = new NodeEvaluation();
		opposite.assignParent(this);
		
		return opposite;
	}
	
	public boolean update (final NodeEvaluation updateNodeEvaluation) {
		final int updateEvaluation = updateNodeEvaluation.getEvaluation();
		
		return update(updateEvaluation);
	}

	public boolean update(final int updateEvaluation) {
		if (updateEvaluation > evaluation) {
			evaluation = updateEvaluation;
			alpha = Math.max(alpha, updateEvaluation);
			
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Updates alpha value by given evaluation and checks for beta-cutoff.
	 * @param evaluation move evaluation
	 * @return true if evaluation is higher than beta value, false if not
	 */
	public boolean updateBoundaries (final int evaluation) {
		alpha = Math.max(alpha, evaluation);

		return evaluation > beta;
	}
	
	/**
	 * Clips boundaries to given interval.
	 * @param alpha lower boundary
	 * @param beta upper boundary
	 * @return if boundaries was clipped
	 */
	public boolean clipBoundaries (final int alpha, final int beta) {
		boolean isClipped = false;
		
		if (alpha > this.alpha) {
			this.alpha = alpha;
			isClipped = true;
		}
		
		if (beta < this.beta) {
			this.beta = beta;
			isClipped = true;				
		}
		
		return isClipped;
	}
	
	public boolean isBetaCutoff() {
		return evaluation > beta;
	}
	
	public String toString() {
		return "" + evaluation + " <" + alpha + ", " + beta + ">";
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
}
