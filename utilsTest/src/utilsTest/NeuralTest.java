package utilsTest;

import java.util.function.DoubleUnaryOperator;

import org.junit.Assert;
import org.junit.Test;

import neural.BalanceActivationFunction;
import neural.ILearningPerceptronLayer;
import neural.LearningInnerPerceptronLayer;
import neural.LearningOutputPerceptronLayer;

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
		final BalanceActivationFunction activationFunction = new BalanceActivationFunction();
		
		checkDerivationForMoreInputs(
			x -> activationFunction.apply((float) x),
			x -> activationFunction.derivate((float) x)
		);
	}
	
	@Test
	public void testLayerDerivation() {
		final BalanceActivationFunction activationFunction = new BalanceActivationFunction();
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
}
