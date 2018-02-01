package bishop.engine;


import bishop.base.Color;
import bishop.base.PieceTypeEvaluations;
import neural.PerceptronNetwork;

public class NeuralPositionEvaluation implements IPositionEvaluation {

	private final PerceptronNetwork network;
	private int onTurn;
	private int colorCoeff;
	private int evaluationShift;
	
	public NeuralPositionEvaluation (final PerceptronNetwork network) {
		this.network = network;
	}
	
	@Override
	public int getEvaluation() {
		network.propagate();
		
		final float evaluation = network.getOutput (0);
		final float shiftedEvaluation = Math.scalb(evaluation, -evaluationShift);
		
		return (int) shiftedEvaluation * colorCoeff;
	}

	@Override
	public void clear(final int onTurn) {
		this.onTurn = onTurn;
		
		network.initialize();
		evaluationShift = 0;
		colorCoeff = (onTurn == Color.WHITE) ? 1 : -1;
	}
	
	@Override
	public void addCoeffWithCount(final int index, final int count) {
		network.addInput(index, count * colorCoeff);
	}
	
	@Override
	public void shiftRight (final int shift) {
		evaluationShift += shift;
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
