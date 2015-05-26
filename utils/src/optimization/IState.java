package optimization;

import java.util.Random;

public interface IState<S extends IState<S, T>, T> {
	public void randomInitialize(final Random random, final T settings);
	public void randomChange(final Random random, final T settings);
	public S copy();
}
