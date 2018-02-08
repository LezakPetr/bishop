package bishop.engine;


import bishop.base.Color;
import neural.PerceptronNetwork;

public class NeuralPositionEvaluation implements IPositionEvaluation {

	private final PerceptronNetwork network;
	private int onTurn;
	private int colorCoeff;
	
	public NeuralPositionEvaluation (final PerceptronNetwork network) {
		this.network = network;
	}
	
	@Override
	public int getEvaluation() {
		network.propagate();
		
		final int evaluation = (int) network.getOutput (0);
		
		return evaluation * colorCoeff;
	}

	@Override
	public void clear(final int onTurn) {
		this.onTurn = onTurn;
		
		network.initialize();
		colorCoeff = (onTurn == Color.WHITE) ? 1 : -1;
	}
	
	@Override
	public void addCoeffWithCount(final int index, final int count) {
		network.addInput(index, count * colorCoeff);
	}
		
	@Override
	public void addCoeff(final int index, final int color, final int count) {
		final int signedCount = (color == onTurn) ? +count : -count;
		
		network.addInput(index, signedCount);
	}

	@Override
	public void addCoeff(final int index, final int color) {
		if (color == onTurn)
			network.addPositiveUnityInput (index);
		else
			network.addNegativeUnityInput (index);
	}

}
