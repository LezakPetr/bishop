package bishopTests;

import org.junit.Assert;
import org.junit.Test;
import bishop.base.Fen;
import bishop.engine.Evaluation;
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
	public void testFindWin() throws Exception {
		final TestValue[] testValueArray = {
			new TestValue("k7/7Q/2K5/8/8/1n6/8/8 w - - 0 1", 1),
			new TestValue("3r3k/8/8/8/8/8/6PP/7K b - - 0 1", 1),
			new TestValue("3r3k/8/8/8/8/5B2/6PP/7K b - - 0 1", Integer.MAX_VALUE),
			new TestValue("r6k/6pp/1R6/2R5/6BB/8/6PP/7K b - - 0 1", 5),
			new TestValue("6rk/6pp/7N/8/8/8/5PPP/6K1 w - - 0 1", 1)			
		};
		
		final int maxDepth = 5;
		
		final Fen fen = new Fen();
		final MateFinder finder = new MateFinder();
		finder.setMaxDepth(maxDepth, 0);
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			finder.setPosition(fen.getPosition());
			
			for (int depth = 1; depth <= maxDepth; depth++) {
				final int evaluation = finder.findWin(depth);
				final int expectedEvaluation = (testValue.depth <= depth) ? Evaluation.getMateEvaluation(2 * testValue.depth - 1) : Evaluation.DRAW;
				
				Assert.assertEquals(testValue.positionFen, expectedEvaluation, evaluation);
			}
		}
	}
	
	@Test
	public void testFindLose() throws Exception {
		final TestValue[] testValueArray = {
			new TestValue("7k/8/8/1B6/8/8/6PP/3r3K w - - 0 1", 1),
			new TestValue("3r3k/8/8/8/8/5B2/6PP/7K b - - 0 1", Integer.MAX_VALUE),
			new TestValue("6rk/5Npp/8/8/8/8/5PPP/6K1 b - - 0 1", 0),
			new TestValue("5rQk/6pp/7N/8/8/8/5PPP/6K1 b - - 0 1", 1),
			new TestValue("5rk1/6pp/4Q2N/8/8/8/5PPP/6K1 b - - 0 1", 2),
			new TestValue("5r1k/5Npp/4Q3/8/8/8/5PPP/6K1 b - - 0 1", 3)
		};
		
		final int maxDepth = 4;
		
		final Fen fen = new Fen();
		final MateFinder finder = new MateFinder();
		finder.setMaxDepth(maxDepth, 0);
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			finder.setPosition(fen.getPosition());
			
			for (int depth = 1; depth <= maxDepth; depth++) {
				final int evaluation = finder.findLose(depth);
				final int expectedEvaluation = (testValue.depth <= depth) ? -Evaluation.getMateEvaluation(2 * testValue.depth) : Evaluation.DRAW;
				
				Assert.assertEquals(testValue.positionFen, expectedEvaluation, evaluation);
			}
		}
	}

}
