package bishop.evaluationStatistics;

import bishop.base.PgnWriter;
import bishop.builderBase.IGameWalker;
import bishop.builderBase.PgnListProcessor;
import parallel.Parallel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutionException;

public class SubSampler {
	public static void main(final String[] args) throws IOException, InterruptedException, ExecutionException {
		final Parallel parallel = new Parallel();
		final List<String> argList = Arrays.asList(args);
		final File outputFile = new File(argList.get(0));
		final double probability = Double.parseDouble(argList.get(1));

		final PgnWriter pgnWriter = new PgnWriter();
		final SplittableRandom rng = new SplittableRandom(1234);

		final IGameWalker walker = (game) -> {
			if (rng.nextDouble() < probability)
				pgnWriter.getGameList().add(game);
		};

		final PgnListProcessor pgnProcessor = new PgnListProcessor(parallel);

		pgnProcessor.addPgnList(argList.subList(2, argList.size()));
		pgnProcessor.setGameWalker(walker);

		System.out.println("Processing");
		pgnProcessor.processGames();

		System.out.println("Writing");

		try (OutputStream outputStream = new FileOutputStream(outputFile)) {
			pgnWriter.writePgnToStream(outputStream);
		}

		parallel.shutdown();

		System.out.println("Finished");
	}
}
