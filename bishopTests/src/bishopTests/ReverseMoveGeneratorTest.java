package bishopTests;

import bishop.base.File;
import bishop.base.IMoveGenerator;
import bishop.base.Move;
import bishop.base.ReverseMoveGenerator;


public class ReverseMoveGeneratorTest extends MoveGeneratorTestBase {
		
	protected TestValue[] getTestValues() {
		return new TestValue[] {
			new TestValue ("1q2k1r1/8/3QB3/8/5K2/1N6/3N4/2b4R w - - 0 1", new String[] {"a8b8 -", "c8b8 -", "d8b8 -", "b7b8 -", "b6b8 -", "b5b8 -", "b4b8 -", "a7b8 -", "c7b8 -", "d8e8 -", "d7e8 -", "e7e8 -", "f7e8 -", "f8e8 -", "f8g8 -", "h8g8 -", "g7g8 -", "g6g8 -", "g5g8 -", "g4g8 -", "g3g8 -", "g2g8 -", "g1g8 -", "a3c1 -", "b2c1 -"}),
			new TestValue ("2n5/4P1k1/8/8/1P3P2/2PK1B2/8/7N b - - 0 1", new String[] {"b2b4 -", "b3b4 -", "c2c3 -", "e6e7 -", "c4d3 -", "c2d3 -", "d2d3 -", "e2d3 -", "e3d3 -", "e4d3 -", "d4d3 -", "f2h1 -", "g3h1 -", "d1f3 -", "e2f3 -", "g2f3 -", "h5f3 -", "g4f3 -", "a8f3 -", "b7f3 -", "c6f3 -", "d5f3 -", "e4f3 -"}),
			new TestValue ("8/5k2/8/6Q1/1pP5/8/5K2/8 b - c3 0 1", new String[] {"c2c4 -"}),
			new TestValue ("n6k/8/8/8/1PpP1P2/8/6K1/8 w - - 0 1", new String[] {"b6a8 -", "b6a8 b", "b6a8 d", "c7a8 -", "c7a8 b", "c7a8 d", "h7h8 -", "h7h8 b", "h7h8 d", "g7h8 -", "g7h8 b", "g7h8 d", "g8h8 -", "g8h8 b", "g8h8 d", "c5c4 -"}),
			new TestValue ("7k/8/8/Pp3P2/8/8/8/7K b - - 0 1", new String[] {"h2h1 -", "h2h1 b", "g2h1 -", "g2h1 b", "g1h1 -", "g1h1 b", "g2h1 -", "g2h1 b", "f4f5 -", "f4f5 b", "a4a5 -"})
		};
	}
	
	protected IMoveGenerator getMoveGenerator() {
		return new ReverseMoveGenerator();
	}
	
	@Override
	protected String moveToString(final Move move) {
		final int epFile = move.getPreviousEpFile();
		final char epFileChar = (epFile == File.NONE) ? '-' : File.toChar(epFile);
		
		return move.toString() + " " + epFileChar;
	}

}
