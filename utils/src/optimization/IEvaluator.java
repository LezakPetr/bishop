package optimization;

public interface IEvaluator<S extends IState<S, T>, T> {
	public double evaluateState (final S state);
	public void setSettings (final T settings);
}
