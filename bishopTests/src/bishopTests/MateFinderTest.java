package bishopTests;

import org.junit.Assert;
import org.junit.Test;
import bishop.base.Fen;
import bishop.engine.MateFinder;


public class MateFinderTest {
	private static class TestValue {
		public final String positionFen;
		public final int depth;
		
		public TestValue (final String positionFen, final int depth) {
			this.positionFen = positionFen;
			this.depth = depth;
		}
	}
	
	@Test
	public void searchEngineTest() throws Exception {
		final TestValue[] testValueArray = {
			new TestValue("k7/7Q/2K5/8/8/1n6/8/8 w - - 0 1", 1),
			new TestValue("k7/7Q/2K5/2n5/8/8/8/8 w - - 0 1", 2),
			new TestValue("3r3k/8/8/8/8/8/6PP/7K b - - 0 1", 1),
			new TestValue("3r3k/8/8/8/8/5B2/6PP/7K b - - 0 1", Integer.MAX_VALUE),
			new TestValue("3K4/8/3k4/8/3r4/8/8/8 b - - 0 1", 3),
			new TestValue("6K1/8/3k4/8/3r4/8/8/8 b - - 0 1", 5)
		};
		
		final int maxDepth = 4;
		
		final Fen fen = new Fen();
		final MateFinder finder = new MateFinder();
		finder.setMaxDepth(maxDepth);
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			finder.setPosition(fen.getPosition());
			
			for (int depth = 1; depth <= maxDepth; depth++) {
				final boolean isWin = finder.isWin(depth);
				
				Assert.assertEquals(testValue.depth <= depth, isWin);
			}
		}
	}
}
