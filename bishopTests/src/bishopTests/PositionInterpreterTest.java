package bishopTests;

import org.junit.Assert;
import org.junit.Test;
import utils.IoUtils;
import bishop.base.BitBoard;
import bishop.base.Fen;
import bishop.base.Square;
import bishop.interpreter.Bytecode;
import bishop.interpreter.Context;
import bishop.interpreter.ExpressionCreator;
import bishop.interpreter.IExpression;

public class PositionInterpreterTest {
	
	private static class TestValue {
		public String expression;
		public String positionFen;
		public long result;
		
		public TestValue (final String expression, final String positionFen, final long result) {
			this.positionFen = positionFen;
			this.expression = expression;
			this.result = result;
		}
	}
	
	@Test
	public void positionInterpreterTest() throws Exception {
		final TestValue[] testValueArray = {
			new TestValue("5 10 +", null, 15),
			new TestValue("-5 0xF -", null, -20),
			new TestValue("8 4 *", null, 32),
			new TestValue("8 4 /", null, 2),
			new TestValue("987 neg", null, -987),
			new TestValue("0xF0 0x0F |", null, 0xFF),
			new TestValue("0xF0 0x3F &", null, 0x30),
			new TestValue("0xF0 0x3F ^", null, 0xCF),
			new TestValue("5 ~", null, ~5L),
			new TestValue("5 neg", null, -5),
			new TestValue("2 3 * 8 2 / +", null, 2*3 + 8/2),
			new TestValue("onTurn", "8/4k3/8/8/8/8/4K3/8 w - - 0 1", 0),
			new TestValue("onTurn", "8/4k3/8/8/8/8/4K3/8 b - - 0 1", 1),
			new TestValue("1 0 piecesMask", "8/4k3/8/8/8/8/4K3/8 b - - 0 1", BitBoard.getSquareMask(Square.E7)),
			new TestValue("0 5 piecesMask 1 5 piecesMask minDist", "8/1K6/1p6/7p/8/6P1/2P5/7k w - - 0 1", 2),
			new TestValue("0 0 piecesMask minEdgeDist", "8/1K6/1p6/7p/8/6P1/2P5/7k w - - 0 1", 1),
			new TestValue("1 0 piecesMask minEdgeDist", "8/1K6/1p6/7p/8/6P1/2P5/7k w - - 0 1", 0),
			new TestValue("0 5 piecesMask minRank", "8/1K6/1p6/7p/8/6P1/2P5/7k w - - 0 1", 1),
			new TestValue("0 5 piecesMask maxRank", "8/1K6/1p6/7p/8/6P1/2P5/7k w - - 0 1", 2),
			new TestValue("3 1 10 2 20 3 15 2 * 4 40 switch4", null, 30),
		};
		
		final Fen fen = new Fen();
		
		for (TestValue testValue: testValueArray) {
			final Bytecode bytecode = Bytecode.parse(IoUtils.getPushbackReader (testValue.expression));
			final ExpressionCreator creator = new ExpressionCreator();
			final IExpression expression = creator.createExpression(bytecode);
			final Context context = new Context();
			
			if (testValue.positionFen != null) {
				fen.readFenFromString(testValue.positionFen);
				context.setPosition(fen.getPosition());
			}
			else {
				context.setPosition(null);
			}
			
			final long expectedResult = testValue.result;
			final long result = expression.evaluate(context);
			
			Assert.assertEquals(testValue.expression, expectedResult, result);
		}
	}

	@Test
	public void speedTest() throws Exception {
		final String expressionStr = "0 0 piecesMask minRank 1 0 piecesMask minRank 10 * + 0 5 piecesMask maxRank 100 * + 1 5 piecesMask minRank 1000 * +";
		final String positionFen = "8/1K6/1p6/7p/8/6P1/2P5/7k w - - 0 1";
		final int count = 1000000;

		final ExpressionCreator creator = new ExpressionCreator();
		final Fen fen = new Fen();
		final Bytecode bytecode = Bytecode.parse(IoUtils.getPushbackReader (expressionStr));
		final IExpression expression = creator.createExpression(bytecode);
		
		final Context context = new Context();
		fen.readFenFromString(positionFen);
		context.setPosition(fen.getPosition());
		
		// Prefetch
		for (int i = 0; i < count; i++) {
			expression.evaluate(context);
		}

		// Test
		final long t0 = System.currentTimeMillis();
		
		for (int i = 0; i < count; i++) {
			expression.evaluate(context);
		}
		
		final long t1 = System.currentTimeMillis();
		System.out.println ((t1 - t0) + "ms");
	}

}
