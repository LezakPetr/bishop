package bishopTests;

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
	
	private static final int PARAMETER_COUNT = 14;
	private static final long MAX_TIME_FOR_POSITION = 2000;
	
	private static final int FINALIZATION_STEP_COUNT = 0;
	private static final int OPTIMIZATION_STEP_COUNT = 500;
	private static final double MAX_TEMPERATURE_COEFF = 0.5;
	private static final double MIN_TEMPERATURE_COEFF = 0.02;
	
	
	private static class Settings {
		private final List<Game> gameList;
		
		public Settings(final List<Game> gameList) {
			this.gameList = gameList;
		}
		
		public List<Game> getGameList() {
			return gameList;
		}
	}
	

	public static class State implements IState<State, Settings> {

		private final SearchSettings searchSettings = new SearchSettings();

		
		@Override
		public void randomInitialize(final Random random, final Settings optimizerSettings) {
			for (int i = 0; i < PARAMETER_COUNT; i++)
				changeParameter(i, random);
		}

		@Override
		public void randomChange(final Random random, final Settings optimizerSettings) {
			final int parameter = getRandom(0, PARAMETER_COUNT, random);
			
			changeParameter(parameter, random);
		}

		private static int getRandom (final int min, final int max, final Random rnd) {
			return min + rnd.nextInt(max - min);
		}
		
		private void changeParameter (final int parameter, final Random random) {
			switch (parameter) {
				case 0:
					searchSettings.setMaxQuiescenceDepth(getRandom(0, 10, random));
					break;
					
				case 1:
					searchSettings.setNullMoveReduction(getRandom(0, 8, random));
					break;

				case 2:
					searchSettings.setMinExtensionHorizon(SearchSettings.EXTENSION_GRANULARITY * getRandom(0, 6, random));
					break;

				case 3:
					searchSettings.setSimpleCheckExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
					break;
					
				case 4:
					searchSettings.setAttackCheckExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
					break;

				case 5:
					searchSettings.setForcedMoveExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
					break;

				case 6:
					searchSettings.setMateExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
					break;

				case 7:
					searchSettings.setRankAttackExtension(getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
					break;
					
				case 8:
					searchSettings.setPawnOnSevenRankExtension (getRandom(0, SearchSettings.EXTENSION_GRANULARITY, random));
					break;

				case 9:
					searchSettings.setRecaptureMaxExtension(getRandom(searchSettings.getRecaptureMinExtension() + 1, SearchSettings.EXTENSION_GRANULARITY, random));
					break;

				case 10:
					searchSettings.setRecaptureMinExtension(getRandom(0, searchSettings.getRecaptureMaxExtension(), random));
					break;

				case 11:
					searchSettings.setRecaptureBeginMaxTreshold(getRandom(searchSettings.getRecaptureBeginMinTreshold() + 1, PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(PieceType.QUEEN), random));
					break;

				case 12:
					searchSettings.setRecaptureBeginMinTreshold(getRandom(0, searchSettings.getRecaptureBeginMaxTreshold(), random));
					break;

				case 13:
					searchSettings.setRecaptureTargetTreshold(getRandom(0, PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(PieceType.QUEEN), random));
					break;
			}
		}

		@Override
		public State copy() {
			final State state = new State();
			state.searchSettings.assign (this.searchSettings);
			
			return state;
		}
		
		@Override
		public String toString() {
			return searchSettings.toString();
		}
	}
	
	public static class Evaluator implements IEvaluator<State, Settings> {

		private final SearchPerformanceTest performanceTest;
		private Settings optimizerSettings;
		
		public Evaluator() {
			performanceTest = new SearchPerformanceTest();
		}
		
		@Override
		public double evaluateState(final State state) {
			performanceTest.initializeSearchManager(null, MAX_TIME_FOR_POSITION);
			
			try {
				long totalTime = 0;
				
				for (Game game: optimizerSettings.getGameList()) {
					final long time = performanceTest.testGame(game);
					
					totalTime += Math.min(time, MAX_TIME_FOR_POSITION);
				}
				
				System.out.println ("Time: " + totalTime);
				
				return totalTime;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				return Long.MAX_VALUE;
			}
			finally {
				performanceTest.stopSearchManager();
			}
		}

		@Override
		public void setSettings(final Settings optimizerSettings) {
			this.optimizerSettings = optimizerSettings;
		}
		
	}

	public static void main(final String[] main) {
		try {
			final String testFile = main[0];
			final SimulatedAnnealing<State, Settings> annealing = new SimulatedAnnealing<State, Settings>(State.class, Evaluator.class);
			final List<Game> gameList = SearchPerformanceTest.readGameList(testFile);
			final int positionCount = gameList.size();
			
			annealing.setFinalizationStepCount(FINALIZATION_STEP_COUNT);
			annealing.setOptimizationStepCount(OPTIMIZATION_STEP_COUNT);
			annealing.setTemperatureRange(MIN_TEMPERATURE_COEFF * MAX_TIME_FOR_POSITION * positionCount, MAX_TEMPERATURE_COEFF * MAX_TIME_FOR_POSITION * positionCount);
			annealing.setSettings(new Settings(gameList));
			
			final State optimalState = annealing.optimize();
			System.out.println (optimalState);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
