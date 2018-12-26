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

import math.Utils;
import optimization.IEvaluator;
import optimization.IState;
import optimization.SimulatedAnnealing;
import utils.IntUtils;

public class SearchSettingOptimizer {

	private static final int ROW_COUNT = 5000;
	private static final long MAX_TIME_FOR_POSITION = 5000;
	private static final long MAX_NODE_COUNT = 10000000;

	private static int getRandom (final int min, final int max, final int mean, final Random rnd) {
		final double sd = (max - min) / 2;
		final double boundedMean = Math.max(Math.min(mean, max), min);
		final double value = boundedMean + sd * rnd.nextGaussian();

		if (value <= min)
			return min;

		if (value >= max - 1)
			return max - 1;

		return Utils.roundToInt(value);
	}

	private static void randomizeSettings(final SearchSettings searchSettings, final SearchSettings currentSettings, final Random random) {
		searchSettings.setMaxQuiescenceDepth(getRandom(1, 20, currentSettings.getMaxQuiescenceDepth(), random));
		searchSettings.setMaxCheckSearchDepth(getRandom(0, searchSettings.getMaxQuiescenceDepth(), currentSettings.getMaxCheckSearchDepth(), random));
		searchSettings.setNullMoveReduction(getRandom(0, 6, currentSettings.getNullMoveReduction(), random));
		searchSettings.setMinExtensionHorizon(getRandom(0, 8, currentSettings.getMinExtensionHorizon(), random));

		searchSettings.setSimpleCheckExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, currentSettings.getSimpleCheckExtension(), random));
		searchSettings.setAttackCheckExtension(getRandom(searchSettings.getSimpleCheckExtension(), SearchSettings.EXTENSION_GRANULARITY, currentSettings.getAttackCheckExtension(), random));
		searchSettings.setForcedMoveExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, currentSettings.getForcedMoveExtension(), random));
		searchSettings.setMateExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, currentSettings.getMateExtension(), random));
		searchSettings.setRankAttackExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, currentSettings.getRankAttackExtension(), random));

		searchSettings.setPawnOnSevenRankExtension (getRandom(0, SearchSettings.EXTENSION_GRANULARITY, currentSettings.getPawnOnSevenRankExtension(), random));
		searchSettings.setProtectingPawnOnSixRankExtension (getRandom(0, SearchSettings.EXTENSION_GRANULARITY, currentSettings.getProtectingPawnOnSixRankExtension(), random));

		searchSettings.setRecaptureMinExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY - 1, currentSettings.getRecaptureMinExtension(), random));
		searchSettings.setRecaptureMaxExtension(getRandom(searchSettings.getRecaptureMinExtension() + 1, SearchSettings.EXTENSION_GRANULARITY, currentSettings.getRecaptureMaxExtension(), random));

		final int queenEvaluation = PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(PieceType.QUEEN);
		searchSettings.setRecaptureBeginMinTreshold(getRandom(0, queenEvaluation - 1, currentSettings.getRecaptureBeginMinTreshold(), random));
		searchSettings.setRecaptureBeginMaxTreshold(getRandom(searchSettings.getRecaptureBeginMinTreshold() + 1, queenEvaluation, currentSettings.getRecaptureBeginMaxTreshold(), random));
		searchSettings.setRecaptureTargetTreshold(getRandom(0, queenEvaluation, currentSettings.getRecaptureTargetTreshold(), random));
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
			final SearchSettings currentSettings = new SearchSettings();

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
					outputWriter.print(totalTime);
					outputWriter.print(", ");
					outputWriter.println(totalNodeCount);
					outputWriter.flush();

					randomizeSettings(settings, currentSettings, rng);
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
