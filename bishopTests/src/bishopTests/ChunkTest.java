package bishopTests;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.Square;
import bishop.tablebase.Chunk;
import bishop.tablebase.CombinationDefinition;
import bishop.tablebase.SquareCombination;
import bishop.tablebase.SquareCombinationKey;

public class ChunkTest {

	private static class TestCase {
		public final Chunk chunk;
		public final SquareCombinationKey[] combinations;
		public final long[] fixedPawnSquares;
		
		public TestCase(final Chunk chunk, final SquareCombinationKey[] combinations, final long[] fixedPawnSquares) {
			this.chunk = chunk;
			this.combinations = combinations;
			this.fixedPawnSquares = fixedPawnSquares;
		}
	}
	
	private static final int VERSION = 2;
	
	private static CombinationDefinition ONE_WHITE_QUEEN = new CombinationDefinition(Piece.withColorAndType(Color.WHITE, PieceType.QUEEN), 1);
	private static CombinationDefinition ONE_WHITE_PAWN = new CombinationDefinition(Piece.withColorAndType(Color.WHITE, PieceType.PAWN), 1);
	private static CombinationDefinition TWO_WHITE_PAWNS = new CombinationDefinition(Piece.withColorAndType(Color.WHITE, PieceType.PAWN), 2);
	private static CombinationDefinition THREE_WHITE_PAWNS = new CombinationDefinition(Piece.withColorAndType(Color.WHITE, PieceType.PAWN), 3);
	private static CombinationDefinition TWO_BLACK_ROOKS = new CombinationDefinition(Piece.withColorAndType(Color.BLACK, PieceType.ROOK), 2);
	private static CombinationDefinition ONE_BLACK_KNIGHT = new CombinationDefinition(Piece.withColorAndType(Color.BLACK, PieceType.KNIGHT), 1);
	private static CombinationDefinition ONE_BLACK_BISHOP = new CombinationDefinition(Piece.withColorAndType(Color.BLACK, PieceType.BISHOP), 1);
	private static CombinationDefinition ONE_BLACK_PAWN = new CombinationDefinition(Piece.withColorAndType(Color.BLACK, PieceType.PAWN), 1);
	private static CombinationDefinition TWO_BLACK_PAWNS = new CombinationDefinition(Piece.withColorAndType(Color.BLACK, PieceType.PAWN), 2);
	
