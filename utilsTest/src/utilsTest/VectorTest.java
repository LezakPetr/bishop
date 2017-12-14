package utilsTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.junit.Assert;
import org.junit.Test;

import math.Density;
import math.IVector;
import math.IVectorIterator;
import math.IVectorRead;
import math.Vectors;

public class VectorTest {
	private static final int[] SIZES = {0, 3, 10, 1024};
	
	private final Random rng = new Random();
	
	@Test
	public void fillTest() {
		fillTest (Vectors::dense);
		fillTest (Vectors::sparse);
	}
	
	private void fillTest (final IntFunction<IVector> vectorCreator) {
		for (int size: SIZES) {
			final double[] values = new double[size];
			final IVector vector = vectorCreator.apply(size);	
			
			fillRandomVector(size, vector, values);
			
			testVectorValues (vector, values);
			
			testVectorValues (
					vector.subVector(    0, size / 3),
					getSubArray (values, 0, size / 3)
			);
			
			testVectorValues (
					vector.subVector(    size / 4, size / 3),
					getSubArray (values, size / 4, size / 3)
			);
			
			testVectorValues (
					vector.subVector(    size / 5, size),
					getSubArray (values, size / 5, size)
			);
			
			testVectorValues (
					vector.subVector(                 size / 4, 3 * size / 4).subVector(size / 3, size / 2),
					getSubArray (getSubArray (values, size / 4, 3 * size / 4),          size / 3, size / 2)
			);
			
			testVectorValues (Vectors.copy(vector), values);
		}
	}

	private void fillRandomVector(final int size, final IVector vector, final double[] values) {
		for (int i = 0; i < 2 * size; i++) {
			final int index = rng.nextInt(size);
			final double value = (rng.nextBoolean()) ? 100 * rng.nextDouble() : 0.0;
			
			vector.setElement(index, value);
			values[index] = value;
		}
	}

	private double[] getSubArray(final double[] values, final int begin, final int end) {
		final double[] result = new double[end - begin];
		System.arraycopy(values, begin, result, 0, result.length);
		
		return result;
	}

	private void testVectorValues(final IVector vector, final double[] values) {
		Assert.assertEquals(values.length, vector.getDimension());
		
		// Getter
		for (int i = 0; i < values.length; i++)
			Assert.assertEquals(values[i], vector.getElement(i), 0.0);
		
		// Iterator
		double[] givenValues = new double[values.length];
		int lastIndex = -1;
		
		for (IVectorIterator it = vector.getNonZeroElementIterator(); it.isValid(); it.next()) {
			final int index = it.getIndex();
			givenValues[index] = it.getElement();
			
			Assert.assertTrue(index > lastIndex);
			lastIndex = index;
		}
		
		Assert.assertArrayEquals(values, givenValues, 0.0);
	}
	
	@Test
	public void testBinaryFunctionEquality() {
		testOneBinaryFunctionEquality(Vectors::plus);
		testOneBinaryFunctionEquality(Vectors::minus);
		testOneBinaryFunctionEquality(Vectors::elementMultiply);
	}
	
	private void testOneBinaryFunctionEquality(final BiFunction<IVectorRead, IVectorRead, ? extends IVectorRead> function) {
		for (int size: SIZES) {
			final List<IVectorRead> listA = createRandomVectorList(size);
			final List<IVectorRead> listB = createRandomVectorList(size);
			 
			IVectorRead lastResult = null;
			 
			for (IVectorRead a: listA) {
				for (IVectorRead b: listB) {
					final IVectorRead result = function.apply(a, b);
					 
					if (lastResult != null)
						Assert.assertEquals(lastResult, result);
				 
					lastResult = result;
				}
			}
		}
	}

	@Test
	public void testUnaryFunctionEquality() {
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

	private List<IVectorRead> createRandomVectorList(final int size) {
		final List<IVector> vectorList = new ArrayList<>();
		vectorList.add(Vectors.dense(size));
		vectorList.add(Vectors.sparse(size));
		
		for (int i = 0; i < size / 3; i++) {
			final double value = 100 * rng.nextDouble() - 50;
			final int index = rng.nextInt (size);
			
			for (IVector vector: vectorList)
				vector.setElement(index, value);
		}
		
		return new ArrayList<>(vectorList);
	}
	
	@Test
	public void testAssign() {
		for (int size: SIZES) {
			for (Density srcDensity: Density.values()) {
				for (Density targetDensity: Density.values()) {
					final IVectorRead source = createRandomVector(srcDensity, size).freeze();
					final IVector target = createRandomVector(targetDensity, size);
					target.assign(source);
					
					Assert.assertEquals(source, target);
				}
			}
		}
	}
	
	private IVector createRandomVector(final Density density, final int size) {
		final IVector vector = Vectors.vectorWithDensity(density, size);
		fillRandomVector(size, vector, new double[size]);
		
		return vector;
	}
	
	@Test
	public void testSwap() {
		for (int size: SIZES) {
			for (Density density1: Density.values()) {
				for (Density density2: Density.values()) {
					final IVector vector1 = createRandomVector(density1, size);
					final IVector vector2 = createRandomVector(density2, size);
					final int hash1 = vector1.hashCode();
					final int hash2 = vector2.hashCode();
					
					Vectors.swap(vector1, vector2);
					
					Assert.assertEquals(vector1.hashCode(), hash2);
					Assert.assertEquals(vector2.hashCode(), hash1);
				}
			}
		}
	}
}
