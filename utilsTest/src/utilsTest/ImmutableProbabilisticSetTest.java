package utilsTest;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.Assert;
import org.junit.Test;

import collections.ImmutableList;
import collections.ImmutableProbabilisticSet;
import utils.Mixer;

public class ImmutableProbabilisticSetTest {

	private static final int SIZE = 256;
	private static final int NOT_CONTAIN_SIZE = 65536;
	
	private static final double FALSE_POSITIVE_PROBABILITY = 0.01;
	
	private static final Random RNG = new Random (42);
	
	
	private static class TestItem {
		private final long value;
		
		public TestItem (final long value) {
			this.value = value;
		}
		
		@Override
		public int hashCode() {
			return Mixer.mixLongToInt(value);
		}
		
		public boolean equals (final Object o) {
			if (o == null || o.getClass() != this.getClass())
				return false;
			
			final TestItem that = (TestItem) o;
			
			return this.value == that.value;
		}
	};
	
	private static List<TestItem> createSequentialList(final int shift) {
		return LongStream
				.range(0, NOT_CONTAIN_SIZE)
				.mapToObj(x -> new TestItem(x << shift))
				.collect (Collectors.toList());
	}
	
	private static final List<List<TestItem>> TEST_DATA = ImmutableList.of(
		createSequentialList (0),
		createSequentialList (16),
		createSequentialList (32),
		createSequentialList (48),
		RNG.longs().limit(NOT_CONTAIN_SIZE).mapToObj(x -> new TestItem (x)).collect (Collectors.toList())
	);
	
	private static final Collection<TestItem> getInsertedElements(final List<TestItem> data) {
		return data.subList(0, SIZE);
	}
	
	private static final Collection<TestItem> getNotInsertedElements(final List<TestItem> data) {
		final HashSet<TestItem> result = new HashSet<>(data);
		result.removeAll(getInsertedElements(data));
		
		return result;		
	}

	@Test
	public void testContainsEverything() {
		for (List<TestItem> testData: TEST_DATA) {
			final Collection<TestItem> insertedElements = getInsertedElements(testData);
			final ImmutableProbabilisticSet<TestItem> set = new ImmutableProbabilisticSet<>(insertedElements, FALSE_POSITIVE_PROBABILITY);
			
			for (TestItem element: insertedElements)
				Assert.assertTrue(set.contains(element));
		}
	}
	
	@Test
	public void testNotContainProbability() {
		for (List<TestItem> testData: TEST_DATA) {
			final Collection<TestItem> insertedElements = getInsertedElements(testData);
			final ImmutableProbabilisticSet<TestItem> set = new ImmutableProbabilisticSet<>(insertedElements, FALSE_POSITIVE_PROBABILITY);
			final Collection<TestItem> notInsertedElements = getNotInsertedElements(testData);
			int failCount = 0;
			
			for (TestItem element: notInsertedElements) {
				if (set.contains(element))
					failCount++;
			}
			
			final double falsePositiveProbability = (double) failCount / (double) notInsertedElements.size();
			System.out.println("False positive probability = " + falsePositiveProbability);
			
			Assert.assertTrue(falsePositiveProbability < FALSE_POSITIVE_PROBABILITY * 1.1);
		}
	}
	
}
