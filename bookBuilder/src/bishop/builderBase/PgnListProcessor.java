package bishop.builderBase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bishop.base.Game;
import bishop.base.PgnReader;

/**
 * Sends all games in PGN list to the walker.
 * 
 * @author Ing. Petr Ležák
 */
public class PgnListProcessor {
	private final List<String> pgnList = new ArrayList<>();
	private IGameWalker gameWalker;
	 
	public void processGames() throws FileNotFoundException, IOException {
		for (String pgnFile: pgnList) {
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
					
					gameWalker.processGame(game);
				}
			}
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
