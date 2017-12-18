package math;

import java.util.function.DoubleBinaryOperator;

/**
 * BinaryVectorAlgorithm that ensures processing of pairs of elements which both are non-zero.
 * @author Ing. Petr Ležák
 */
public class BinaryVectorAlgorithmBothNonzero extends BinaryVectorAlgorithmBase {
	/**
	 * Processes corresponding elements of two vectors.
	 * @param a first vector
	 * @param b second vector
	 * @param operator binary operator. It must return zero if at least one arguments is zero. 
	 * @param processor processor that processes the results of operator
	 * @return processor
	 */
	@Override
	public <P extends IVectorElementProcessor> P processElements(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor) {
		checkPreconditions(a, b, operator, processor);
		
		if (a.density() == Density.SPARSE && b.density() == Density.SPARSE)
			return SPARSE_SPARSE_ALGORITHM.processElements(a, b, operator, processor);
		
		if (a.density() == Density.SPARSE)
			return SPARSE_DENSE_ALGORITHM.processElements(a, b, operator, processor);
		
		if (b.density() == Density.SPARSE)
			return SPARSE_DENSE_ALGORITHM.processElements(b, a, (x, y) -> operator.applyAsDouble (y, x), processor);
		
		return BinaryVectorAlgorithmDense.getInstance().processElements(a, b, operator, processor);
	}

	private static final IBinaryVectorAlgorithm INSTANCE = new BinaryVectorAlgorithmBothNonzero();
	
	private static final IBinaryVectorAlgorithm SPARSE_DENSE_ALGORITHM = new SparseDenseAlgorithm();
	private static final IBinaryVectorAlgorithm SPARSE_SPARSE_ALGORITHM = new SparseSparseAlgorithm();
	
	public static IBinaryVectorAlgorithm getInstance() {
		return INSTANCE;
	};

	private static class SparseDenseAlgorithm implements IBinaryVectorAlgorithm {
		@Override
		public <P extends IVectorElementProcessor> P processElements(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor) {
			final int dimension = a.getDimension();		
			processor.init (Density.SPARSE, dimension);
			
			final IVectorIterator itA = a.getNonZeroElementIterator();
			
			while (itA.isValid()) {
				final int index = itA.getIndex();
				
				processor.processElement(index, operator.applyAsDouble(itA.getElement(), b.getElement(index)));
				itA.next();
			}
			
			return processor;
		}

	}

	private static class SparseSparseAlgorithm implements IBinaryVectorAlgorithm {
		@Override
		public <P extends IVectorElementProcessor> P processElements(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor) {
			final int dimension = a.getDimension();		
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
			
			return processor;
		}
	}
}