	private static final TestCase[] TEST_CASES = {
		// No EP
		new TestCase(
			new Chunk (
				VERSION,
				Color.WHITE,
				Square.B2, Square.E4,
				File.NONE, false,
				new CombinationDefinition[] {
					ONE_WHITE_QUEEN,
					THREE_WHITE_PAWNS,
					TWO_BLACK_ROOKS
				},
				0
			),
			new SquareCombinationKey[] {
				new SquareCombinationKey(ONE_WHITE_QUEEN, ~BitBoard.fromString("b2, d3, d4, d5, e3, e4, e5, f3, f4, f5")),
				new SquareCombinationKey(THREE_WHITE_PAWNS, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("b2, e4, d3, f3")),
				new SquareCombinationKey(TWO_BLACK_ROOKS, ~BitBoard.fromString("b2, e4"))
			},
			new long[] {
				BitBoard.EMPTY,
				BitBoard.EMPTY
			}
		),
		new TestCase(
			new Chunk (
				VERSION,
				Color.BLACK,
				Square.C3, Square.G1,
				File.NONE, false,
				new CombinationDefinition[] {
					ONE_WHITE_QUEEN,
					ONE_WHITE_PAWN,
					TWO_BLACK_ROOKS,
					ONE_BLACK_KNIGHT,
					ONE_BLACK_BISHOP,
					ONE_BLACK_PAWN
				},
				0
			),
			new SquareCombinationKey[] {
				new SquareCombinationKey(ONE_WHITE_QUEEN, ~BitBoard.fromString("c3, g1")),
				new SquareCombinationKey(ONE_WHITE_PAWN, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("c3, g1")),
				new SquareCombinationKey(TWO_BLACK_ROOKS, ~BitBoard.fromString("c2, c3, c4, b3, d3, g1")),
				new SquareCombinationKey(ONE_BLACK_KNIGHT, ~BitBoard.fromString("c3, g1, a2, b1, d1, e2, e4, d5, b5, a4")),
				new SquareCombinationKey(ONE_BLACK_BISHOP, ~BitBoard.fromString("c3, g1, b2, d2, d4, b4")),
				new SquareCombinationKey(ONE_BLACK_PAWN, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("c3, g1, b4, d4"))
			},
			new long[] {
				BitBoard.EMPTY,
				BitBoard.EMPTY
			}
		),
		// EP
		new TestCase(
			new Chunk (
				VERSION,
				Color.BLACK,
				Square.E6, Square.E8,
				File.FD, true,
				new CombinationDefinition[] {
					ONE_WHITE_QUEEN,
					THREE_WHITE_PAWNS,
					TWO_BLACK_ROOKS,
					TWO_BLACK_PAWNS
				},
				0
			),
			new SquareCombinationKey[] {
				new SquareCombinationKey(ONE_WHITE_QUEEN, ~BitBoard.fromString("c4, d2, d3, d4, e6, e8")),
				new SquareCombinationKey(TWO_WHITE_PAWNS, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("c4, d2, d3, d4, e6, e8")),
				new SquareCombinationKey(TWO_BLACK_ROOKS, ~BitBoard.fromString("c4, d2, d3, d4, e6, e8, e5, e7, d6, f6")),
				new SquareCombinationKey(ONE_BLACK_PAWN, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("c4, d2, d3, d4, e6, e8, d7, f7"))
			},
			new long[] {
				BitBoard.fromString("d4"),
				BitBoard.fromString("c4")
			}
		),
		new TestCase(
			new Chunk (
				VERSION,
				Color.BLACK,
				Square.E6, Square.E8,
				File.FD, false,
				new CombinationDefinition[] {
					ONE_WHITE_QUEEN,
					THREE_WHITE_PAWNS,
					TWO_BLACK_ROOKS,
					TWO_BLACK_PAWNS
				},
				0
			),
			new SquareCombinationKey[] {
				new SquareCombinationKey(ONE_WHITE_QUEEN, ~BitBoard.fromString("e4, d2, d3, d4, e6, e8")),
				new SquareCombinationKey(TWO_WHITE_PAWNS, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("e4, d2, d3, d4, e6, e8")),
				new SquareCombinationKey(TWO_BLACK_ROOKS, ~BitBoard.fromString("e4, d2, d3, d4, e6, e8, e5, e7, d6, f6")),
				new SquareCombinationKey(ONE_BLACK_PAWN, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("c4, e4, d2, d3, d4, e6, e8, d7, f7"))
			},
			new long[] {
				BitBoard.fromString("d4"),
				BitBoard.fromString("e4")
			}
		),
		new TestCase(
			new Chunk (
				VERSION,
				Color.WHITE,
				Square.H2, Square.A5,
				File.FG, true,
				new CombinationDefinition[] {
					THREE_WHITE_PAWNS,
					ONE_BLACK_BISHOP,
					TWO_BLACK_PAWNS
				},
				0
			),
			new SquareCombinationKey[] {
				new SquareCombinationKey(TWO_WHITE_PAWNS, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("a5, h2, f5, g5, g6, g7, b4")),
				new SquareCombinationKey(ONE_BLACK_BISHOP, ~BitBoard.fromString("a5, h2, f5, g5, g6, g7")),
				new SquareCombinationKey(ONE_BLACK_PAWN, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("a5, h2, f5, g5, g6, g7"))
			},
			new long[] {
				BitBoard.fromString("f5"),
				BitBoard.fromString("g5")
			}
		),
		new TestCase(
			new Chunk (
				VERSION,
				Color.WHITE,
				Square.H2, Square.A5,
				File.FG, false,
				new CombinationDefinition[] {
					THREE_WHITE_PAWNS,
					ONE_BLACK_BISHOP,
					TWO_BLACK_PAWNS
				},
				0
			),
			new SquareCombinationKey[] {
				new SquareCombinationKey(TWO_WHITE_PAWNS, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("a5, h2, f5, g5, g6, g7, h5, b4")),
				new SquareCombinationKey(ONE_BLACK_BISHOP, ~BitBoard.fromString("a5, h2, h5, g5, g6, g7")),
				new SquareCombinationKey(ONE_BLACK_PAWN, ~BoardConstants.RANK_18_MASK & ~BitBoard.fromString("a5, h2, h5, g5, g6, g7"))
			},
			new long[] {
				BitBoard.fromString("h5"),
				BitBoard.fromString("g5")
			}
		)		
	};
	
	@Test
	public void testGeneratedCombinations() {
		for (TestCase testCase: TEST_CASES) {
			final Chunk chunk = testCase.chunk;
			
			for (int color = Color.FIRST; color < Color.LAST; color++)
				Assert.assertEquals(testCase.fixedPawnSquares[color], chunk.getFixedPawnMask(color));
			
			if (chunk.getSquareCombinationCount() == testCase.combinations.length) {
				for (int i = 0; i < testCase.combinations.length; i++) {
					final SquareCombinationKey expectedKey = testCase.combinations[i];
					final SquareCombination combination = chunk.getSquareCombinationAt(i);
					
					Assert.assertEquals(expectedKey, combination.getKey());
				}
			}
			else
				Assert.fail("Different combination count");
		}
	}
}
