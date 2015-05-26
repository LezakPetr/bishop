package bishop.engine;

import java.util.ArrayList;
import java.util.List;

import bishop.base.Move;
import bishop.base.MoveList;
import bishop.base.Position;

public final class SearchNode {
	
	public enum TreeState {
		LEAF,
		OPENED,
		CLOSED
	};
	
	public enum EvaluationState {
		NOT_EVALUATED,   // Move is not evaluated
		EVALUATING,   // Some engine is evaluating this move
		EVALUATED   // Move is evaluated
	}
	
	private final Position position;
	private final SearchNode parent;
	private final Move move;
	private final NodeEvaluation evaluation;
	private final MoveList principalVariation;
	private TreeState treeState;
	private EvaluationState evaluationState;
	private int evaluatedChildrenCount;
	private int requiredHorizon;
	private final List<SearchNode> children;
	private final int depth;
	private int maxExtension;
	
	/**
	 * Creates new search node.
	 * Makes a copy of given position and move.
	 * @param position position after the move
	 * @param parent parent node; null in case of root node
	 * @param move move leading to this node; null in case of root node
	 */
	public SearchNode (final Position position, final SearchNode parent, final Move move) {
		if (parent != null) {
			this.move = move.copy();
			this.depth = parent.depth + 1;
		}
		else {
			this.move = null;
			this.depth = 0;
		}
		
		this.position = position.copy();
		this.parent = parent;
		this.evaluation = new NodeEvaluation();
		this.principalVariation = new MoveList();
		this.children = new ArrayList<SearchNode>();
		this.treeState = TreeState.LEAF;
		
		clear();
	}
	
	public void clear() {
		evaluationState = EvaluationState.NOT_EVALUATED;
		evaluatedChildrenCount = 0;
		
		if (treeState == TreeState.OPENED) {
			treeState = TreeState.CLOSED;
		}
		
		for (SearchNode child: children) {
			child.clear();
		}
	}

	public Position getPosition() {
		return position;
	}

	public SearchNode getParent() {
		return parent;
	}

	public Move getMove() {
		return move;
	}

	public NodeEvaluation getEvaluation() {
		return evaluation;
	}

	public MoveList getPrincipalVariation() {
		return principalVariation;
	}

	public TreeState getTreeState() {
		return treeState;
	}

	public void setTreeState(final TreeState state) {
		this.treeState = state;
	}
	
	public EvaluationState getEvaluationState() {
		return evaluationState;
	}

	public void setEvaluationState(final EvaluationState state) {
		this.evaluationState = state;
	}
	
	public List<SearchNode> getChildren() {
		return children;
	}
	
	public int getEvaluatedChildrenCount() {
		return evaluatedChildrenCount;
	}

	public void setEvaluatedChildrenCount(final int count) {
		this.evaluatedChildrenCount = count;
	}

	public int getDepth() {
		return depth;
	}
	
	public String variationToString() {
		String str = "";
		
		if (parent != null) {
			str += parent.variationToString() + " ";
			str += move.toString();
		}
		
		return str;
	}
	
	private void printTree(final StringBuffer buffer, final int depth) {
		for (int i = 0; i < depth; i++)
			buffer.append('\t');
		
		if (move != null) {
			buffer.append(move);
			buffer.append(' ');
		}
		
		buffer.append(evaluationState.toString());
		buffer.append(' ');
		buffer.append(treeState.toString());
		buffer.append(' ');
		buffer.append(evaluation);
		buffer.append(' ');
		buffer.append(principalVariation);
		buffer.append('\n');
		
		for (SearchNode child: children) {
			child.printTree(buffer, depth+1);
		}
	}
	
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		printTree(buffer, 0);
		
		return buffer.toString();
	}

	public int getRequiredHorizon() {
		return requiredHorizon;
	}

	public void setRequiredHorizon(int requiredHorizon) {
		this.requiredHorizon = requiredHorizon;
	}

	public int getMaxExtension() {
		return maxExtension;
	}

	public void setMaxExtension(final int maxExtension) {
		this.maxExtension = maxExtension;
	}

}
