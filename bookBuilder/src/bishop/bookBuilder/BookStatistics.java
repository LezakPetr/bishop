package bishop.bookBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import bishop.base.Game;
import bishop.base.GameResult;
import bishop.base.Move;
import bishop.base.Position;
import bishop.builderBase.LinearGameWalker;
import bishop.builderBase.PgnListProcessor;
import bishop.engine.BookMove;
import bishop.engine.BookReader;
import bishop.engine.BookRecord;
import bishop.engine.BookSource;
import bishop.engine.BookWriter;
import math.Utils;
import parallel.Parallel;

/**
 * BookStatistics holds statistical information about a book.
 * 
 * @author Ing. Petr Ležák
 */
public class BookStatistics {
	
	// Statistics about each position
	private final Map<Position, PositionStatistics> positionStatisticsMap = new HashMap<>();
	
	private BookSource book;	
	private final PositionRepetitionFilter repetitionFilter = new PositionRepetitionFilter();
	
	private int minAbsolutePositionRepetition = 16;   // Minimal count of occurrence of position to be written into book
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
			PositionStatistics statistics = positionStatisticsMap.get(position);
			
			if (statistics == null) {
				statistics = new PositionStatistics();
				positionStatisticsMap.put(position.copy(), statistics);
			}
			
			statistics.addMove(position.getOnTurn(), move, result);
		}
	}
		
	// Build statistics from main variations of games in given PGNs. 
	public void addLinearGames (final Collection<String> pgnList) throws IOException, InterruptedException, ExecutionException {
		final Parallel parallel = new Parallel();
		final PgnListProcessor processor = new PgnListProcessor(parallel);
		processor.addPgnList(pgnList);
		
		// Build repetition filter
		repetitionFilter.clear();
		processor.setGameWalker((game) -> { addLinearGameToRepetitionFilter(game); });
		processor.processGames();
		
		// Build statistics
		processor.setGameWalker((game) -> { addLinearGameToStatistics(game); });
		processor.processGames();
		
		parallel.shutdown();
	}
	
	// Stores book to dat file.
	public void storeBookToFile(final String path) throws FileNotFoundException, IOException {
		buildBook();
		
		final BookWriter writer = new BookWriter();
		writer.writeBook(book, path);
		
		verifyBook(path);
		
		System.out.println(book.getPositionCount() + " positions written");
	}
	
	private void verifyBook(final String path) throws FileNotFoundException, IOException {
		System.out.println("Verification");
		
		final BookReader reader = new BookReader(path);
		
		for (BookRecord origRecord: book.getAllRecords()) {
			final Position position = origRecord.getPosition();
			final BookRecord readRecord = reader.getRecord(position);
		
			if (!origRecord.equals(readRecord)) {
				throw new RuntimeException("Wrong record. Orig: " + origRecord + ", read: " + readRecord);
			}
		}
	}
	
	private void buildBook() {
		book = new BookSource();
		
		for (Position position: positionStatisticsMap.keySet())
			storePosition (position);
	}
		
	// Stores descendants of position on given iterator to the game 
	private void storePosition (final Position prevPosition) {
		final PositionStatistics prevPositionStatistics = positionStatisticsMap.get(prevPosition);
		final long prevPositionRepetition = prevPositionStatistics.getTotalCount();
		
		if (prevPositionRepetition < minAbsolutePositionRepetition)
			return;
		
		final double prevBalance = prevPositionStatistics.getBalance();
		
		final BookRecord record = new BookRecord();
		record.getPosition().assign(prevPosition);
		record.setRepetitionCount(prevPositionRepetition);
		record.setBalance(Utils.roundToPercents(prevBalance));
		
		for (Move move: prevPositionStatistics.getMoves()) {
			final Position nextPosition = prevPosition.copy();
			nextPosition.makeMove(move);

			final PositionStatistics nextPositionStatistics = positionStatisticsMap.get(nextPosition);
			
			if (nextPositionStatistics != null) {
				final long nextPositionRepetition = nextPositionStatistics.getTotalCount();
				final int moveRepetition = prevPositionStatistics.getMoveCount(move);
				final double relativeMoveRepetition = (double) moveRepetition / (double) prevPositionRepetition;
				
				if (nextPositionRepetition >= minAbsolutePositionRepetition) {
					final BookMove bookMove = new BookMove();
					bookMove.setMove(move);
					bookMove.setRelativeMoveRepetition(Utils.roundToPercents(relativeMoveRepetition));
					bookMove.setTargetPositionBalance(Utils.roundToPercents(nextPositionStatistics.getBalance()));
					
					record.addMove(bookMove);
				}
			}
		}
		
		book.addRecord(record);
	}
}
