package utilsTest;

import org.junit.Assert;
import org.junit.Test;
import utils.IntUtils;

public class IntUtilsTest {
	@Test
	public void ceilLogInt() {
		Assert.assertEquals(0, IntUtils.ceilLog(1));
		Assert.assertEquals(1, IntUtils.ceilLog(2));
		Assert.assertEquals(2, IntUtils.ceilLog(3));
		Assert.assertEquals(2, IntUtils.ceilLog(4));
		Assert.assertEquals(3, IntUtils.ceilLog(7));
		Assert.assertEquals(3, IntUtils.ceilLog(8));
		Assert.assertEquals(32, IntUtils.ceilLog(0xFFFF_FFFF));
	}

	@Test
	public void ceilLogLong() {
		Assert.assertEquals(0, IntUtils.ceilLog(1L));
		Assert.assertEquals(1, IntUtils.ceilLog(2L));
		Assert.assertEquals(2, IntUtils.ceilLog(3L));
		Assert.assertEquals(2, IntUtils.ceilLog(4L));
		Assert.assertEquals(3, IntUtils.ceilLog(7L));
		Assert.assertEquals(3, IntUtils.ceilLog(8L));
		Assert.assertEquals(32, IntUtils.ceilLog(0xFFFF_FFFFL));
		Assert.assertEquals(64, IntUtils.ceilLog(0xFFFF_FFFF_FFFF_FFFFL));
	}

	@Test
	public void ceilLogDouble() {
		Assert.assertEquals(0, IntUtils.ceilLog(1.0));
		Assert.assertEquals(1, IntUtils.ceilLog(2.0));
		Assert.assertEquals(2, IntUtils.ceilLog(3.0));
		Assert.assertEquals(2, IntUtils.ceilLog(4.0));
		Assert.assertEquals(3, IntUtils.ceilLog(7.0));
		Assert.assertEquals(3, IntUtils.ceilLog(8.0));
		Assert.assertEquals(32, IntUtils.ceilLog(Math.pow(2, 32) - 1));
		Assert.assertEquals(32, IntUtils.ceilLog(Math.pow(2, 32)));
		Assert.assertEquals(64, IntUtils.ceilLog(Math.pow(2, 64) - 1));
		Assert.assertEquals(64, IntUtils.ceilLog(Math.pow(2, 64)));
	}

	@Test
	public void divideRoundUpInt() {
		Assert.assertEquals(0, IntUtils.divideRoundUp(0, 1));
		Assert.assertEquals(3, IntUtils.divideRoundUp(8, 3));
		Assert.assertEquals(3, IntUtils.divideRoundUp(9, 3));
		Assert.assertEquals(4, IntUtils.divideRoundUp(10, 3));
	}

	@Test
	public void divideRoundUpLong() {
		Assert.assertEquals(0L, IntUtils.divideRoundUp(0L, 1L));
		Assert.assertEquals(3L, IntUtils.divideRoundUp(8L, 3L));
		Assert.assertEquals(3L, IntUtils.divideRoundUp(9L, 3L));
		Assert.assertEquals(4L, IntUtils.divideRoundUp(10L, 3L));
	}

	@Test
	public void testGetLowestBitsLong() {
		Assert.assertEquals(0x0000_0000_0000_0000L, IntUtils.getLowestBitsLong(0));
		Assert.assertEquals(0x0000_0000_0000_0001L, IntUtils.getLowestBitsLong(1));
		Assert.assertEquals(0x0000_0000_0000_0003L, IntUtils.getLowestBitsLong(2));
		Assert.assertEquals(0x0000_0000_0000_0007L, IntUtils.getLowestBitsLong(3));
		Assert.assertEquals(0x7FFF_FFFF_FFFF_FFFFL, IntUtils.getLowestBitsLong(63));
		Assert.assertEquals(0xFFFF_FFFF_FFFF_FFFFL, IntUtils.getLowestBitsLong(64));
	}
}
