package math;

/**
 * Simple (one variable) linear regression model of the function y = f(x).
 * Samples added by method addSample are accumulated into internal variables.
 * This method does not affect the model itself. Method recalculateModel recalculates the coefficients
 * of the linear model. Method  estimate calculates values of the linear function by the coefficients.
 * The class guarantees that if the maximal absolute value of x and y is M then 2^63 / (M^2)
 * samples can be accumulated without overflow.
 * 
 * @author Ing. Petr Ležák
 */
public class SimpleLinearModel {
	// Accumulated samples
	private long sumX;
	private long sumY;
	private long sumXY;
	private long sumX2;
	private long count;
	
	// Coefficients of linear function y = f(x) = coeffA * x + coeffB
	private double coeffA;
	private double coeffB;
	
	/**
	 * Adds sample to the linear model.
	 */
	public void addSample (final int x, final int y) {
		sumX += x;
		sumY += y;
		sumXY += (long) x * (long) y;
		sumX2 += (long) x * (long) x;
		count++;
	}
	
	/**
	 * Reduces model in a way as if random half of the samples would be removed.
	 */
	public void reduceWeightOfSamples() {
		sumX >>= 1;
		sumY >>= 1;
		sumXY >>= 1;
		sumX2 >>= 1;
		count >>= 1;
	}
	
	/**
	 * Evaluates the linear function.
	 * @param x x
	 * @return f(x)
	 */
	public int estimate (final int x) {
		return Utils.roundToInt (coeffA * x + coeffB);
	}
	
	/**
	 * Recalculates the coefficients of linear function.
	 */
	public void recalculateModel() {
		final double sumXd = sumX;
		final double sumYd = sumY;
		final double countd = count;
		final double sumXYd = sumXY;
		final double sumX2d = sumX2;
		
		coeffA = (sumXd * sumYd - countd * sumXYd) / (sumXd * sumXd - countd * sumX2d);
		coeffB = (sumYd - coeffA * sumXd) / countd;
	}
}
