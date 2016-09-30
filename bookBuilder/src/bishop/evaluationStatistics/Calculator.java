package bishop.evaluationStatistics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import bishop.builderBase.IGameWalker;
import bishop.builderBase.IPositionWalker;
import bishop.builderBase.LinearGameWalker;
import bishop.builderBase.PgnListProcessor;

public class Calculator {
	
	public static void main (final String[] args) throws FileNotFoundException, IOException {
		final MaterialStatisticsPositionProcessor materialProcessor = new MaterialStatisticsPositionProcessor();
		
		final IPositionProcessor[] processors = { materialProcessor };
		
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
		
		final PgnListProcessor pgnProcessor = new PgnListProcessor();
		pgnProcessor.addPgnList(Arrays.asList(args));
		pgnProcessor.setGameWalker(walker);
		
		pgnProcessor.processGames();
		
		materialProcessor.calculate();
	}
}
