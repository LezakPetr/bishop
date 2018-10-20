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
	 * Adds second vector to the first vector. Vectors cannot be modified by the operation,
	 * so calling addTo on same vectors or subvectors of same vector is not legal.
	 * @param a target vector
	 * @param b source vector
	 */
	public static void addInPlace (final IVector a, final IVectorRead b) {
		assert a != b;

		for (IVectorIterator it = b.getNonZeroElementIterator(); it.isValid(); it.next()) {
			final int index = it.getIndex();

			a.setElement(
				index,
				a.getElement(index) + it.getElement()
			);
		}
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
		final IVector tmp = a.copy();
		a.assign(b);
		b.assign(tmp);
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
			final IVector resultRow = result.getRowVector(itA.getIndex());

			for (IVectorIterator itB = a.getNonZeroElementIterator(); itB.isValid(); itB.next()) {
				resultRow.setElement(itB.getIndex(), itA.getElement() * itB.getElement());
			}
		}

		return result;
	}

}
