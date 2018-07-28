package math;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;

import collections.ImmutableOrdinalMap;

public class Vectors {
	
	/**
	 * Returns mutable dense zero vector with given dimension.
	 */
	public static IVector dense (final int dimension) {
		return new DenseVector(new double[dimension]);
	}

	/**
	 * Returns mutable sparse zero vector with given dimension.
	 */
	public static IVector sparse (final int dimension) {
		return new SparseVector(dimension);
	}
	
	private static final ImmutableOrdinalMap<Density, IntFunction<IVector>> CREATE_MAP = ImmutableOrdinalMap.<Density, IntFunction<IVector>>forEnum(Density.class)
		.put (Density.DENSE, Vectors::dense)
		.put (Density.SPARSE, Vectors::sparse)
		.build();
	
	/**
	 * Returns mutable zero vector with given dimension and density. 
	 */
	public static IVector vectorWithDensity(final Density density, final int dimension) {
		return CREATE_MAP.get(density).apply(dimension);
	}

	/**
	 * Returns immutable zero vector with given dimension.
	 * @param dimension dimension of vector
	 * @return zero vector
	 */
	public static IVectorRead getZeroVector (final int dimension) {
		return sparse(dimension).freeze();
	}

    /**
     * Returns immutable unit vector with given dimension.
     * @param index index of element with value 1
     * @param dimension dimension of vector
     * @return unit vector
     */
    public static IVectorRead getUnitVector (final int index, final int dimension) {
        final IVector result = sparse(dimension);
        result.setElement(index, 1);

        return result.freeze();
    }

	/**
	 * Returns dense vector with given elements.
	 * @param elements elements
	 * @return vector
	 */
	public static IVectorRead of (final double... elements) {
		final IVector v = dense(elements.length);

		for (int i = 0; i < elements.length; i++)
			v.setElement(i, elements[i]);

		return v.freeze();
	}

	/**
	 * Returns dense vector with given elements.
	 * @param elements elements
	 * @return vector
	 */
	public static IVectorRead of (final float... elements) {
		final IVector v = dense(elements.length);

		for (int i = 0; i < elements.length; i++)
			v.setElement(i, elements[i]);

		return v.freeze();
	}

	/**
	 * Adds two vectors.
	 * @param a vector
	 * @param b vector
	 * @return a + b
	 */
	public static IVectorRead plus (final IVectorRead a, final IVectorRead b) {
		return BinaryVectorAlgorithmOneNonzero.getInstance()
				.processElements(a, b, Double::sum, new VectorSetter())
				.getVector();
	}

	/**
	 * Subtracts two vectors.
	 * @param a vector
	 * @param b vector
	 * @return a - b
	 */
	public static IVectorRead minus (final IVectorRead a, final IVectorRead b) {
		return BinaryVectorAlgorithmOneNonzero.getInstance()
				.processElements(a, b, (x, y) -> x - y, new VectorSetter())
				.getVector();
	}

	/**
	 * Multiplies vector by scalar.
	 * @param c scalar
	 * @param v vector
	 * @return c * v
	 */
	public static IVectorRead multiply (final double c, final IVectorRead v) {
		return UnaryVectorAlgorithm.getInstance().processElements(v, x -> c * x, new VectorSetter()).getVector();
	}

	/**
	 * Multiplies two vectors element by element.
	 * @param a vector
	 * @param b vector
	 * @return vector with elements equal to products of corresponding elements of input vectors
	 */
	public static IVectorRead elementMultiply (final IVectorRead a, final IVectorRead b) {
		return BinaryVectorAlgorithmBothNonzero.getInstance()
				.processElements(a, b, (x, y) -> x * y, new VectorSetter())
				.getVector();
	}

	/**
	 * Makes the vector unit. Result is undefined for zero vector.
	 * @param vec vector
	 * @return unit vector with same direction
	 */
	public static IVectorRead normalize(final IVectorRead vec) {
		final double length = getLength(vec);
		
		return multiply(1 / length, vec);
	}

	/**
	 * Returns length of the vector.
	 * @param vec vector
	 * @return length
	 */
	public static double getLength(final IVectorRead vec) {
		final double squareSum = UnaryVectorAlgorithm.getInstance().processElements(vec, x -> x*x, new VectorElementSum()).getSum();
		
		return Math.sqrt(squareSum);
	}

	public static void swap(final IVector a, final IVector b) {
		if (a == b)
			return;
		
		// Swapping dense vectors
		if (a instanceof DenseVector && b instanceof DenseVector) {
			final DenseVector vectorA = (DenseVector) a;
			final DenseVector vectorB = (DenseVector) b;
			
			vectorA.swap(vectorB);
			
			return;
		}
		
		// Swapping sparse vectors
		if (a instanceof SparseVector && b instanceof SparseVector) {
			final SparseVector vectorA = (SparseVector) a;
			final SparseVector vectorB = (SparseVector) b;
			
			vectorA.swap(vectorB);
			
			return;
		}

		// General method
		final IVector tmp = copy(a);
		a.assign(b);
		b.assign(tmp);
	}

	/**
	 * Calculates a + b*coeff
	 */
	public static IVectorRead multiplyAndAdd(final IVectorRead a, final IVectorRead b, final double coeff) {
		return BinaryVectorAlgorithmOneNonzero.getInstance()
				.processElements(a, b, (x, y) -> x + coeff * y, new VectorSetter())
				.getVector();
	}

	/**
	 * Calculates dot product of two vectors.
	 */
	public static double dotProduct(final IVectorRead a, final IVectorRead b) {
		return BinaryVectorAlgorithmBothNonzero.getInstance()
				.processElements(a, b, (x, y) -> x * y, new VectorElementSum())
				.getSum();
	}
	
	/**
	 * Returns mutable copy of given vector.
	 */
	public static IVector copy(final IVectorRead orig) {
		return UnaryVectorAlgorithm.getInstance().processElements(orig, DoubleUnaryOperator.identity(), new VectorSetter()).getMutableVector();
	}

	/**
	 * Returns immutable copy of given vector.
	 */
	public static IVectorRead immutableCopy(final IVectorRead orig) {
		if (orig.isImmutable())
			return orig;
		else
			return copy(orig).freeze();
	}

	/**
	 * Returns frozen random vector with elements in range <-1; +1>.
	 * @param rng random number generator
	 * @param dimension dimension of the vector
	 * @return random vector
	 */
	public static IVectorRead getRandomVector (final Random rng, final int dimension) {
		final IVector v = Vectors.dense(dimension);

		for (int i = 0; i < dimension; i++)
			v.setElement(i, 2 * rng.nextDouble() - 1);

		return v.freeze();
	}

	private static Density maxDensity (final Density a, final Density b) {
		return (a == Density.SPARSE || b == Density.SPARSE) ? Density.SPARSE : Density.DENSE;
	}

	public static IMatrixRead cartesianProduct (final IVectorRead a, final IVectorRead b) {
		final int rowCount = a.getDimension();
		final int columnCount = b.getDimension();

		final IMatrix result = Matrices.createMutableMatrix(maxDensity(a.density(), b.density()), rowCount, columnCount);

		for (IVectorIterator itA = a.getNonZeroElementIterator(); itA.isValid(); itA.next()) {
			for (IVectorIterator itB = a.getNonZeroElementIterator(); itB.isValid(); itB.next()) {
				result.setElement(itA.getIndex(), itB.getIndex(), itA.getElement() * itB.getElement());
			}
		}

		return result;
	}

}
