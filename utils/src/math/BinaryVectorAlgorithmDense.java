package math;

import java.util.function.DoubleBinaryOperator;

/**
 * BinaryVectorAlgorithm - dense version. Processes all pairs of elements.
 * @author Ing. Petr Ležák
 */
class BinaryVectorAlgorithmDense extends BinaryVectorAlgorithmBase {
	
	public <P extends IVectorElementProcessor> P processElements(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor) {
		checkPreconditions(a, b, operator, processor);
		
		final int dimension = a.getDimension();		
		processor.init (Density.DENSE, dimension);
		
		for (int index = 0; index < dimension; index++)
			processor.processElement(index, operator.applyAsDouble(a.getElement(index), b.getElement(index)));
		
		return processor;
	}

	private static final IBinaryVectorAlgorithm INSTANCE = new BinaryVectorAlgorithmDense();
	
	public static IBinaryVectorAlgorithm getInstance() {
		return INSTANCE;
	};

}
