package math;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

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
	
	/**
	 * Returns mutable zero vector with given dimension and density. 
	 */
	public static IVector vectorWithDensity(final Density density, final int dimension) {
		switch (density) {
			case DENSE:
				return dense(dimension);
			
			case SPARSE:
				return sparse(dimension);
		
			default:
				throw new RuntimeException("Unknown density " + density);
		}
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
	 * Processes corresponding elements of two vectors.
	 * @param a first vector
	 * @param b second vector
	 * @param operator binary operator. It must return zero if both arguments are zero. 
	 * @param processor processor that processes the results of operator
	 * @return processor
	 */
	public static <P extends IVectorElementProcessor> P processElementsBinaryOneNonzero (final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);
		Objects.requireNonNull(operator);
		Objects.requireNonNull(processor);
		
		if (a.density() == Density.SPARSE && b.density() == Density.SPARSE)
			processElementsBinaryOneNonzeroSparse(a, b, operator, processor);
		else
			processElementsBinaryDense(a, b, operator, processor);
		
		return processor;
	}

	/**
	 * Processes corresponding elements of two vectors. Dense version.
	 */
	private static void processElementsBinaryDense(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final IVectorElementProcessor processor) {		
		final int dimension = a.getDimension();
		
		if (b.getDimension() != dimension)
			throw new RuntimeException("Dimensions of input vectors does not match");
		
		processor.init (Density.DENSE, dimension);
		
		for (int index = 0; index < dimension; index++)
			processor.processElement(index, operator.applyAsDouble(a.getElement(index), b.getElement(index)));
	}

	/**
	 * Processes corresponding elements of two vectors. Both vectors should be sparse.
	 * Assumes that the operator returns zero if both its arguments are zero.
	 */
	private static void processElementsBinaryOneNonzeroSparse(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final IVectorElementProcessor processor) {
		final int dimension = a.getDimension();
		
		if (b.getDimension() != dimension)
			throw new RuntimeException("Dimensions of input vectors does not match");
		
		processor.init (Density.SPARSE, dimension);
		
		final IVectorIterator itA = a.getNonZeroElementIterator();
		final IVectorIterator itB = b.getNonZeroElementIterator();
		
		while (itA.isValid() && itB.isValid()) {
			final int indexA = itA.getIndex();
			final int indexB = itB.getIndex();
			final double valueA;
			final double valueB;
			
			if (indexA <= indexB) {
				valueA = itA.getElement();
				itA.next();
			}
			else
				valueA = 0.0;

			if (indexB <= indexA) {
				valueB = itB.getElement();
				itB.next();
			}
			else
				valueB = 0.0;
			
			processor.processElement(Math.min(indexA, indexB), operator.applyAsDouble(valueA, valueB));
		}
		
		while (itA.isValid()) {
			processor.processElement(itA.getIndex(), operator.applyAsDouble(itA.getElement(), 0.0));
			itA.next();
		}
		
		while (itB.isValid()) {
			processor.processElement(itB.getIndex(), operator.applyAsDouble(0.0, itB.getElement()));
			itB.next();
		}
	}
	
	/**
	 * Processes corresponding elements of two vectors.
	 * @param a first vector
	 * @param b second vector
	 * @param operator binary operator. It must return zero if at least one arguments is zero. 
	 * @param processor processor that processes the results of operator
	 * @return processor
	 */
	public static <P extends IVectorElementProcessor> P processElementsBinaryBothNonzero(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);
		Objects.requireNonNull(operator);
		
		if (a.density() == Density.SPARSE && b.density() == Density.SPARSE) {
			processElementsBinaryBothNonzeroSparseSparse(a, b, operator, processor);
			
			return processor;
		}
		
		if (a.density() == Density.SPARSE) {
			processElementsBinaryBothNonzeroSparseDense(a, b, operator, processor);
			
			return processor;
		}
		
		if (b.density() == Density.SPARSE) {
			processElementsBinaryBothNonzeroSparseDense(b, a, (x, y) -> operator.applyAsDouble (y, x), processor);
			
			return processor;
		}
		
		processElementsBinaryDense(a, b, operator, processor);
		
		return processor;
	}
	
	/**
	 * Processes corresponding elements of two vectors. Both vectors should be sparse.
	 * Assumes that the operator returns zero if at least one of its arguments is zero.
	 */
	private static void processElementsBinaryBothNonzeroSparseSparse(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final IVectorElementProcessor processor) {
		final int dimension = a.getDimension();
		
		if (b.getDimension() != dimension)
			throw new RuntimeException("Dimensions of input vectors does not match");
		
		processor.init (Density.SPARSE, dimension);
		
		final IVectorIterator itA = a.getNonZeroElementIterator();
		final IVectorIterator itB = b.getNonZeroElementIterator();
		
		while (itA.isValid() && itB.isValid()) {
			final int indexA = itA.getIndex();
			final int indexB = itB.getIndex();
			
			if (indexA == indexB)
				processor.processElement(indexA, operator.applyAsDouble(itA.getElement(), itB.getElement()));
			
			if (indexA <= indexB)
				itA.next();

			if (indexB <= indexA)
				itB.next();
		}
	}

	/**
	 * Processes corresponding elements of two vectors. First vector should be sparse, second dense.
	 * Assumes that the operator returns zero if at least one of its arguments is zero.
	 */
	private static void processElementsBinaryBothNonzeroSparseDense(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final IVectorElementProcessor processor) {
		final int dimension = a.getDimension();
		
		if (b.getDimension() != dimension)
			throw new RuntimeException("Dimensions of input vectors does not match");
		
		processor.init (Density.SPARSE, dimension);
		
		final IVectorIterator itA = a.getNonZeroElementIterator();
		
		while (itA.isValid()) {
			final int index = itA.getIndex();
			
			processor.processElement(index, operator.applyAsDouble(itA.getElement(), b.getElement(index)));
			itA.next();
		}
	}
	
	/**
	 * Processes all elements of the vector.
	 * @param v vector
	 * @param f operator
	 * @param processor processor that processes the results of operator
	 * @return processor
	 */
	public static <T extends IVectorElementProcessor> T processElementsUnary(final IVectorRead v, final DoubleUnaryOperator f, final T processor) {
		Objects.requireNonNull(v);
		
		switch (v.density()) {
			case DENSE:
				processElementsUnaryDense(v, f, processor);
				return processor;
				
			case SPARSE:
				processElementsUnarySparse(v, f, processor);
				return processor;
				
			default:
				throw new RuntimeException("Unknown density");
		}
	}
	
	/**
	 * Processes all elements of the vector. Unary dense version.
	 */
	private static void processElementsUnaryDense(final IVectorRead v, final DoubleUnaryOperator f, final IVectorElementProcessor processor) {
		final int dimension  = v.getDimension();
		processor.init(Density.DENSE, dimension);
		
		for (int i = 0; i < dimension; i++)
			processor.processElement(i, f.applyAsDouble(v.getElement(i)));
	}

	/**
	 * Processes all elements of the vector. Unary sparse version.
	 * Assumes that the operator returns zero for zero argument.
	 */
	private static void processElementsUnarySparse(final IVectorRead v, final DoubleUnaryOperator f, final IVectorElementProcessor processor) {
		final int dimension  = v.getDimension();
		processor.init(Density.DENSE, dimension);
		
		for (IVectorIterator it = v.getNonZeroElementIterator(); it.isValid(); it.next())
			processor.processElement(it.getIndex(), f.applyAsDouble(it.getElement()));
	}
	
	/**
	 * Adds two vectors.
	 * @param a vector
	 * @param b vector
	 * @return a + b
	 */
	public static IVectorRead plus (final IVectorRead a, final IVectorRead b) {
		return processElementsBinaryOneNonzero(a, b, Double::sum, new VectorSetter()).getVector();
	}

	/**
	 * Subtracts two vectors.
	 * @param a vector
	 * @param b vector
	 * @return a - b
	 */
	public static IVectorRead minus (final IVectorRead a, final IVectorRead b) {
		return processElementsBinaryOneNonzero(a, b, (x, y) -> x - y, new VectorSetter()).getVector();
	}

	/**
	 * Multiplies vector by scalar.
	 * @param c scalar
	 * @param v vector
	 * @return c * v
	 */
	public static IVectorRead multiply (final double c, final IVectorRead v) {
		return processElementsUnary(v, x -> c * x, new VectorSetter()).getVector();
	}

	/**
	 * Multiplies two vectors element by element.
	 * @param a vector
	 * @param b vector
	 * @return vector with elements equal to products of corresponding elements of input vectors
	 */
	public static IVectorRead elementMultiply (final IVectorRead a, final IVectorRead b) {
		return processElementsBinaryBothNonzero(a, b, (x, y) -> x * y, new VectorSetter()).getVector();
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
		final double squareSum = processElementsUnary(vec, x -> x*x, new VectorElementSum()).getSum();
		
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
		return processElementsBinaryOneNonzero(a, b, (x, y) -> x + coeff * y, new VectorSetter()).getVector();
	}

	/**
	 * Calculates dot product of two vectors.
	 */
	public static double dotProduct(final IVectorRead a, final IVectorRead b) {
		return processElementsBinaryBothNonzero(a, b, (x, y) -> x * y, new VectorElementSum()).getSum();
	}
	
	/**
	 * Returns mutable copy of given vector.
	 */
	public static IVector copy(final IVectorRead orig) {
		return processElementsUnary(orig, DoubleUnaryOperator.identity(), new VectorSetter()).getMutableVector();
	}
		
}
