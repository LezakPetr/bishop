package bishopTests;

import bishop.base.IMoveGenerator;
import bishop.base.LegalMoveGenerator;


public class LegalMoveGeneratorTest extends MoveGeneratorTestBase {
	
	protected TestValue[] getTestValues() {
		return new TestValue[] {
				new TestValue ("1q2k1r1/8/3QB3/8/5K2/1N6/3N4/2b4R w - - 0 1", new String[] {"h1c1", "h1d1", "h1e1", "h1f1", "h1g1", "h1h2", "h1h3", "h1h4", "h1h5", "h1h6", "h1h7", "h1h8", "b3a1", "b3c1", "b3d4", "b3c5", "b3a5", "f4f3", "f4e3", "f4e4", "f4e5", "f4f5", "d6b8", "d6c7", "d6e5", "e6c4", "e6d5", "e6f7", "e6g8", "e6c8", "e6d7", "e6f5", "e6g4", "e6h3"}),
				new TestValue ("b3R1N1/4n3/4k3/n7/7r/1B6/5K2/3q4 b - - 0 1", new String[] {"d1b3", "d1d5", "h4c4", "a5b3", "a5c4", "e6d6", "e6d7", "e6e5", "e6f5", "a8d5"}),
				new TestValue ("5k2/7P/6p1/p1pppp2/PpP3P1/1P1P1P2/4P2p/5K2 w - - 0 1", new String[] {"c4d5", "d3d4", "e2e3", "e2e4", "f3f4", "g4g5", "g4f5", "h7h8q", "h7h8r", "h7h8b", "h7h8n", "f1e1", "f1f2", "f1g2"}),
				new TestValue ("8/4p2P/4k1p1/p1pp1p2/PpP3P1/1P1P1P2/4P2p/5KB1 b - - 0 1", new String[] {"d5d4", "d5c4", "f5f4", "f5g4", "g6g5", "h2h1q", "h2h1r", "h2h1b", "h2h1n", "h2g1q", "h2g1r", "h2g1b", "h2g1n", "e6d7", "e6d6", "e6e5", "e6f7", "e6f6"}),
				new TestValue ("k7/8/8/3PpP2/8/8/8/K7 w - e6 0 1", new String[] {"a1a2", "a1b2", "a1b1", "d5d6", "d5e6", "f5f6", "f5e6"}),
				new TestValue ("k7/8/8/8/6pP/8/8/K7 b - h3 0 1", new String[] {"a8a7", "a8b7", "a8b8", "g4g3", "g4h3"}),
				new TestValue ("4k3/8/8/8/8/5b2/8/R3K2R w KQ - 0 1", new String[] {"a1a2", "a1a3", "a1a4", "a1a5", "a1a6", "a1a7", "a1a8", "a1b1", "a1c1", "a1d1", "h1h2", "h1h3", "h1h4", "h1h5", "h1h6", "h1h7", "h1h8", "h1g1", "h1f1", "e1g1", "e1d2", "e1f2", "e1f1"}),
				new TestValue ("4k3/8/8/8/2b5/n7/8/R3K2R w KQ - 0 1", new String[] {"a1a2", "a1a3", "a1b1", "a1c1", "a1d1", "h1h2", "h1h3", "h1h4", "h1h5", "h1h6", "h1h7", "h1h8", "h1g1", "h1f1", "e1c1", "e1d1", "e1d2", "e1f2"}),
				new TestValue ("r3k2r/8/8/8/8/8/4Q3/4K3 b kq - 0 1", new String[] {"e8d8", "e8d7", "e8f8", "e8f7"}),
				new TestValue ("r3k2r/8/8/8/8/8/8/4K3 b - - 0 1", new String[] {"a8a7", "a8a6", "a8a5", "a8a4", "a8a3", "a8a2", "a8a1", "a8b8", "a8c8", "a8d8", "h8h7", "h8h6", "h8h5", "h8h4", "h8h3", "h8h2", "h8h1", "h8g8", "h8f8", "e8d8", "e8d7", "e8e7", "e8f8", "e8f7"})
		};
	}
	
	protected IMoveGenerator getMoveGenerator() {
		return new LegalMoveGenerator();
	}
}
