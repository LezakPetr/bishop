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
		finder.setMaxDepth(maxDepth, 0, 0);
		
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
		
		finder.setMaxDepth(maxDepth, 0, 0);
		
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
	
	@Test
	public void testSingularWin() throws Exception {
		final TestValue[] testValueArray = {
			new TestValue("r6k/5ppp/3N4/8/2Q5/B7/5PPP/6K1 w - - 0 1", 4)
		};
		
		final int maxDepth = 1;
		
		final Fen fen = new Fen();
		final MateFinder finder = new MateFinder();
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			finder.setPosition(fen.getPosition());
			
			for (int extension = 0; extension <= maxDepth; extension++) {
				finder.setMaxDepth(maxDepth, 0, extension);
				
				final int evaluation = finder.findWin(maxDepth);
				final int expectedEvaluation = (testValue.depth <= maxDepth + extension) ? Evaluation.getMateEvaluation(2 * testValue.depth - 1) : Evaluation.DRAW;
				
				Assert.assertEquals(testValue.positionFen, expectedEvaluation, evaluation);
			}
		}
	}

	@Test
	public void testSingularLose() throws Exception {
		final TestValue[] testValueArray = {
			new TestValue("r6k/5Npp/8/8/2Q5/B7/5PPP/6K1 b - - 0 1", 4)
		};
		
		final int maxDepth = 1;
		
		final Fen fen = new Fen();
		final MateFinder finder = new MateFinder();
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			finder.setPosition(fen.getPosition());
			
			for (int extension = 0; extension <= maxDepth; extension++) {
				finder.setMaxDepth(maxDepth, 0, extension);
				
				final int evaluation = finder.findLose(maxDepth);
				final int expectedEvaluation = (testValue.depth <= maxDepth + extension) ? Evaluation.getMateEvaluation(2 * testValue.depth - 1) : Evaluation.DRAW;
				
				Assert.assertEquals(testValue.positionFen, expectedEvaluation, evaluation);
			}
		}
	}

	@Test
	public void testExternalSingularNonLose() throws Exception {
		final TestValue[] testValueArray = {
			new TestValue("2R3k1/3q1ppp/B7/8/8/8/5PPP/6K1 b - - 0 1", 2)
		};
		
		final int maxDepth = 2;
		
		final Fen fen = new Fen();
		final MateFinder finder = new MateFinder();
		finder.setMaxDepth(maxDepth, 0, 0);
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			finder.setPosition(fen.getPosition());
			
			final int evaluation = finder.findLose(testValue.depth);
			Assert.assertEquals(testValue.positionFen, Evaluation.DRAW, evaluation);
			
			final int nonLosingMoveCount = finder.getNonLosingMoveCount();
			Assert.assertEquals(testValue.positionFen, 1, nonLosingMoveCount);
			
			final int nonLosingMovesEvaluation = finder.getLosingMovesEvaluation();
			Assert.assertEquals(testValue.positionFen, -Evaluation.getMateEvaluation(testValue.depth), nonLosingMovesEvaluation);
		}
	}
}
