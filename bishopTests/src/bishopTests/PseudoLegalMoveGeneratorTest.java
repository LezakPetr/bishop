package bishopTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bishop.base.IMoveGenerator;
import bishop.base.PseudoLegalMoveGenerator;

public class PseudoLegalMoveGeneratorTest extends MoveGeneratorTestBase {
	
	public static class PositionWithMoves {
		private final String positionFen;
		private final boolean check;
		private final Set<String> legalMoves;
		private final Set<String> pseudoLegalMoves;
		private final Set<String> checkMoves;
		
		public PositionWithMoves (final String positionFen, final boolean check, final Set<String> legalMoves, final Set<String> pseudoLegalMoves, final Set<String> checkMoves) {
			this.positionFen = positionFen;
			this.check = check;
			this.legalMoves = Collections.unmodifiableSet(legalMoves);
			this.pseudoLegalMoves = Collections.unmodifiableSet(pseudoLegalMoves);
			this.checkMoves = Collections.unmodifiableSet(checkMoves);
		}
		
		public PositionWithMoves (final String positionFen, final boolean check, final String legalMoves, final String pseudoLegalMoves, final String checkMoves) {
			this (
				positionFen,
				check,
				TestValue.splitMoveList(legalMoves),
				TestValue.splitMoveList(pseudoLegalMoves),
				TestValue.splitMoveList(checkMoves)
			);
		}

		public boolean isCheck() {
			return check;
		}

		public Set<String> getLegalMoves() {
			return legalMoves;
		}

		public Set<String> getCheckMoves() {
			return checkMoves;
		}

		public Set<String> getPseudoLegalMoves() {
			return pseudoLegalMoves;
		}

		public String getPositionFen() {
			return positionFen;
		}
		
	}
	
