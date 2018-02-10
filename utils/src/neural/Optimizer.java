package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Optimizer {
	private static final int SHIFT_MEGA = 20;
	private static final int MASK_MEGA = (1 << SHIFT_MEGA) - 1;
	
	private LearningPerceptronNetwork network;
	
	// Setting
	private final List<ISample> sampleList = new ArrayList<>();
	private long rngSeed = 4616981167149669634L;
	private int testSampleCount = 1000;
	private float initialAlpha = 1.0f;
	private float alphaDropdown = 10.0f;
	private long epochSize = 3000;
	private long epochCount = 20;
	
	// Operational values
	private final List<ISample> trainingSamples = new ArrayList<>();
	private final List<ISample> testSamples = new ArrayList<>();
	private float trainAccuracy;
	private float testAccuracy;
	
	private final Random rng = new Random();
	
	public void addSample(final ISample sample) {
		sampleList.add(sample);
		
		if ((sampleList.size() & MASK_MEGA) == 0)
			System.out.println("Sample cont " + (sampleList.size() >> SHIFT_MEGA) + "M");
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
				final ISample sample = sampleList.get(sampleIndex);
				
				network.learnFromSample(sample, (float) alpha);
			}
			
			trainAccuracy = evaluateAccuracy (trainingSamples.subList(0, testSampleCount));
			testAccuracy = evaluateAccuracy (testSamples);
			
			System.out.println("Train accuracy = " + trainAccuracy + ", test accuracy = " + testAccuracy);
		}
	}

	private float evaluateAccuracy(final List<ISample> samples) {
		double error = 0.0;
		
		for (ISample sample: samples) {
			network.propagateSampleAndCalculateError(sample);
			error += network.getOutputError();
		}
		
		return (float) (error / samples.size());
	}

	private void divideSamples() {
		if (sampleList.size() < 2 * testSampleCount)
			throw new RuntimeException("Not enough samples: " + sampleList.size());
		
		final List<ISample> shuffledSamples = new ArrayList<>(sampleList);
		Collections.shuffle(shuffledSamples, rng);
		
		testSamples.clear();
		testSamples.addAll(shuffledSamples.subList(0, testSampleCount));
		
		trainingSamples.clear();
		trainingSamples.addAll(shuffledSamples.subList(testSampleCount, shuffledSamples.size()));
	}
	
	public LearningPerceptronNetwork getNetwork() {
		return network;
	}

	public void setNetwork(final LearningPerceptronNetwork network) {
		this.network = network;
	}
	
	public void setInitialAlpha (final float initialAlpha) {
		this.initialAlpha = initialAlpha;
	}

	public void setAlphaDropdown (final float dropdown) {
		this.alphaDropdown = dropdown;
	}
	
	public void setEpochSize (final long epochSize) {
		this.epochSize = epochSize;
	}

	public void setEpochCount (final long epochCount) {
		this.epochCount = epochCount;
	}
	
	public float getTestAccuracy() {
		return testAccuracy;
	}
	
	public float getTrainAccuracy() {
		return trainAccuracy;
	}
}
