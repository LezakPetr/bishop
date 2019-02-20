package math;

public interface IErrorAccumulator {
	public void addSample (final double probability, final int y);
	public void clear();
}
