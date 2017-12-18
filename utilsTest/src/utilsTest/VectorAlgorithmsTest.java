package utilsTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import math.BinaryVectorAlgorithmBothNonzero;
import math.BinaryVectorAlgorithmOneNonzero;
import math.IBinaryVectorAlgorithm;
import math.IVector;
import math.IVectorRead;
import math.VectorSetter;
import math.Vectors;

public class VectorAlgorithmsTest {
	
	private static final int[] SIZES = {0, 3, 10, 1024};
	private final Random rng = new Random();
	
	
	@Test
	public void testSameResultsOfBinaryAlgorithms() {
		// The functions should be non-commutative function to test it properly
		testSameResultsOfBinaryAlgorithm(BinaryVectorAlgorithmOneNonzero.getInstance(), (x, y) -> x - y);
		testSameResultsOfBinaryAlgorithm(BinaryVectorAlgorithmBothNonzero.getInstance(), (x, y) -> x*y*y);
	}

	private void testSameResultsOfBinaryAlgorithm (final IBinaryVectorAlgorithm algorithm, final DoubleBinaryOperator function) {
		for (int size: SIZES) {
			final List<IVectorRead> listA = createRandomVectorList(size);
			final List<IVectorRead> listB = createRandomVectorList(size);

			IVectorRead lastResult = null;
			 
			for (IVectorRead a: listA) {
				for (IVectorRead b: listB) {
					final IVectorRead result = algorithm.processElements(a, b, function, new VectorSetter()).getVector();
					
					if (lastResult != null)
						Assert.assertEquals(lastResult, result);
				 
					lastResult = result;
				}
			}
		}
	}
	
	private List<IVectorRead> createRandomVectorList(final int size) {
		final List<IVector> vectorList = new ArrayList<>();
		vectorList.add(Vectors.dense(size));
		vectorList.add(Vectors.sparse(size));
		
		for (int i = 0; i < size / 3; i++) {
			final double value = rng.nextInt(100) - 50;
			final int index = rng.nextInt (size);
			
			for (IVector vector: vectorList)
				vector.setElement(index, value);
		}
		
		return new ArrayList<>(vectorList);
	}
	
	@Test
	public void testSameResultsOfUnaryAlgorithms() {
		testOneUnaryFunctionEquality(v -> Vectors.multiply(2.0, v));
	}
	
	private void testOneUnaryFunctionEquality(final Function<IVectorRead, ? extends IVectorRead> function) {
		for (int size: SIZES) {
			final List<IVectorRead> list = createRandomVectorList(size);
			 
			IVectorRead lastResult = null;
			 
			for (IVectorRead v: list) {
				final IVectorRead result = function.apply(v);
				 
				if (lastResult != null)
					Assert.assertEquals(lastResult, result);
			 
				lastResult = result;
			}
		}
	}

}
