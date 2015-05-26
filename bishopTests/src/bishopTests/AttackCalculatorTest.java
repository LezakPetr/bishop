package bishopTests;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.BitBoard;
import bishop.base.Color;
import bishop.base.Fen;
import bishop.base.Position;
import bishop.engine.AttackCalculator;
import bishop.engine.AttackEvaluationTable;

public class AttackCalculatorTest {
	@Test
	public void mobilityTest() throws IOException {
		class TestValue {
			public final String positionFen;
			public final long[] expectedAttackedSquareBoards;
			
			public TestValue (final String positionFen, final String whiteSquares, final String blackSquares) {
				this.positionFen = positionFen;
				
				this.expectedAttackedSquareBoards = new long[Color.LAST];
				this.expectedAttackedSquareBoards[Color.WHITE] = BitBoard.fromString(whiteSquares);
				this.expectedAttackedSquareBoards[Color.BLACK] = BitBoard.fromString(blackSquares);
			}
		}
		
		TestValue[] testValueArray = {
			new TestValue(
				"r3r3/ppp2ppk/3p1q1p/b3pNn1/4P3/P1PPB2P/1P3PPK/R1Q2R2 w - - 0 1",
				"a1, b1, c1, d1, e1, f1, g1, h1, a2, b2, c2, d2, f2, g2, a3, c3, e3, f3, g3, h3, b4, c4, d4, e4, f4, g4, h4, c5, d5, f5, g5, b6, d6, h6, a7, e7, g7",
				"c3, f3, h3, b4, d4, e4, f4, c5, e5, f5, g5, a6, b6, c6, d6, e6, f6, g6, h6, a7, c7, e7, f7, g7, h7, a8, b8, c8, d8, e8, f8, g8, h8"
			),
			new TestValue(
				"4rrk1/6b1/4p1q1/4p3/4n1pp/2P1Q2P/PP3PPK/2BRR2N b - - 0 1",
				"c1, d1, e1, f1, g1, h1, b2, d2, e2, f2, g2, a3, b3, c3, d3, e3, f3, g3, h3, b4, d4, e4, f4, g4, c5, d5, g5, b6, d6, h6, a7, d7, d8",
				"d2, f2, c3, f3, g3, h3, d4, e4, f4, g4, c5, d5, e5, f5, g5, h5, d6, e6, f6, h6, e7, f7, g7, h7, a8, b8, c8, d8, e8, f8, g8, h8"
			),
			new TestValue(
				"8/3P4/8/8/8/8/5p2/k6K w - - 0 1",
				"g1, g2, h2, c8, e8",
				"b1, e1, g1, a2, b2"
			),
			new TestValue(
				"8/8/8/4K2r/8/2b5/1k6/4q3 w - - 0 1",
				"d4, e4, f4, d5, f5, d6, e6, f6",
				"a1, b1, c1, d1, e1, f1, g1, h1, a2, b2, c2, d2, e2, f2, h2, a3, b3, c3, e3, g3, h3, b4, d4, e4, h4, a5, b5, c5, d5, e5, f5, g5, e6, f6, h6, e7, g7, h7, e8, h8"
			),
			new TestValue(
				"7b/8/5k2/8/3K4/8/8/B7 w - - 0 1",
				"b2, c3, d3, e3, c4, d4, e4, c5, d5, e5",
				"e5, f5, g5, e6, f6, g6, e7, f7, g7"
			)
		};
		
		final Fen fen = new Fen();
		final AttackCalculator calculator = new AttackCalculator();
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			
			final Position position = fen.getPosition();
			calculator.calculate(position, AttackEvaluationTable.ZERO_TABLE);
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final long attackedSquares = calculator.getDirectlyAttackedSquares(color);
				TestUtils.assertBitBoardsEqual (testValue.expectedAttackedSquareBoards[color], attackedSquares);
			}
		}
	}
	
	@Test
	public void canBeMateTest() throws IOException {
		class TestValue {
			public final String positionFen;
			public final boolean canBeMate;
			
			public TestValue (final String positionFen, final boolean canBeMate) {
				this.positionFen = positionFen;
				this.canBeMate = canBeMate;
			}
		}
		
		TestValue[] testValueArray = {
			new TestValue("3N1K2/5q2/8/8/2b5/8/8/7k w - - 0 1", true),
			new TestValue("3N1K2/5q2/8/8/8/8/8/7k w - - 0 1", false),
			new TestValue("2R3k1/5p1p/6p1/8/4r3/2B5/8/7K b - - 0 1", true),
			new TestValue("2R3k1/5p1p/6p1/4r3/8/2B5/8/7K b - - 0 1", false),
			new TestValue("2R3k1/5p1N/6p1/8/4r3/2B5/8/7K b - - 0 1", false),
			new TestValue("2R3k1/5ppp/8/8/8/8/8/7K b - - 0 1", true)
		};
		
		final Fen fen = new Fen();
		final AttackCalculator calculator = new AttackCalculator();
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			
			final Position position = fen.getPosition();
			calculator.calculate(position, AttackEvaluationTable.ZERO_TABLE);
			
			Assert.assertEquals(testValue.canBeMate, calculator.getCanBeMate());
		}
	}
	

}
