package bishop.engine;

import bishop.base.Move;
import bishop.base.MoveList;
import bishop.base.Position;
import bishop.base.PseudoLegalMoveGenerator;

public final class SearchTask {

	private final Position position;
	private int horizon;
	private int alpha;
	private int beta;
	private final MoveList principalVariation;   // Hint from previous iteration
	private boolean initialSearch;
	private RepeatedPositionRegister positionRegister;
	private boolean terminated;
	private int rootMaterialEvaluation;
	private int maxExtension;
	private final Move move;
	private final EvaluatedMoveList rootMoveList;   // Optional moves in the root sorted from worst to best ones
	
	public SearchTask() {
		horizon = 0;
		position = new Position();
		alpha = Evaluation.MIN;
		beta = Evaluation.MAX;
		principalVariation = new MoveList();
		initialSearch = true;
		terminated = false;
		move = new Move();
		rootMoveList = new EvaluatedMoveList(PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public int getHorizon() {
		return horizon;
	}
	
	public void setHorizon (final int horizon) {
		this.horizon = horizon;
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

	public MoveList getPrincipalVariation() {
		return principalVariation;
	}

	public boolean isInitialSearch() {
		return initialSearch;
	}

	public void setInitialSearch(final boolean initialSearch) {
		this.initialSearch = initialSearch;
	}

	public RepeatedPositionRegister getRepeatedPositionRegister() {
		return this.positionRegister;
	}

	public void setRepeatedPositionRegister(final RepeatedPositionRegister positionRegister) {
		this.positionRegister = positionRegister;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(final boolean terminated) {
		this.terminated = terminated;
	}

	/**
	 * Returns material evaluation of the root node from white point of view.
	 * @return material evaluation
	 */
	public int getRootMaterialEvaluation() {
		return rootMaterialEvaluation;
	}

	/**
	 * Sets material evaluation of the root node from white point of view.
	 * @param rootMaterialEvaluation material evaluation
	 */
	public void setRootMaterialEvaluation(int rootMaterialEvaluation) {
		this.rootMaterialEvaluation = rootMaterialEvaluation;
	}

	public int getMaxExtension() {
		return maxExtension;
	}

	public void setMaxExtension(int maxExtension) {
		this.maxExtension = maxExtension;
	}
	
	public Move getMove() {
		return move;
	}
	
	public void setMove (final Move move) {
		this.move.assign(move);
	}

	public EvaluatedMoveList getRootMoveList() {
		return rootMoveList;
	}

}
