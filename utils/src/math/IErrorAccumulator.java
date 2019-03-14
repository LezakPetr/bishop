package math;

public interface IErrorAccumulator {
	public void addSample (final double predictedY, final int expectedY);
	public void clear();
}
