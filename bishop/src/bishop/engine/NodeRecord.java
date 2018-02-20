package bishop.engine;

import java.util.function.Supplier;

import bishop.base.Move;
import bishop.base.MoveList;

public class NodeRecord implements ISearchResult {
	public final Move currentMove;
	public int moveListBegin;
	public int moveListEnd;
	public final MoveList principalVariation;
	public final NodeEvaluation evaluation;
	public final Move killerMove;
	public final Move principalMove;
	public final Move hashBestMove;
	public final Move firstLegalMove;
	public boolean allMovesGenerated;
	public int maxExtension;
	public final AttackCalculator attackCalculator;
	public boolean isQuiescenceSearch;
	public int legalMoveCount;
	public int bestLegalMoveIndex;

	public NodeRecord(final int maxPrincipalDepth, final Supplier<IPositionEvaluation> evaluationFactory) {
		currentMove = new Move();
		principalVariation = new MoveList(maxPrincipalDepth);
		evaluation = new NodeEvaluation();
		killerMove = new Move();
		principalMove = new Move();
		hashBestMove = new Move();
		firstLegalMove = new Move();
		attackCalculator = new AttackCalculator();
	}

	public void openNode(final int alpha, final int beta) {
		this.evaluation.setEvaluation(Evaluation.MIN);
		this.evaluation.setAlpha(alpha);
		this.evaluation.setBeta(beta);
		this.firstLegalMove.clear();
		this.allMovesGenerated = false;
		this.legalMoveCount = 0;
		this.bestLegalMoveIndex = -1;
	}
	
	public NodeEvaluation getNodeEvaluation() {
		return evaluation;
	}
	
	public MoveList getPrincipalVariation() {
		return principalVariation;
	}
}