	public static PositionWithMoves[] getPseudoLegalTestValues() {
		return new PositionWithMoves[] {
				new PositionWithMoves (
					"b3R1N1/4n3/4k3/n7/7r/1B6/5K2/3q4 b - - 0 1",
					true,	
					"d1b3, d1d5, h4c4, a5b3, a5c4, e6d6, e6d7, e6e5, e6f5, a8d5",
					"d1a1, d1b1, d1c1, d1e1, d1f1, d1g1, d1h1, d1d2, d1d3, d1d4, d1d5, d1d6, d1d7, d1d8, d1b3, d1c2, d1e2, d1f3, d1g4, d1h5, a5b3, a5c4, a5c6, a5b7, h4a4, h4b4, h4c4, h4d4, h4e4, h4f4, h4g4, h4h1, h4h2, h4h3, h4h5, h4h6, h4h7, h4h8, a8b7, a8c6, a8d5, a8e4, a8f3, a8g2, a8h1, e6d7, e6d6, e6d5, e6e5, e6f5, e6f6, e6f7, e7c8, e7c6, e7d5, e7f5, e7g6, e7g8",
					"d1c2, d1d2, d1e2, d1e1, d1f3, d1f1, d1g1, h4h2, h4f4"
				),
				new PositionWithMoves (
					"1q2k1r1/8/3QB3/8/5K2/1N6/3N4/2b4R w - - 0 1",
					false,
					"h1c1, h1d1, h1e1, h1f1, h1g1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, b3a1, b3c1, b3d4, b3c5, b3a5, f4f3, f4e3, f4e4, f4e5, f4f5, d6b8, d6c7, d6e5, e6c4, e6d5, e6f7, e6g8, e6c8, e6d7, e6f5, e6g4, e6h3",
					"h1c1, h1d1, h1e1, h1f1, h1g1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, d2b1, d2c4, d2e4, d2f3, d2f1, b3a1, b3c1, b3d4, b3c5, b3a5, f4f3, f4e3, f4e4, f4e5, f4f5, f4g5, f4g4, f4g3, d6b8, d6c7, d6e5, d6a6, d6b6, d6c6, d6d3, d6d4, d6d5, d6d7, d6d8, d6a3, d6b4, d6c5, d6e7, d6f8, e6c4, e6d5, e6f7, e6g8, e6c8, e6d7, e6f5, e6g4, e6h3",
					"d6b8, d6d8, d6f8, d6c6, d6d7, d6e7, e6d7, e6f7"
				),
				new PositionWithMoves (
					"5k2/7P/6p1/p1pppp2/PpP3P1/1P1P1P2/4P2p/5K2 w - - 0 1",
					false,
					"c4d5, d3d4, e2e3, e2e4, f3f4, g4g5, g4f5, h7h8q, h7h8r, h7h8b, h7h8n, f1e1, f1f2, f1g2",
					"c4d5, d3d4, e2e3, e2e4, f3f4, g4g5, g4f5, h7h8q, h7h8r, h7h8b, h7h8n, f1e1, f1f2, f1g2, f1g1",
					"h7h8q, h7h8r"
				),	
				new PositionWithMoves (
					"8/4p2P/4k1p1/p1pp1p2/PpP3P1/1P1P1P2/4P2p/5KB1 b - - 0 1",
					false,
					"d5d4, d5c4, f5f4, f5g4, g6g5, h2h1q, h2h1r, h2h1b, h2h1n, h2g1q, h2g1r, h2g1b, h2g1n, e6d7, e6d6, e6e5, e6f7, e6f6",
					"d5d4, d5c4, f5f4, f5g4, g6g5, h2h1q, h2h1r, h2h1b, h2h1n, h2g1q, h2g1r, h2g1b, h2g1n, e6d7, e6d6, e6e5, e6f7, e6f6",
					"h2g1q, h2g1r"
				),
				new PositionWithMoves (
					"k7/8/8/3PpP2/8/8/8/K7 w - e6 0 1",
					false,
					"a1a2, a1b2, a1b1, d5d6, d5e6, f5f6, f5e6",
					"a1a2, a1b2, a1b1, d5d6, d5e6, f5f6, f5e6",
					""
				),
				new PositionWithMoves (
					"k7/8/8/8/6pP/8/8/K7 b - h3 0 1",
					false,
					"a8a7, a8b7, a8b8, g4g3, g4h3",
					"a8a7, a8b7, a8b8, g4g3, g4h3",
					""
				),
				new PositionWithMoves (
					"4k3/8/8/8/8/5b2/8/R3K2R w KQ - 0 1",
					false,
					"a1a2, a1a3, a1a4, a1a5, a1a6, a1a7, a1a8, a1b1, a1c1, a1d1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, h1g1, h1f1, e1g1, e1d2, e1f2, e1f1",
					"a1a2, a1a3, a1a4, a1a5, a1a6, a1a7, a1a8, a1b1, a1c1, a1d1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, h1g1, h1f1, e1g1, e1d1, e1d2, e1e2, e1f2, e1f1",
					"a1a8, h1h8"
				),
				new PositionWithMoves (
					"4k3/8/8/8/2b5/n7/8/R3K2R w KQ - 0 1",
					false,
					"a1a2, a1a3, a1b1, a1c1, a1d1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, h1g1, h1f1, e1c1, e1d1, e1d2, e1f2",
					"a1a2, a1a3, a1b1, a1c1, a1d1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, h1g1, h1f1, e1c1, e1d1, e1d2, e1e2, e1f2, e1f1",
					"h1h8"
				),
				new PositionWithMoves (
					"r3k2r/8/8/8/8/8/4Q3/4K3 b kq - 0 1",
					true,
					"e8d8, e8d7, e8f8, e8f7",
					"a8a7, a8a6, a8a5, a8a4, a8a3, a8a2, a8a1, a8b8, a8c8, a8d8, e8d8, e8d7, e8e7, e8f8, e8f7, h8h7, h8h6, h8h5, h8h4, h8h3, h8h2, h8h1, h8g8, h8f8",
					"a8a1, h8h1"
				),
				new PositionWithMoves (
					"r3k2r/8/8/8/8/8/8/4K3 b - - 0 1",
					false,
					"a8a7, a8a6, a8a5, a8a4, a8a3, a8a2, a8a1, a8b8, a8c8, a8d8, h8h7, h8h6, h8h5, h8h4, h8h3, h8h2, h8h1, h8g8, h8f8, e8d8, e8d7, e8e7, e8f8, e8f7",
					"a8a7, a8a6, a8a5, a8a4, a8a3, a8a2, a8a1, a8b8, a8c8, a8d8, h8h7, h8h6, h8h5, h8h4, h8h3, h8h2, h8h1, h8g8, h8f8, e8d8, e8d7, e8e7, e8f8, e8f7",
					"a8a1, h8h1"
				),
				new PositionWithMoves(
					"4k3/8/8/8/8/8/1p4p1/R3K2R b KQ - 0 1",
					false,
					"e8d8, e8d7, e8e7, e8f7, e8f8, b2a1q, b2a1r, b2a1b, b2a1n, b2b1q, b2b1r, b2b1b, b2b1n, g2g1q, g2g1r, g2g1b, g2g1n, g2h1q, g2h1r, g2h1b, g2h1n",
					"e8d8, e8d7, e8e7, e8f7, e8f8, b2a1q, b2a1r, b2a1b, b2a1n, b2b1q, b2b1r, b2b1b, b2b1n, g2g1q, g2g1r, g2g1b, g2g1n, g2h1q, g2h1r, g2h1b, g2h1n",
					"b2a1q, b2a1r, b2b1q, b2b1r, g2g1q, g2g1r, g2h1q, g2h1r"
				),
				new PositionWithMoves(
					"6rk/6pp/7N/8/8/8/5PPP/6K1 w - - 0 1",
					false,
					"g1f1, g1h1, f2f3, f2f4, g2g3, g2g4, h2h3, h2h4, h6g4, h6f5, h6f7, h6g8",
					"g1f1, g1h1, f2f3, f2f4, g2g3, g2g4, h2h3, h2h4, h6g4, h6f5, h6f7, h6g8",
					"h6f7"
				),
				new PositionWithMoves(   // Generating checks
					"b7/1k6/8/8/8/6p1/4p3/7K b - - 0 1",
					false,
					"b7a7, b7a6, b7b6, b7c6, b7c7, b7c8, b7b8, g3g2, e2e1q, e2e1r, e2e1n, e2e1b",
					"b7a7, b7a6, b7b6, b7c6, b7c7, b7c8, b7b8, g3g2, e2e1q, e2e1r, e2e1n, e2e1b",
					"b7a7, b7a6, b7b6, b7c7, b7c8, b7b8, g3g2, e2e1q, e2e1r"
				),
				new PositionWithMoves(   // Generating checks by pawn promotion
					"5r2/4P2k/8/8/8/8/8/7K w - - 0 1",
					false,
					"h1g1, h1g2, h1h2, e7e8q, e7e8r, e7e8n, e7e8b, e7f8q, e7f8r, e7f8n, e7f8b",
					"h1g1, h1g2, h1h2, e7e8q, e7e8r, e7e8n, e7e8b, e7f8q, e7f8r, e7f8n, e7f8b",
					"e7f8n"
				),
				new PositionWithMoves(   // Generating checks by pawns
					"8/8/8/3k4/4p3/5P2/2P5/7K w - - 0 1",
					false,
					"h1g1, h1g2, h1h2, c2c3, c2c4, f3f4, f3e4",
					"h1g1, h1g2, h1h2, c2c3, c2c4, f3f4, f3e4",
					"c2c4, f3e4"
				),
				new PositionWithMoves(   // Reduction of moves in check
					"2R3k1/3q1ppp/B7/8/8/8/5PPP/6K1 b - - 0 1",
					true,
					"d7c8, d7d8, d7e8",
					"d7c8, d7d8, d7e8, d7a7, d7b7, d7c7, d7e7, d7a4, d7b5, d7c6, d7d1, d7d2, d7d3, d7d4, d7d5, d7d6, d7h3, d7g4, d7f5, d7e6, g8f8, g8h8, h7h5, h7h6, g7g5, g7g6, f7f5, f7f6",
					"d7d1"
				)
				
		};
	}
	
