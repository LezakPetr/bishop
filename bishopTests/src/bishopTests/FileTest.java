package bishopTests;

import bishop.base.File;
import org.junit.Assert;
import org.junit.Test;

public class FileTest {
	@Test
	public void getOppositeFileTest() {
		Assert.assertEquals(File.FH, File.getOppositeFile(File.FA));
		Assert.assertEquals(File.FG, File.getOppositeFile(File.FB));
		Assert.assertEquals(File.FF, File.getOppositeFile(File.FC));
		Assert.assertEquals(File.FE, File.getOppositeFile(File.FD));
		Assert.assertEquals(File.FD, File.getOppositeFile(File.FE));
		Assert.assertEquals(File.FC, File.getOppositeFile(File.FF));
		Assert.assertEquals(File.FB, File.getOppositeFile(File.FG));
		Assert.assertEquals(File.FA, File.getOppositeFile(File.FH));
	}
}
