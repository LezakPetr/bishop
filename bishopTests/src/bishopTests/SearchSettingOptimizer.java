package bishopTests;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import bishop.base.Game;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.engine.ISearchEngine;
import bishop.engine.SearchSettings;

import optimization.IEvaluator;
import optimization.IState;
import optimization.SimulatedAnnealing;

public class SearchSettingOptimizer {

	private static final int ROW_COUNT = 50;
	private static final long MAX_TIME_FOR_POSITION = 2000;
	private static final long MAX_NODE_COUNT = 10000000;

	private static int getRandom (final int min, final int max, final Random rnd) {
		return min + rnd.nextInt(max - min);
	}

	private static void randomizeSettings(final SearchSettings searchSettings, final Random random) {
		searchSettings.setMaxQuiescenceDepth(getRandom(0, 10, random));
		searchSettings.setNullMoveReduction(getRandom(0, 8, random));
		searchSettings.setMinExtensionHorizon(SearchSettings.EXTENSION_GRANULARITY * getRandom(0, 6, random));
		searchSettings.setSimpleCheckExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
		searchSettings.setAttackCheckExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
		searchSettings.setForcedMoveExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
		searchSettings.setMateExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
		searchSettings.setRankAttackExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
		searchSettings.setPawnOnSevenRankExtension (getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
		searchSettings.setRecaptureMaxExtension(getRandom(searchSettings.getRecaptureMinExtension() + 1, SearchSettings.EXTENSION_GRANULARITY, random));
		searchSettings.setRecaptureMinExtension(getRandom(0, searchSettings.getRecaptureMaxExtension(), random));
		searchSettings.setRecaptureBeginMaxTreshold(getRandom(searchSettings.getRecaptureBeginMinTreshold() + 1, PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(PieceType.QUEEN), random));
		searchSettings.setRecaptureBeginMinTreshold(getRandom(0, searchSettings.getRecaptureBeginMaxTreshold(), random));
		searchSettings.setRecaptureTargetTreshold(getRandom(0, PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(PieceType.QUEEN), random));
	}

	public static void main(final String[] main) {
		final File outputFile = new File(main[1]);

		try (PrintWriter outputWriter = new PrintWriter(outputFile)){
			final String testFile = main[0];
			final List<Game> gameList = SearchPerformanceTest.readGameList(testFile);
			final Random rng = new Random(12345);

			final SearchPerformanceTest performanceTest = new SearchPerformanceTest();
			performanceTest.setThreadCount (1);
			performanceTest.initializeSearchManager(null, MAX_TIME_FOR_POSITION);

			final SearchSettings settings = new SearchSettings();

			try {
				outputWriter.print(SearchSettings.CSV_HEADER);
				outputWriter.println(", totalTime, totalNodeCount");

				for (int i = 0; i < ROW_COUNT; i++) {
					long totalTime = 0;
					long totalNodeCount = 0;

					for (Game game: gameList) {
						final SearchPerformanceTest.SearchStatictics statictics = performanceTest.testGame(game);

						totalTime += Math.min(statictics.getTime(), MAX_TIME_FOR_POSITION);
						totalNodeCount += Math.min(statictics.getNodeCount(), MAX_NODE_COUNT);
					}

					outputWriter.print(settings.toString());
					outputWriter.print(", ");
					outputWriter.println(totalTime);
					outputWriter.print(", ");
					outputWriter.println(totalNodeCount);
					outputWriter.flush();

					randomizeSettings(settings, rng);
				}
			}
			finally {
				performanceTest.stopSearchManager();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
