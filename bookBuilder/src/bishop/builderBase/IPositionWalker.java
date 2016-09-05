package bishop.builderBase;

import bishop.base.GameResult;
import bishop.base.Move;
import bishop.base.Position;

public interface IPositionWalker {
	public void processPosition (final Position position, final Move move, final GameResult result);
}