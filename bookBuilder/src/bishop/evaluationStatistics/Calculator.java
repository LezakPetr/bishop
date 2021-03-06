package bishop.evaluationStatistics;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bishop.builderBase.IGameWalker;
import bishop.builderBase.IPositionWalker;
import bishop.builderBase.LinearGameWalker;
import bishop.builderBase.PgnListProcessor;
import parallel.Parallel;

public class Calculator {
	
	public static void main (final String[] args) throws IOException, InterruptedException, ExecutionException {
		final Parallel parallel = new Parallel();
		final List<String> argList = Arrays.asList(args);
		final File coeffFile = new File (argList.get(0));

		final CoeffPositionProcessor coeffProcessor = new CoeffPositionProcessor(coeffFile);
		
		final IPositionProcessor[] processors = { coeffProcessor };
		
		final IPositionWalker positionWalker = (position, move, result) -> {
			for (IPositionProcessor processor: processors)
				processor.processPosition (position);
		};
		
		final LinearGameWalker linearGameWalker = new LinearGameWalker(positionWalker);
		final IGameWalker walker = (game) -> {
			for (IPositionProcessor processor: processors)
				processor.newGame(game.getHeader().getResult());
			
			linearGameWalker.processGame(game);
			
			for (IPositionProcessor processor: processors)
				processor.endGame();
		};
		
		final PgnListProcessor pgnProcessor = new PgnListProcessor(parallel);
		
		pgnProcessor.addPgnList(argList.subList(1, argList.size()));
		pgnProcessor.setGameWalker(walker);
		
		System.out.println("Processing");
		pgnProcessor.processGames();
		
		System.out.println("Calculating");
		coeffProcessor.calculate();

		parallel.shutdown();
		
		System.out.println("Finished");
	}
}
