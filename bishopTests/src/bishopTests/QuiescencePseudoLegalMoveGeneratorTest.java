package bishopTests;

import bishop.base.IMoveGenerator;
import bishop.base.QuiescencePseudoLegalMoveGenerator;

public class QuiescencePseudoLegalMoveGeneratorTest extends MoveGeneratorTestBase {
	
	protected TestValue[] getTestValues() {
		return new TestValue[] {
				new TestValue ("1q2k1r1/8/3QB3/8/5K2/1N6/3N4/2b4R w - - 0 1", new String[] {"h1c1", "b3c1", "d6b8", "e6g8"}),
				new TestValue ("5k2/7P/6p1/p1pppp2/PpP3P1/1P1P1P2/4P2p/5K2 w - - 0 1", new String[] {"c4d5", "g4f5", "h7h8q", "h7h8r", "h7h8b", "h7h8n"}),
				new TestValue ("8/4p2P/4k1p1/p1pp1p2/PpP3P1/1P1P1P2/4P2p/5KB1 b - - 0 1", new String[] {"d5c4", "f5g4","h2h1q", "h2h1r", "h2h1b", "h2h1n", "h2g1q", "h2g1r", "h2g1b", "h2g1n"}),
				new TestValue ("k7/8/8/3PpP2/8/8/8/K7 w - e6 0 1", new String[] {"d5e6", "f5e6"}),
				new TestValue ("k7/8/8/8/6pP/8/8/K7 b - h3 0 1", new String[] {"g4h3"}),
				new TestValue ("4k3/8/8/8/8/5b2/8/R3K2R w KQ - 0 1", new String[] {}),
				new TestValue ("4k3/8/8/8/2b5/n7/8/R3K2R w KQ - 0 1", new String[] {"a1a3"}),
				new TestValue ("rnbqkb1r/ppp2pPp/8/4p3/P6P/8/1p1PPP2/RNBQKBNR b KQkq - 0 1", new String[] {"b2a1q", "b2a1r", "b2a1n", "b2a1b", "b2c1q", "b2c1r", "b2c1n", "b2c1b", "f8g7", "d8h4", "d8d2"})
				
		};
	}
	
	protected IMoveGenerator getMoveGenerator() {
		return new QuiescencePseudoLegalMoveGenerator();
	}
}
