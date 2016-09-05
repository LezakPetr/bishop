package bishop.evaluationStatistics;

import bishop.base.GameResult;
import bishop.base.Position;

public interface IPositionProcessor {
	public void newGame(final GameResult result);
	public void processPosition (final Position position);
	public void endGame();
}
