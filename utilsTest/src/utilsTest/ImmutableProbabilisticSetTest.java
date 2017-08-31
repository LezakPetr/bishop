package utilsTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import collections.ImmutableList;
import collections.ImmutableProbabilisticSet;

public class ImmutableProbabilisticSetTest {

	private static final int SIZE = 256;
	private static final int NOT_CONTAIN_SIZE = 65536;
	
	private static final double FALSE_POSITIVE_PROBABILITY = 0.01;
	
	private static final Random RNG = new Random (42);
	
	private static final List<List<Integer>> TEST_DATA = ImmutableList.of(
		IntStream.range(0, NOT_CONTAIN_SIZE).boxed().collect (Collectors.toList()),
		IntStream.range(0, NOT_CONTAIN_SIZE).map (x -> 65536 * x).boxed().collect (Collectors.toList()),
		RNG.ints().limit(NOT_CONTAIN_SIZE).boxed().collect (Collectors.toList())
	);
	
	private static final Collection<Integer> getInsertedElements(final List<Integer> data) {
		return data.subList(0, SIZE);
	}
	
	private static final Collection<Integer> getNotInsertedElements(final List<Integer> data) {
		final HashSet<Integer> result = new HashSet<>(data);
		result.removeAll(getInsertedElements(data));
		
		return result;		
	}

	@Test
	public void testContainsEverything() {
		for (List<Integer> testData: TEST_DATA) {
			final Collection<Integer> insertedElements = getInsertedElements(testData);
			final ImmutableProbabilisticSet<Integer> set = new ImmutableProbabilisticSet<>(insertedElements, FALSE_POSITIVE_PROBABILITY);
			
			for (Integer element: insertedElements)
				Assert.assertTrue(set.contains(element));
		}
	}
	
	@Test
	public void testNotContainProbability() {
		for (List<Integer> testData: TEST_DATA) {
			final Collection<Integer> insertedElements = getInsertedElements(testData);
			final ImmutableProbabilisticSet<Integer> set = new ImmutableProbabilisticSet<>(insertedElements, FALSE_POSITIVE_PROBABILITY);
			final Collection<Integer> notInsertedElements = getNotInsertedElements(testData);
			int failCount = 0;
			
			for (Integer element: notInsertedElements) {
				if (set.contains(element))
					failCount++;
			}
			
			final double falsePositiveProbability = (double) failCount / (double) notInsertedElements.size();
			
			Assert.assertTrue(falsePositiveProbability < FALSE_POSITIVE_PROBABILITY * 1.2);
		}
	}
	
}
