package math;

import java.util.function.DoubleUnaryOperator;

/**
 * UnaryVectorAlgorithm is an algorithm that applies operator to vector elements and then passes the result the processor.
 * @author Ing. Petr Ležák
 */
public interface IUnaryVectorAlgorithm {
	public <P extends IVectorElementProcessor> P processElements(final IVectorRead v, final DoubleUnaryOperator operator, final P processor);
}
