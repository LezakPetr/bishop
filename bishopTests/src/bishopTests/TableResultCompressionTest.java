package bishopTests;

import org.junit.Assert;

import org.junit.Test;

import bishop.tablebase.TableResult;

public class TableResultCompressionTest {

	@Test
	public void compressionTest() {
		for (int result = Short.MIN_VALUE; result < Short.MAX_VALUE; result++) {
			if (TableResult.canBeCompressed(result)) {
				final byte compressedResult = TableResult.compress(result);
				final int decompressedResult = TableResult.decompress(compressedResult);
				
				Assert.assertEquals(result, decompressedResult);
			}
		}
	}

}
