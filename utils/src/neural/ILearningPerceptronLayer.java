package neural;

public interface ILearningPerceptronLayer extends IPerceptronLayer {
	public void backPropagateError();
	
	public float getInputError(final int inputIndex);
}
