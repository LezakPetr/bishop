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

	private static final int PREWARM_COUNT = 2;
	private static final int ROW_COUNT = 5000;
	private static final long MAX_TIME_FOR_POSITION = 10000;
	private static final long MAX_NODE_COUNT = 10000000;
	private static final double SD_RANGE_RATIO = 0.2;

	private final Random random = new Random(12345);

	private int getRandom (final int min, final int max, final int mean) {
		final double sd = (max - min) * SD_RANGE_RATIO;
		final double boundedMean = Math.max(Math.min(mean, max), min);
		final double value = boundedMean + sd * random.nextGaussian();

		if (value <= min)
			return min;

		if (value >= max)
			return max;

		return Utils.roundToInt(value);
	}

	private void randomizeSettings(final SearchSettings searchSettings, final SearchSettings optimalSettings) {
		searchSettings.setMaxQuiescenceDepth(
				getRandom(
						1, 20,
						optimalSettings.getMaxQuiescenceDepth()
				)

		);

		searchSettings.setMaxCheckSearchDepth(
				getRandom(
						0, searchSettings.getMaxQuiescenceDepth() - 1,
						optimalSettings.getMaxCheckSearchDepth()
				)
		);

		searchSettings.setNullMoveReduction(
				getRandom(
						0, 6,
						optimalSettings.getNullMoveReduction()
				)
		);

		searchSettings.setMinExtensionHorizon(
				getRandom(
						0, 8,
						optimalSettings.getMinExtensionHorizon()
				)
		);

		searchSettings.setSimpleCheckExtension(
				getRandom(
						0, SearchSettings.EXTENSION_GRANULARITY - 1,
						optimalSettings.getSimpleCheckExtension()
				)
		);

		searchSettings.setAttackCheckExtension(
				getRandom(
						searchSettings.getSimpleCheckExtension() + 1, SearchSettings.EXTENSION_GRANULARITY,
						optimalSettings.getAttackCheckExtension()
				)
		);

		searchSettings.setForcedMoveExtension(
				getRandom(
						0, SearchSettings.EXTENSION_GRANULARITY,
						optimalSettings.getForcedMoveExtension()
				)
		);

		searchSettings.setMateExtension(
				getRandom(
						0, SearchSettings.EXTENSION_GRANULARITY,
						optimalSettings.getMateExtension()
				)
		);

		searchSettings.setRankAttackExtension(
				getRandom(
						0, SearchSettings.EXTENSION_GRANULARITY,
						optimalSettings.getRankAttackExtension()
				)
		);

		searchSettings.setPawnOnSevenRankExtension (
				getRandom(
						0, SearchSettings.EXTENSION_GRANULARITY,
						optimalSettings.getPawnOnSevenRankExtension()
				)
		);

		searchSettings.setProtectingPawnOnSixRankExtension (
				getRandom(
						0, SearchSettings.EXTENSION_GRANULARITY,
						optimalSettings.getProtectingPawnOnSixRankExtension()
				)
		);

		searchSettings.setRecaptureMinExtension(
				getRandom(
						0, SearchSettings.EXTENSION_GRANULARITY - 1,
						optimalSettings.getRecaptureMinExtension()
				)
		);

		searchSettings.setRecaptureMaxExtension(
				getRandom(
						searchSettings.getRecaptureMinExtension() + 1, SearchSettings.EXTENSION_GRANULARITY,
						optimalSettings.getRecaptureMaxExtension()
				)
		);

		final int queenEvaluation = PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(PieceType.QUEEN);

		searchSettings.setRecaptureBeginMinTreshold(
				getRandom(
						0, queenEvaluation - 1,
						optimalSettings.getRecaptureBeginMinTreshold()
				)
		);

		searchSettings.setRecaptureBeginMaxTreshold(
				getRandom(
						searchSettings.getRecaptureBeginMinTreshold() + 1, queenEvaluation,
						optimalSettings.getRecaptureBeginMaxTreshold()
				)
		);

		searchSettings.setRecaptureTargetTreshold(
				getRandom(
						0, queenEvaluation,
						optimalSettings.getRecaptureTargetTreshold()
				)
		);
	}

	private void optimize(final String[] args) {
		final File outputFile = new File(args[1]);

		try (PrintWriter outputWriter = new PrintWriter(outputFile)){
			final String testFile = args[0];
			final List<Game> gameList = SearchPerformanceTest.readGameList(testFile);

			final SearchPerformanceTest performanceTest = new SearchPerformanceTest();
			performanceTest.setThreadCount (1);
			performanceTest.initializeSearchManager(null, MAX_TIME_FOR_POSITION);

			final SearchSettings settings = new SearchSettings();
			final SearchSettings optimalSettings = new SearchSettings();
			long optimalNodeCount = Long.MAX_VALUE;

			try {
				// Prewarm
				for (int i = 0; i < PREWARM_COUNT; i++) {
					for (Game game: gameList)
						performanceTest.testGame(game);
				}

				// Run
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

					if (totalNodeCount < optimalNodeCount) {
						optimalNodeCount = totalNodeCount;
						optimalSettings.assign(settings);

						System.out.println ("Iteration " + i + " - new optimum " + totalNodeCount);
					}

					randomizeSettings(settings, optimalSettings);
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

	public static void main(final String[] args) {
		final SearchSettingOptimizer optimizer = new SearchSettingOptimizer();
		optimizer.optimize(args);
	}
}
