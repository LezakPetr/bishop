package math;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;


/**
 * BinaryVectorAlgorithm that ensures processing of pairs of elements which at least one is non-zero.
 * @author Ing. Petr Ležák
 */
public class BinaryVectorAlgorithmOneNonzero extends BinaryVectorAlgorithmBase {
	/**
	 * Processes corresponding elements of two vectors.
	 * @param a first vector
	 * @param b second vector
	 * @param operator binary operator. It must return zero if both arguments are zero. 
	 * @param processor processor that processes the results of operator
	 * @return processor
	 */
	public <P extends IVectorElementProcessor> P processElements(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor) {
		checkPreconditions(a, b, operator, processor);
				
		final IBinaryVectorAlgorithm algorithm = selectAlgorithm(a, b);
		
		return algorithm.processElements(a, b, operator, processor);
	}

	private IBinaryVectorAlgorithm selectAlgorithm(final IVectorRead a, final IVectorRead b) {
		if (a.density() == Density.SPARSE && b.density() == Density.SPARSE)
			return SPARSE_ALGORITHM;
		else
			return BinaryVectorAlgorithmDense.getInstance();
	}
	
	private static final IBinaryVectorAlgorithm INSTANCE = new BinaryVectorAlgorithmOneNonzero();
	
	private static final IBinaryVectorAlgorithm SPARSE_ALGORITHM = new SparseAlgorithm();
	
	public static IBinaryVectorAlgorithm getInstance() {
		return INSTANCE;
	};

	private static class SparseAlgorithm extends BinaryVectorAlgorithmBase {
		@Override
		public <P extends IVectorElementProcessor> P processElements(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor) {
			final int dimension = a.getDimension();		
			processor.init (Density.SPARSE, dimension);
			
			final IVectorIterator itA = a.getNonZeroElementIterator();
			final IVectorIterator itB = b.getNonZeroElementIterator();
			
			processBothOperatorsValid(itA, itB, operator, processor);
			processOneIteratorValid(itA, x -> operator.applyAsDouble (x, 0.0), processor);
			processOneIteratorValid(itB, x -> operator.applyAsDouble (0.0, x), processor);
			
			return processor;
		}

		private <P extends IVectorElementProcessor> void processBothOperatorsValid(final IVectorIterator itA, final IVectorIterator itB, final DoubleBinaryOperator operator, final P processor) {
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
		}

		private <P extends IVectorElementProcessor> void processOneIteratorValid(final IVectorIterator it, final DoubleUnaryOperator operator, final P processor) {
			while (it.isValid()) {
				processor.processElement(it.getIndex(), operator.applyAsDouble(it.getElement()));
				it.next();
			}
		}
	}
}
