package math;

import java.util.function.DoubleUnaryOperator;

/**
 * Sigmoid function. This function is defined for all real numbers.
 * It converges to minY at -infinity and maxX at +infinity.
 * Its starts raising at minX and ends raising at maxX.
 * The function looks like:
 * 
 *                 maxX
 *                  |
 * maxY             +--------
 *                 /
 *                /
 *               /
 *              /  
 * minY -------+    
 *             |
 *            minX 
 *            
 * @author Ing. Petr Ležák
 */
public class Sigmoid implements DoubleUnaryOperator {

	private final double offsetX;
	private final double scaleX;
	private final double offsetY;
	private final double scaleY;
	
	public Sigmoid (final double minX, final double maxX, final double minY, final double maxY) {
		if (minX >= maxX)
			throw new IllegalArgumentException("Wrong X parameters: " + minX + " " + maxX);
		
		this.offsetX = (minX + maxX) / (minX - maxX);
		this.scaleX = 2 / (maxX - minX);
		
		this.offsetY = (maxY + minY) / 2;
		this.scaleY = (maxY - minY) / 2;
	}

	@Override
	public double applyAsDouble(final double x) {
		final double normX = scaleX * x + offsetX; 
		final double normY = Math.tanh(normX);
		
		return scaleY * normY + offsetY;
	}
}
