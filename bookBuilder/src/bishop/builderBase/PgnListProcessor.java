package bishop.builderBase;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bishop.base.Game;
import bishop.base.PgnReader;
import parallel.Parallel;

/**
 * Sends all games in PGN list to the walker.
 * 
 * @author Ing. Petr Ležák
 */
public class PgnListProcessor {
	private final Parallel parallel;
	private final List<String> pgnList = new ArrayList<>();
	private IGameWalker gameWalker;
	
	public PgnListProcessor(final Parallel parallel) {
		this.parallel = parallel;
	}
	 
	public void processGames() throws IOException, InterruptedException, ExecutionException {
		parallel.parallelForEach(pgnList, this::processPgnFile);
	}

	public void processPgnFile(String pgnFile) {
		try {
			System.out.println(pgnFile);
			
			final PgnReader reader = new PgnReader();
			
			try (
				FileInputStream stream = new FileInputStream(pgnFile);
				PushbackReader pushbackReader = PgnReader.createPushbackReader(stream)
			) {
				while (true) {
					final Game game = reader.readSingleGame(pushbackReader);
					
					if (game == null)
						break;
					
					synchronized (gameWalker) {
						gameWalker.processGame(game);
					}
				}
			}
		}
		catch (Throwable ex) {
			throw new RuntimeException("Cannot read PGN " + pgnFile, ex);
		}
		
	}
	
	public void clearPgnList() {
		this.pgnList.clear();
	}
	
	public void addPgnList (final Collection<String> pgnList) {
		this.pgnList.addAll(pgnList);
	}

	public IGameWalker getGameWalker() {
		return gameWalker;
	}

	public void setGameWalker(final IGameWalker gameWalker) {
		this.gameWalker = gameWalker;
	}

}
