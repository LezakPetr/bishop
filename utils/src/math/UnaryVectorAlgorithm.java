package math;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

import collections.ImmutableOrdinalMap;

/**
 * UnaryVectorAlgorithm is an algorithm that applies operator to vector elements and then passes the result the processor.
 * @author Ing. Petr Ležák
 */
public class UnaryVectorAlgorithm implements IUnaryVectorAlgorithm {
	/**
	 * Processes all elements of the vector.
	 * @param v vector
	 * @param operator operator
	 * @param processor processor that processes the results of operator
	 * @return processor
	 */
	public <P extends IVectorElementProcessor> P processElements(final IVectorRead v, final DoubleUnaryOperator operator, final P processor) {
		Objects.requireNonNull(v);
		Objects.requireNonNull(operator);
		Objects.requireNonNull(processor);
		
		final Density density = v.density();

		return ALGORITHM_MAP.get(density).processElements (v, operator, processor);
	}
	
	private static final ImmutableOrdinalMap<Density, IUnaryVectorAlgorithm> ALGORITHM_MAP = ImmutableOrdinalMap.<Density, IUnaryVectorAlgorithm>forEnum(Density.class)
			.put(Density.DENSE, new DenseAlgorithm())
			.put(Density.SPARSE, new SparseAlgorithm())
			.build();
	
	private static final IUnaryVectorAlgorithm INSTANCE = new UnaryVectorAlgorithm();
	
	public static IUnaryVectorAlgorithm getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Processes all elements of the vector. Unary dense version.
	 */
	private static class DenseAlgorithm implements IUnaryVectorAlgorithm {
		public <P extends IVectorElementProcessor> P processElements(final IVectorRead v, final DoubleUnaryOperator operator, final P processor) {
			final int dimension = v.getDimension();
			processor.init(Density.DENSE, dimension);
			
			for (int i = 0; i < dimension; i++)
				processor.processElement(i, operator.applyAsDouble(v.getElement(i)));
			
			return processor;
		}
	}
	
	/**
	 * Processes all elements of the vector. Unary sparse version.
	 * Assumes that the operator returns zero for zero argument.
	 */
	private static class SparseAlgorithm implements IUnaryVectorAlgorithm {
		public <P extends IVectorElementProcessor> P processElements(final IVectorRead v, final DoubleUnaryOperator operator, final P processor) {
			final int dimension  = v.getDimension();
			processor.init(Density.SPARSE, dimension, v.getNonZeroElementCount());
			
			for (IVectorIterator it = v.getNonZeroElementIterator(); it.isValid(); it.next())
				processor.processElement(it.getIndex(), operator.applyAsDouble(it.getElement()));
			
			return processor;
		}
	}

}
