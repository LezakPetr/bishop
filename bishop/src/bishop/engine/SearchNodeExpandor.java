package bishop.engine;

import bishop.base.IMoveWalker;
import bishop.base.LegalMoveGenerator;
import bishop.base.Move;
import bishop.base.Position;

public final class SearchNodeExpandor {
	
	private SearchNode searchNode;
	private final LegalMoveGenerator moveGenerator;
	
	private final IMoveWalker moveWalker = new IMoveWalker() {
		public boolean processMove (final Move move) {
			final Position position = searchNode.getPosition().copy();
			position.makeMove(move);
			
			final SearchNode node = new SearchNode(position, searchNode, move);
			searchNode.getChildren().add(node);
			
			return true;
		}
	};
	
	public SearchNodeExpandor() {
		moveGenerator = new LegalMoveGenerator();
		moveGenerator.setWalker(moveWalker);
	}
	
	public void expandNode (final SearchNode node) {
		this.searchNode = node;
		this.moveGenerator.setPosition(node.getPosition());
		
		node.getChildren().clear();
		this.moveGenerator.generateMoves();
		
		this.moveGenerator.setPosition(null);
		this.searchNode = null;
	}

}
