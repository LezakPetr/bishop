package optimization;

import java.util.Random;

public class SimulatedAnnealing<S extends IState<S, T>, T> {
	
	private final Class<S> stateClass;
	private final Class<? extends IEvaluator<S, T>> evaluatorClass;
	
	private int optimizationStepCount = 10000;
	private int finalizationStepCount = 100;
	private double minTemperature = 1;
	private double maxTemperature = 256;
	private T settings;
	
	public SimulatedAnnealing (final Class<S> stateClass, final Class<? extends IEvaluator<S, T>> evaluatorClass) {
		this.stateClass = stateClass;
		this.evaluatorClass = evaluatorClass;
	}

	public S optimize() {
		try {
			final IEvaluator<S, T> evaluator = evaluatorClass.newInstance();
			evaluator.setSettings(settings);
			
			final Random random = new Random();
			
			S state = stateClass.newInstance();
			state.randomInitialize(random, settings);
			
			final double a = (minTemperature - maxTemperature) / (Math.E - 1);
			final double b = maxTemperature - a;
			
			double evaluation = evaluator.evaluateState(state);
			
			S bestState = state;
			double bestEvaluation = evaluation;
			
			for (int i = 0; i < optimizationStepCount; i++) {
				final double temp = a * Math.exp (i / (double) optimizationStepCount) + b;
				
				final S newState = state.copy();
				newState.randomChange(random, settings);
				
				final double newEvaluation = evaluator.evaluateState(newState);
				final double evaluationDiff = newEvaluation - evaluation;
				final double probability;
				
				if (evaluationDiff <= 0.0)
					probability = 1.0;
				else
					probability = Math.exp(-evaluationDiff / temp);
					
				if (random.nextDouble() < probability) {
					state = newState;
					evaluation = newEvaluation;
				}
				
				if (newEvaluation < bestEvaluation) {
					bestState = newState;
					bestEvaluation = newEvaluation;
				}
			}
			
			for (int i = 0; i < finalizationStepCount; i++) {
				final S newState = bestState.copy();
				newState.randomChange(random, settings);
				
				final double newEvaluation = evaluator.evaluateState(newState);
				
				if (newEvaluation < bestEvaluation) {
					bestState = newState;
					bestEvaluation = newEvaluation;
				}
			}

			
			return bestState;
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot optimize", ex);
		}
	}
	
	public void setSettings(final T settings) {
		this.settings = settings;
	}
	
	public void setOptimizationStepCount(final int stepCount) {
		this.optimizationStepCount = stepCount;
	}

	public void setFinalizationStepCount(final int stepCount) {
		this.finalizationStepCount = stepCount;
	}

	public void setTemperatureRange (final double minTemperature, final double maxTemperature) {
		this.minTemperature = minTemperature;
		this.maxTemperature = maxTemperature;
		
	}
}
