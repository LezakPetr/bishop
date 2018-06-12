package neural;

import regression.Sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Optimizer {
	private LearningPerceptronNetwork network;
	
	// Setting
	private final List<Sample> sampleList = new ArrayList<>();
	private long rngSeed = 4616981167149669634L;
	private int testSampleCount = 1000;
	private float initialAlpha = 1.0f;
	private float alphaDropdown = 10.0f;
	private long epochSize = 300000;
	private long epochCount = 20;
	
	// Operational values
	private final List<Sample> trainingSamples = new ArrayList<>();
	private final List<Sample> testSamples = new ArrayList<>();
	private float trainAccuracy;
	private float testAccuracy;
	
	private final Random rng = new Random();
	
	public void addSample(final Sample sample) {
		sampleList.add(sample);
	}
	
	public void learn() {
		rng.setSeed(rngSeed);
		divideSamples();
		
		network.randomInitializeNetwork(rng);
		
		for (long epoch = 0; epoch < epochCount; epoch++) {
			final double progress = (double) epoch / (double) epochCount;
			final float alpha = (float) (initialAlpha * Math.exp(-alphaDropdown * progress));
			
			for (int i = 0; i < epochSize; i++) {
				final int sampleIndex = rng.nextInt(sampleList.size());
				final Sample sample = sampleList.get(sampleIndex);
				
				network.learnFromSample(sample, (float) alpha);
			}
			
			trainAccuracy = evaluateAccuracy (trainingSamples.subList(0, testSampleCount));
			testAccuracy = evaluateAccuracy (testSamples);
			
			System.out.println("Train accuracy = " + trainAccuracy + ", test accuracy = " + testAccuracy);
		}
	}

	private float evaluateAccuracy(final List<Sample> samples) {
		double error = 0.0;
		
		for (Sample sample: samples) {
			network.propagateSampleAndCalculateError(sample);
			error += network.getOutputError();
		}
		
		return (float) (error / samples.size());
	}

	private void divideSamples() {
		if (sampleList.size() < 2 * testSampleCount)
			throw new RuntimeException("Not enough samples: " + sampleList.size());
		
		final List<Sample> shuffledSamples = new ArrayList<>(sampleList);
		Collections.shuffle(shuffledSamples, rng);
		
		testSamples.clear();
		testSamples.addAll(shuffledSamples.subList(0, testSampleCount));
		
		trainingSamples.clear();
		trainingSamples.addAll(shuffledSamples.subList(testSampleCount, shuffledSamples.size()));
	}

	public void setNetwork(final LearningPerceptronNetwork network) {
		this.network = network;
	}
	
	public float getTestAccuracy() {
		return testAccuracy;
	}
	
	public float getTrainAccuracy() {
		return trainAccuracy;
	}

}
