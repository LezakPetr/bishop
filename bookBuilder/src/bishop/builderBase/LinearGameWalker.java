package bishop.builderBase;

import bishop.base.Game;
import bishop.base.GameResult;
import bishop.base.IGameNode;
import bishop.base.ITreeIterator;
import bishop.base.Move;
import bishop.base.Position;

/**
 * Sends all positions in the main variation of the game to the walker
 * 
 * @author Ing. Petr Ležák
 */
public class LinearGameWalker implements IGameWalker {
	
	private final IPositionWalker positionWalker;
	private int maxDepth = Integer.MAX_VALUE;
	
	public LinearGameWalker(final IPositionWalker positionWalker) {
		this.positionWalker = positionWalker;
	}

	@Override
	public void processGame(final Game game) {
		final ITreeIterator<IGameNode> it = game.getRootIterator();
		final GameResult result = game.getHeader().getResult();
		
		for (int i = 0; i < maxDepth; i++) {
			final Position position = it.getItem().getTargetPosition();
			
			if (!it.hasChild())
				break;
			
			it.moveFirstChild();
			
			final Move move = it.getItem().getMove();
			positionWalker.processPosition(position, move, result);
		}
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

}
