package bishopTests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import bishop.base.Game;
import bishop.base.IGameNode;
import bishop.base.ITreeIterator;
import bishop.base.Move;
import bishop.base.PgnReader;
import bishop.base.Position;
import bishop.engine.TablebasePositionEvaluator;

public abstract class EloTest extends SearchPerformanceTest {
	
	protected abstract long getMaxTimeForPosition();
	protected abstract void initialize();
	protected abstract void processResult (final boolean correct, final long time);
	protected abstract int calculateElo();

	public void runTest (final String[] args) throws IOException, InterruptedException {
		final String testFile = args[0];
		final String tbbsDir = args[1];
		
		final List<Game> gameList = readGameList(testFile);
		
		final long maxTimeForPosition = getMaxTimeForPosition();
		final TablebasePositionEvaluator tablebaseEvaluator = new TablebasePositionEvaluator(new File (tbbsDir));
		initializeSearchManager(tablebaseEvaluator, maxTimeForPosition);
		initialize();
		
		testGameList(gameList);
				
		final int elo = calculateElo();
		System.out.println ("ELO: " + elo);
		
		stopSearchManager();
	}
	
	private void testGameList(final List<Game> gameList) throws InterruptedException {
		for (Game game: gameList) {
			final long elapsedTime = testGame(game);
			
			System.out.println ("Time: " + elapsedTime + "ms");
			System.out.println();
			
			processResult(elapsedTime < Long.MAX_VALUE, elapsedTime);
		}
	}

}
