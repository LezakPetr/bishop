package utilsTest;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import utils.BitNumberArray;
import utils.INumberArray;

public class NumberArrayTest {

	private void testArray (final INumberArray array) {
		randomInitialize (array);
		
		for (int i = 0; i < array.getSize(); i++) {
			array.setAt(i, i % array.getMaxElement());
		}
		
		for (int i = 0; i < array.getSize(); i++) {
			final int returned = array.getAt(i);
			final int expected = i % array.getMaxElement();
			
			Assert.assertEquals(expected, returned);
		}
	}
	
	private void randomInitialize(final INumberArray array) {
		final Random rnd = new Random(6516);
		
		for (int i = 0; i < array.getSize(); i++) {
			array.setAt(i, rnd.nextInt(array.getMaxElement()));
		}
	}

	@Test
	public void testBitNumberArray() {
		testArray(new BitNumberArray(1024, 9));
	}
	
}
