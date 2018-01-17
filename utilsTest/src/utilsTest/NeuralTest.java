package utilsTest;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import org.junit.Assert;
import org.junit.Test;

import neural.BalanceActivationFunction;
import neural.ILearningPerceptronLayer;
import neural.LearningInnerPerceptronLayer;
import neural.LearningOutputPerceptronLayer;
import neural.LearningPerceptronNetwork;
import neural.Optimizer;
import neural.PerceptronNetwork;
import neural.Sample;

public class NeuralTest {
	
	private static final double DERIVATION_DX = 1e-4;
	private static final double DERIVATION_EPS = 1e-3;
	
	private void checkDerivation (final double derivation, final double x, final DoubleUnaryOperator valueCalculator) {
		final double y1 = valueCalculator.applyAsDouble(x + DERIVATION_DX);
		final double y0 = valueCalculator.applyAsDouble(x - DERIVATION_DX);
		final double expectedDerivation = (y1 - y0) / (2 * DERIVATION_DX);
		
		//System.out.println(x + " " + expectedDerivation + " " + derivation  );
		Assert.assertEquals(expectedDerivation, derivation, DERIVATION_EPS);
	}
	
	private void checkDerivationForMoreInputs (final DoubleUnaryOperator valueCalculator, final DoubleUnaryOperator derivationCalculator) {
		for (double x = -3; x <= +3; x += 0.2) {
			checkDerivation(derivationCalculator.applyAsDouble(x), x, valueCalculator);
		}
	}
	
	@Test
	public void testBalanceActivationFunctionDerivation() {
		final BalanceActivationFunction activationFunction = BalanceActivationFunction.getInstance();
		
		checkDerivationForMoreInputs(
			x -> activationFunction.apply((float) x),
			x -> activationFunction.derivate((float) x)
		);
	}
	
	@Test
	public void testLayerDerivation() {
		final BalanceActivationFunction activationFunction = BalanceActivationFunction.getInstance();
		final LearningOutputPerceptronLayer outputLayer = new LearningOutputPerceptronLayer(3);
		final ILearningPerceptronLayer layer = new LearningInnerPerceptronLayer(activationFunction, 2, outputLayer);
		
		final int inputIndex = 1;
		final int outputIndex = 2;
		final float outputError = 3.0f;
		
		checkDerivationForMoreInputs(
			x -> {
				layer.initialize();
				layer.addInput(inputIndex, (float) x);
				layer.propagate();
				
				return outputLayer.getInput (outputIndex);
			},
			x -> {
				layer.initialize();
				layer.addInput(1, (float) x);
				layer.propagate();
				
				for (int i = 0; i < outputLayer.getInputNodeCount(); i++)
					outputLayer.setExpectedInput (i, 2.0f);
				
				outputLayer.setExpectedInput (outputIndex, outputLayer.getInput(outputIndex) + outputError);
				
				layer.backPropagateError();
				
				return layer.getInputError(inputIndex) / outputError;
			}
		);
	}
	
	private float testFunction (final float x, final float y) {
		return (float) Math.tanh(x + 2 * y);
	}
	
	@Test
	public void testLearning() {
		final Random rng = new Random(1654677316576L);
		final int[] layerSizes = {2, 3, 1};
		final LearningPerceptronNetwork network = LearningPerceptronNetwork.create(BalanceActivationFunction.getInstance(), layerSizes);
		final Optimizer optimizer = new Optimizer();
		optimizer.setNetwork(network);
		
		for (int i = 0; i < 100000; i++) {
			final float x = 10 * rng.nextFloat() - 5;
			final float y = 10 * rng.nextFloat() - 5;
			final float val = testFunction(x, y);
			
			optimizer.addSample(new Sample(new float[] {x,  y}, new float[] {val}, 1.0f));
		}
		
		optimizer.learn();
		
		Assert.assertTrue(optimizer.getTrainAccuracy() < 1e-2);
		Assert.assertTrue(optimizer.getTestAccuracy() < 1e-2);
	}
	
	private static final int SPEED_COUNT = 1000000;
	private static final int SPEED_INPUT_LAYER_SIZE = 1000;
	private static final int SPEED_INPUT_COUNT = 50;

	private void singleSpeedTest(final PerceptronNetwork network) {
		for (int i = 0; i < SPEED_COUNT; i++) {
			network.initialize();
			
			for (int j = 0; j < SPEED_INPUT_COUNT; j++)
				network.addInput(j, j);
			
			network.propagate();
		}		
	}
	
	@Test
	public void speedTest() {
		final int[] layerSizes = {SPEED_INPUT_LAYER_SIZE, 200, 10, 1};
		final PerceptronNetwork network = PerceptronNetwork.create(BalanceActivationFunction.getInstance(), layerSizes);
		singleSpeedTest(network);
		
		final long t1 = System.currentTimeMillis();
		singleSpeedTest(network);
		final long t2 = System.currentTimeMillis();
		
		final long dt = t2 - t1;
		
		System.out.println ("Time " + dt + "ms; " + (1000L * SPEED_COUNT / dt) + "prop/s");
	}
}
