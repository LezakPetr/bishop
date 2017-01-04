package bishop.bookBuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import bishop.base.Annotation;
import bishop.base.Game;
import bishop.base.GameResult;
import bishop.base.IGameNode;
import bishop.base.ITreeIterator;
import bishop.base.Move;
import bishop.base.PgnWriter;
import bishop.base.Position;
import bishop.builderBase.LinearGameWalker;
import bishop.builderBase.PgnListProcessor;
import parallel.Parallel;

/**
 * BookStatistics holds statistical information about a book.
 * 
 * @author Ing. Petr Ležák
 */
public class BookStatistics {
	
	// Statistics about each position
	private final Map<Long, PositionStatistics> positionStatisticsMap = new HashMap<>();
	
	// Hashes of positions already processed when writing the book
	private final Set<Long> processedPositions = new HashSet<>();
	
	private final PositionRepetitionFilter repetitionFilter = new PositionRepetitionFilter();
	
	private int minAbsolutePositionRepetition = 16;   // Minimal count of occurrence of position to be written into book
	private double minRelativeMoveRepetition = 0.02;   // Minimal relative occurrence of move in a position to be considered as a good move
	private double minBalanceDifference = -0.2;   // Minimal difference between balances of source and target positions so the move is considered as a good one 
	private int maxDepth = 40;

	
	private void addLinearGameToRepetitionFilter (final Game game) {
		final LinearGameWalker walker = new LinearGameWalker(
			(position, move, result) -> { repetitionFilter.addPositionToRepetitionFilter(position.getHash()); }
		);
		
		walker.setMaxDepth(maxDepth);
		walker.processGame(game);
	}

	private void addLinearGameToStatistics (final Game game) {
		final LinearGameWalker walker = new LinearGameWalker(
			(position, move, result) -> { addMoveToStatistics(position, move, result); }
		);
		
		walker.setMaxDepth(maxDepth);
		walker.processGame(game);
	}
	
	private void addMoveToStatistics (final Position position, final Move move, final GameResult result) {
		final Long hash = position.getHash();
		
		if (repetitionFilter.canPositionBeRepeatedAtLeast(hash, minAbsolutePositionRepetition)) {
			PositionStatistics statistics = positionStatisticsMap.get(hash);
			
			if (statistics == null) {
				statistics = new PositionStatistics();
				positionStatisticsMap.put(hash, statistics);
			}
			
			statistics.addMove(position.getOnTurn(), move, result);
		}
	}
		
	// Build statistics from main variations of games in given PGNs. 
	public void addLinearGames (final Collection<String> pgnList) throws IOException, InterruptedException, ExecutionException {
		final PgnListProcessor processor = new PgnListProcessor(new Parallel());
		processor.addPgnList(pgnList);
		
		// Build repetition filter
		repetitionFilter.clear();
		processor.setGameWalker((game) -> { addLinearGameToRepetitionFilter(game); });
		processor.processGames();
		
		// Build statistics
		processor.setGameWalker((game) -> { addLinearGameToStatistics(game); });
		processor.processGames();
	}
	
	// Stores book to PGN file.
	public void storeBookToFile(final String path) throws FileNotFoundException, IOException {
		final Game game = storeBookToGame();
		final PgnWriter writer = new PgnWriter();
		writer.getGameList().add(game);
		
		try (OutputStream stream = new FileOutputStream(path)) {
			writer.writePgnToStream(stream);
		}	
	}
	
	// Stores book to game.
	public Game storeBookToGame() {
		processedPositions.clear();
		
		final Game game = new Game();
		final ITreeIterator<IGameNode> it = game.getRootIterator();		
		
		storePosition (game, it);
		
		return game;
	}
	
	// Stores descendants of position on given iterator to the game 
	private void storePosition (final Game game, final ITreeIterator<IGameNode> it) {
		final Position prevPosition = it.getItem().getTargetPosition();
		final Long prevHash = prevPosition.getHash();
		
		if (processedPositions.contains(prevHash))
			return;
		
		processedPositions.add(prevHash);
		
		final PositionStatistics prevPositionStatistics = positionStatisticsMap.get(prevHash);
		final long prevPositionRepetition = prevPositionStatistics.getTotalCount();
		final double prevBalance = prevPositionStatistics.getBalance();
				
		for (Move move: prevPositionStatistics.getMoves()) {
			final Position nextPosition = prevPosition.copy();
			nextPosition.makeMove(move);
			
			final PositionStatistics nextPositionStatistics = positionStatisticsMap.get(nextPosition.getHash());
			
			if (nextPositionStatistics != null) {
				final long nextPositionRepetition = nextPositionStatistics.getTotalCount();
				final int moveRepetition = prevPositionStatistics.getMoveCount(move);
				final double relativeMoveRepetition = (double) moveRepetition / (double) prevPositionRepetition;
				
				if (nextPositionRepetition >= minAbsolutePositionRepetition) {
					boolean goodMove = false;
					
					if (relativeMoveRepetition >= minRelativeMoveRepetition) {
						final double nextBalance = nextPositionStatistics.getBalance();
						
						if (-nextBalance - prevBalance >= minBalanceDifference) {
							goodMove = true;
						}						
					}
					
					final ITreeIterator<IGameNode> childIt = game.addChild(it, move);
					
					if (!goodMove)
						childIt.getItem().setAnnotation(Annotation.POOR_MOVE);
					
					storePosition(game, childIt);
				}
			}
		}
	}
}
