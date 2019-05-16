package bishopTests;

import bishop.base.BitBoard;
import bishop.base.Square;
import bishop.tables.ProlongTable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProlongTableTest {

	@Test
	public void tableCellTest() {
		class TestValue {
			public final int beginSquare;
			public final int targetSquare;
			public final long expectedMask;

			public TestValue (final int beginSquare, final int targetSquare, final String expectedSquares) {
				this.beginSquare = beginSquare;
				this.targetSquare = targetSquare;
				this.expectedMask = BitBoard.fromString (expectedSquares);
			}
		}

		final TestValue[] testValueArray = {
				new TestValue(Square.A1, Square.H8, ""),
				new TestValue(Square.B3, Square.B6, "b7, b8"),
				new TestValue(Square.H2, Square.C2, "b2, a2"),
				new TestValue(Square.A8, Square.H1, ""),
				new TestValue(Square.E4, Square.E5, "e6, e7, e8"),
				new TestValue(Square.E4, Square.F6, ""),
				new TestValue(Square.E4, Square.E4, "")
		};

		for (TestValue testValue: testValueArray) {
			final long expectedMask = testValue.expectedMask;
			final long returnedMask = ProlongTable.getItem (testValue.beginSquare, testValue.targetSquare);

			assertEquals (expectedMask, returnedMask);
		}

	}

}
