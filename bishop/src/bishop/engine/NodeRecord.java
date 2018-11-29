package bishop.engine;

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
	public boolean isQuiescenceSearch;
	public int legalMoveCount;
	public int bestLegalMoveIndex;
	public final MobilityCalculator mobilityCalculator;

	// Precreated objects to prevent reallocation
	public final HashRecord precreatedHashRecord;
	public final Move precreatedNullMove;
	public final Move precreatedPrecalculatedMove;
	public final Move precreatedCurrentMove;
	public final Move precreatedNonLosingMove;

	public NodeRecord(final int maxPrincipalDepth) {
		currentMove = new Move();
		principalVariation = new MoveList(maxPrincipalDepth);
		evaluation = new NodeEvaluation();
		killerMove = new Move();
		principalMove = new Move();
		hashBestMove = new Move();
		firstLegalMove = new Move();
		mobilityCalculator = new MobilityCalculator();
		precreatedHashRecord = new HashRecord();
		precreatedNullMove = new Move();
		precreatedPrecalculatedMove = new Move();
		precreatedCurrentMove = new Move();
		precreatedNonLosingMove = new Move();
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
