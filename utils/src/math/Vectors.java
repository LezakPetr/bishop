package math;

import java.util.function.DoubleUnaryOperator;

public class Vectors {
	
	/**
	 * Returns zero vector with given dimension.
	 * @param dimension dimension of vector
	 * @return zero vector
	 */
	public static IVector getZeroVector (final int dimension) {
		final double[] elements = new double[dimension];
		
		return new VectorImpl(elements);
	}

	/**
	 * Adds two vectors.
	 * @param a vector
	 * @param b vector
	 * @return a + b
	 */
	public static IVector plus (final IVector a, final IVector b) {
		final int dimension = a.getDimension();
		
		if (dimension != b.getDimension())
			throw new RuntimeException("Dimensions of input vectors does not match");
	
		final double[] components = new double[dimension];
		
		for (int i = 0; i < dimension; i++)
			components[i] = a.getElement(i) + b.getElement(i);
		
		return new VectorImpl (components);
	}

	/**
	 * Subtracts two vectors.
	 * @param a vector
	 * @param b vector
	 * @return a - b
	 */
	public static IVector minus (final IVector a, final IVector b) {
		final int dimension = a.getDimension();
		
		if (dimension != b.getDimension())
			throw new RuntimeException("Dimensions of input vectors does not match");
		
		final double[] components = new double[dimension];
		
		for (int i = 0; i < dimension; i++)
			components[i] = a.getElement(i) - b.getElement(i);
		
		return new VectorImpl (components);
	}

	/**
	 * Multiplies vector by scalar.
	 * @param c scalar
	 * @param v vector
	 * @return c * v
	 */
	public static IVector multiply (final double c, final IVector v) {
		final int dimension = v.getDimension();
		final double[] components = new double[dimension];
		
		for (int i = 0; i < dimension; i++)
			components[i] = c * v.getElement(i);
		
		return new VectorImpl (components);
	}

	/**
	 * Multiplies two vectors element by element.
	 * @param a vector
	 * @param b vector
	 * @return vector with elements equal to products of corresponding elements of input vectors
	 */
	public static IVector elementMultiply (final IVector a, final IVector b) {
		final int dimension = a.getDimension();
		
		if (dimension != b.getDimension())
			throw new RuntimeException("Dimensions of input vectors does not match");
	
		final double[] components = new double[dimension];
		
		for (int i = 0; i < dimension; i++)
			components[i] = a.getElement(i) * b.getElement(i);
		
		return new VectorImpl (components);
	}

	public static IVector normalize(final IVector vec) {
		final double length = getLength(vec);
		
		return multiply(1 / length, vec);
	}

	public static double getLength(final IVector vec) {
		double squareSum = 0.0;
		
		for (int i = 0; i < vec.getDimension(); i++) {
			final double x = vec.getElement(i);
			squareSum += x*x;
		}
		
		return Math.sqrt(squareSum);
	}
	
	/**
	 * Applies given function to all elements of the vector and returns new vector.
	 * @param v vector
	 * @param f function
	 * @return f(v)
	 */
	public static IVector applyToElements(final IVector v, final DoubleUnaryOperator f) {
		final double[] result = new double[v.getDimension()];
		
		for (int i = 0; i < result.length; i++)
			result[i] = f.applyAsDouble(v.getElement(i));
		
		return new VectorImpl(result);
	}

	
}
