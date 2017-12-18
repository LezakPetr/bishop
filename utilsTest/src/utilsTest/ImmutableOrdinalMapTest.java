package utilsTest;

import org.junit.Assert;
import org.junit.Test;

import collections.ImmutableOrdinalMap;


public class ImmutableOrdinalMapTest {

	enum TestEnum {
		FIRST, SECOND
	}
	
	@Test
	public void testEnum() {
		final ImmutableOrdinalMap<TestEnum, Integer> map = ImmutableOrdinalMap.<TestEnum, Integer>forEnum(TestEnum.class)
			.put(TestEnum.FIRST, 1)
			.put(TestEnum.SECOND, 2)
			.build();
		
		Assert.assertEquals((Integer) 1, map.get(TestEnum.FIRST));
		Assert.assertEquals((Integer) 2, map.get(TestEnum.SECOND));
	}
	
	@Test
	public void testInteger() {
		final ImmutableOrdinalMap<Integer, Integer> map = ImmutableOrdinalMap.<Integer, Integer>forMapper(Integer::intValue, 4)
			.put(1, 2)
			.put(2, 3)
			.put(3, 4)
			.build();
		
		Assert.assertEquals((Integer) 2, map.get(1));
		Assert.assertEquals((Integer) 3, map.get(2));
		Assert.assertEquals((Integer) 4, map.get(3));
	}

}