	@Override
	protected List<List<Object>> getParameterCombinations() {
		final List<List<Object>> result = new ArrayList<>();
		
		result.add(Arrays.asList(Boolean.FALSE, Boolean.FALSE));
		result.add(Arrays.asList(Boolean.FALSE, Boolean.TRUE));
		result.add(Arrays.asList(Boolean.TRUE, Boolean.FALSE));
		result.add(Arrays.asList(Boolean.TRUE, Boolean.TRUE));
		
		return result;
	}
	
	protected TestValue[] getTestValues() {
		final boolean reduceInCheck = (Boolean) parameters.get(0);
		final boolean generateOnlyCheck = (Boolean) parameters.get(1);
		
		final PositionWithMoves[] pseudoLegalTestValues = PseudoLegalMoveGeneratorTest.getPseudoLegalTestValues();
		final List<TestValue> result = new ArrayList<>();
		
		for (PositionWithMoves testCase: pseudoLegalTestValues) {
			if (!reduceInCheck || testCase.isCheck()) {
				final Set<String> allMoves = testCase.getPseudoLegalMoves();
				final Set<String> minMoves = new HashSet<>(generateOnlyCheck ? testCase.getCheckMoves() : allMoves);
				
				if (reduceInCheck)
					minMoves.retainAll(testCase.legalMoves);
				
				result.add(new TestValue(testCase.getPositionFen(), minMoves, allMoves));
			}
		}
		
		return result.toArray(new TestValue[result.size()]);
	}
	
	protected IMoveGenerator getMoveGenerator() {
		final PseudoLegalMoveGenerator generator = new PseudoLegalMoveGenerator();
		
		final boolean reduceInCheck = (Boolean) parameters.get(0);
		generator.setReduceMovesInCheck(reduceInCheck);
		
		final boolean generateOnlyCheck = (Boolean) parameters.get(1);
		generator.setGenerateOnlyChecks(generateOnlyCheck);
		
		return generator;
	}
	
}
