package bishop.evaluationStatistics;

import bishop.builderBase.IGameWalker;
import bishop.builderBase.IPositionWalker;
import bishop.builderBase.LinearGameWalker;
import bishop.builderBase.PgnListProcessor;
import parallel.Parallel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SampleListGenerator {
	
	public static void main (final String[] args) throws IOException, InterruptedException, ExecutionException {
		final Parallel parallel = new Parallel();
		final List<String> argList = Arrays.asList(args);
		final File sampleListFile = new File (argList.get(0));

		final SampleWriter writer = new SampleWriter(sampleListFile);

		final IPositionWalker positionWalker = (position, move, result) -> {
			writer.processPosition (position);
		};
		
		final LinearGameWalker linearGameWalker = new LinearGameWalker(positionWalker);
		final IGameWalker walker = (game) -> {
			writer.newGame(game.getHeader().getResult());
			linearGameWalker.processGame(game);
			writer.endGame();
		};
		
		final PgnListProcessor pgnProcessor = new PgnListProcessor(parallel);
		
		pgnProcessor.addPgnList(argList.subList(2, argList.size()));
		pgnProcessor.setGameWalker(walker);
		
		System.out.println("Processing");
		pgnProcessor.processGames();

		parallel.shutdown();
		
		System.out.println("Finished");
	}
}
