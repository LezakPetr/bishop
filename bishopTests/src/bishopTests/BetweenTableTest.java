package bishopTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import bishop.base.BitBoard;
import bishop.base.Square;
import bishop.tables.BetweenTable;


public class BetweenTableTest {
	
	@Test
	public void tableCellTest() {
		class TestValue {
			public final int square1;
			public final int square2;
			public final long expectedMask;
			
			public TestValue (final int square1, final int square2, final int[] expectedSquares) {
				this.square1 = square1;
				this.square2 = square2;
				this.expectedMask = BitBoard.fromSquareArray (expectedSquares);
			}
		}
		
		final TestValue[] testValueArray = {
			new TestValue(Square.A1, Square.H8, new int[] {Square.B2, Square.C3, Square.D4, Square.E5, Square.F6, Square.G7}),
			new TestValue(Square.B3, Square.B6, new int[] {Square.B4, Square.B5}),
			new TestValue(Square.C2, Square.H2, new int[] {Square.D2, Square.E2, Square.F2, Square.G2}),
			new TestValue(Square.A8, Square.H1, new int[] {Square.B7, Square.C6, Square.D5, Square.E4, Square.F3, Square.G2}),
			new TestValue(Square.E4, Square.E5, new int[] {}),
			new TestValue(Square.E4, Square.F6, new int[] {})
		};
		
		for (TestValue testValue: testValueArray) {
			final long expectedMask = testValue.expectedMask;
			final long returnedMask = BetweenTable.getItem (testValue.square1, testValue.square2);
			
			assertEquals (expectedMask, returnedMask);
		}
		
	}

}

