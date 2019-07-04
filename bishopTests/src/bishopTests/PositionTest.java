package bishopTests;


import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import bishop.base.*;
import bishop.engine.Evaluation;
import org.junit.Assert;
import org.junit.Test;


public class PositionTest {

	private static final PieceTypeEvaluations pte = PieceTypeEvaluations.DEFAULT;
	
	@Test
	public void squareAttackTest() throws IOException {
		class TestValue {
			public final String positionFen;
			public final long expectedAttackedSquareBoard;
			
			public TestValue (final String positionFen, final int[] expectedAttackedSquares) {
				this.positionFen = positionFen;
				this.expectedAttackedSquareBoard = BitBoard.fromSquareArray(expectedAttackedSquares);
			}
		}
		
		TestValue[] testValueArray = {
			new TestValue("r3r3/ppp2ppk/3p1q1p/b3pNn1/4P3/P1PPB2P/1P3PPK/R1Q2R2 w - - 0 1", new int[] {Square.A1, Square.B1, Square.C1, Square.D1, Square.E1, Square.F1, Square.G1, Square.H1, Square.A2, Square.B2, Square.C2, Square.D2, Square.F2, Square.G2, Square.A3, Square.C3, Square.E3, Square.F3, Square.G3, Square.H3, Square.B4, Square.C4, Square.D4, Square.E4, Square.F4, Square.G4, Square.H4, Square.C5, Square.D5, Square.F5, Square.G5, Square.B6, Square.D6, Square.H6, Square.A7, Square.E7, Square.G7}),
			new TestValue("4rrk1/6b1/4p1q1/4p3/4n1pp/2P1Q2P/PP3PPK/2BRR2N b - - 0 1", new int[] {Square.D2, Square.F2, Square.C3, Square.F3, Square.G3, Square.H3, Square.D4, Square.E4, Square.F4, Square.G4, Square.C5, Square.D5, Square.E5, Square.F5, Square.G5, Square.H5, Square.D6, Square.E6, Square.F6, Square.H6, Square.E7, Square.F7, Square.G7, Square.H7, Square.A8, Square.B8, Square.C8, Square.D8, Square.E8, Square.F8, Square.G8, Square.H8}),
			new TestValue("8/3P4/8/8/8/8/5p2/k6K w - - 0 1", new int[] {Square.G1, Square.G2, Square.H2, Square.C8, Square.E8}),
			new TestValue("8/3P4/8/8/8/8/5p2/k6K b - - 0 1", new int[] {Square.A2, Square.B2, Square.B1, Square.E1, Square.G1})
		};
		
		final Fen fen = new Fen();
		
		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			
			final Position position = fen.getPosition();
			final int onTurn = position.getOnTurn();
			
			long attackedSquares = 0;
			
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				if (position.isSquareAttacked (onTurn, square))
					attackedSquares |= BitBoard.getSquareMask(square);
			}
			
			TestUtils.assertBitBoardsEqual (testValue.expectedAttackedSquareBoard, attackedSquares);
		}
	}
	
	@Test
	public void integrityTest() throws IOException {
		final String[] testCaseArray = {
			"1q2k1r1/8/3QB3/8/5K2/1N6/3N4/2b4R w - - 0 1",
			"b3R1N1/4n3/4k3/n7/7r/1B6/5K2/3q4 b - - 0 1",
			"5k2/7P/6p1/p1pppp2/PpP3P1/1P1P1P2/4P2p/5K2 w - - 0 1",
			"8/4p2P/4k1p1/p1pp1p2/PpP3P1/1P1P1P2/4P2p/5KB1 b - - 0 1",
			"k7/8/8/3PpP2/8/8/8/K7 w - e6 0 1",
			"k7/8/8/8/6pP/8/8/K7 b - h3 0 1",
			"4k3/8/8/8/8/5b2/8/R3K2R w KQ - 0 1",
			"4k3/8/8/8/2b5/n7/8/R3K2R w KQ - 0 1",
			"r3k2r/8/8/8/8/8/4Q3/4K3 b kq - 0 1",
			"r3k2r/8/8/8/8/8/8/4K3 b - - 0 1"
		};
		
		final MoveList moveList = new MoveList();
		
		final IMoveWalker walker = new IMoveWalker() {
			public boolean processMove(final Move move) { 
				moveList.add(move);
				return true;
			}
		};

		final IMoveGenerator generator = new LegalMoveGenerator();
		generator.setWalker(walker);

		Fen fen = new Fen();

		for (String testCase: testCaseArray) {
			moveList.clear();

			fen.readFenFromString(testCase);
			
			final Position beginPosition = fen.getPosition();
			beginPosition.checkIntegrity();

			generator.setPosition(beginPosition);
			generator.generateMoves();
			
			// Check integrity of target positions
			final Position targetPosition = new Position();
			
			for (Move move: moveList) {
				targetPosition.assign(beginPosition);
				targetPosition.makeMove(move);
				
				targetPosition.checkIntegrity();
			}
		}
	}
	
	private static  class MakeMoveTestCase {
		public final String previousPosition;
		public final String move;
		public final String nextPosition;
		
		public MakeMoveTestCase(final String previousPosition, final String move, final String nextPosition) {
			this.previousPosition = previousPosition;
			this.move = move;
			this.nextPosition = nextPosition;
		}
	}
	
	@Test
	public void makeUndoMoveTest() throws IOException {
		final MakeMoveTestCase[] testCaseArray = {
			new MakeMoveTestCase("5k2/8/8/8/8/1B6/8/6K1 w - - 0 1", "Bd5", "5k2/8/8/3B4/8/8/8/6K1 b - - 0 1"),   // Figure move
			new MakeMoveTestCase("5k2/8/8/8/3r4/8/2N5/6K1 w - - 0 1", "Nd4", "5k2/8/8/8/3N4/8/8/6K1 b - - 0 1"),   // Figure capture
			new MakeMoveTestCase("5k2/8/8/8/3p4/2P5/8/6K1 w - - 0 1", "c3c4", "5k2/8/8/8/2Pp4/8/8/6K1 b - - 0 1"),   // White pawn move
			new MakeMoveTestCase("5k2/8/8/8/3p4/8/2P5/6K1 w - - 0 1", "c2c4", "5k2/8/8/8/2Pp4/8/8/6K1 b - c3 0 1"),   // Double white pawn move with EP
			new MakeMoveTestCase("8/8/7k/8/5p2/8/3BP3/6K1 w - - 0 1", "e2e4", "8/8/7k/8/4Pp2/8/3B4/6K1 b - e3 0 1"),   // Double white pawn move with EP
			new MakeMoveTestCase("8/8/8/5k2/5p2/8/4P3/6K1 w - - 0 1", "e2e4", "8/8/8/5k2/4Pp2/8/8/6K1 b - e3 0 1"),   // Double white pawn move with EP
			new MakeMoveTestCase("8/k7/8/8/3p4/8/2P2B2/6K1 w - - 0 1", "c2c4", "8/k7/8/8/2Pp4/8/5B2/6K1 b - - 0 1"),   // Double white pawn move with impossible EP
			new MakeMoveTestCase("8/8/8/8/3p4/8/k1P2R2/6K1 w - - 0 1", "c2c4", "8/8/8/8/2Pp4/8/k4R2/6K1 b - - 0 1"),   // Double white pawn move with impossible EP
			new MakeMoveTestCase("5k2/8/8/8/8/8/2P5/6K1 w - - 0 1", "c2c4", "5k2/8/8/8/2P5/8/8/6K1 b - - 0 1"),   // Double white pawn move without EP
			new MakeMoveTestCase("5k2/8/4p3/5P2/8/8/8/6K1 b - - 0 1", "e6f5", "5k2/8/8/5p2/8/8/8/6K1 w - - 0 1"),   // Black pawn move
			new MakeMoveTestCase("5k2/4p3/8/5P2/8/8/8/6K1 b - - 0 1", "e7e5", "5k2/8/8/4pP2/8/8/8/6K1 w - e6 0 1"),   // Double black pawn move with EP
			new MakeMoveTestCase("5k2/4p3/8/8/8/8/8/6K1 b - - 0 1", "e7e5", "5k2/8/8/4p3/8/8/8/6K1 w - - 0 1"),   // Double black pawn move without EP
			new MakeMoveTestCase("5k2/8/8/8/2p5/1P6/8/6K1 w - - 0 1", "b3c4", "5k2/8/8/8/2P5/8/8/6K1 b - - 0 1"),   // White pawn capture
			new MakeMoveTestCase("5k2/8/8/3p4/2P5/8/8/6K1 b - - 0 1", "d5c4", "5k2/8/8/8/2p5/8/8/6K1 w - - 0 1"),   // Black pawn capture
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "Ke1e2", "r3k2r/8/8/8/8/8/4K3/R6R b kq - 0 1"),   // White king move
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "Ra1a2", "r3k2r/8/8/8/8/8/R7/4K2R b Kkq - 0 1"),   // White rook move
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "Rh1h2", "r3k2r/8/8/8/8/8/7R/R3K3 b Qkq - 0 1"),   // White rook move
			new MakeMoveTestCase("r3k2r/8/8/8/3b4/8/8/R3K2R b KQkq - 0 1", "Bd4a1", "r3k2r/8/8/8/8/8/8/b3K2R w Kkq - 0 1"),   // White rook capture
			new MakeMoveTestCase("r3k2r/8/8/8/4b3/8/8/R3K2R b KQkq - 0 1", "Be4h1", "r3k2r/8/8/8/8/8/8/R3K2b w Qkq - 0 1"),   // White rook capture
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "Ke8e7", "r6r/4k3/8/8/8/8/8/R3K2R w KQ - 0 1"),   // Black king move
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "Ra8a7", "4k2r/r7/8/8/8/8/8/R3K2R w KQk - 0 1"),   // Black rook move
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "Rh8h7", "r3k3/7r/8/8/8/8/8/R3K2R w KQq - 0 1"),   // Black rook move
			new MakeMoveTestCase("r3k2r/8/8/3B4/8/8/8/R3K2R w KQkq - 0 1", "Bd5a8", "B3k2r/8/8/8/8/8/8/R3K2R b KQk - 0 1"),   // Black rook capture
			new MakeMoveTestCase("r3k2r/8/8/4B3/8/8/8/R3K2R w KQkq - 0 1", "Be5h8", "r3k2B/8/8/8/8/8/8/R3K2R b KQq - 0 1"),   // Black rook capture
			new MakeMoveTestCase("3q3k/4P3/8/8/8/8/8/7K w - - 0 1", "e7e8=Q", "3qQ2k/8/8/8/8/8/8/7K b - - 0 1"),   // White pawn promotion
			new MakeMoveTestCase("3q3k/4P3/8/8/8/8/8/7K w - - 0 1", "e7d8=N", "3N3k/8/8/8/8/8/8/7K b - - 0 1"),   // White pawn promotion and capture
			new MakeMoveTestCase("7k/8/8/8/8/8/1p6/2N4K b - - 0 1", "b2b1=R", "7k/8/8/8/8/8/8/1rN4K w - - 0 1"),   // Black pawn promotion
			new MakeMoveTestCase("7k/8/8/8/8/8/1p6/2N4K b - - 0 1", "b2c1=B", "7k/8/8/8/8/8/8/2b4K w - - 0 1"),   // Black pawn promotion and capture
			new MakeMoveTestCase("7k/8/8/1pP5/8/8/8/7K w - b6 0 1", "c5b6", "7k/8/1P6/8/8/8/8/7K b - - 0 1"),   // White EP
			new MakeMoveTestCase("7k/8/8/8/2pP4/8/8/7K b - d3 0 1", "c4d3", "7k/8/8/8/8/3p4/8/7K w - - 0 1"),   // Black EP
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "O-O", "r3k2r/8/8/8/8/8/8/R4RK1 b kq - 0 1"),   // White castling
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "O-O-O", "r3k2r/8/8/8/8/8/8/2KR3R b kq - 0 1"),   // White castling
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "O-O", "r4rk1/8/8/8/8/8/8/R3K2R w KQ - 0 1"),   // Black castling
			new MakeMoveTestCase("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "O-O-O", "2kr3r/8/8/8/8/8/8/R3K2R w KQ - 0 1")   // Black castling
		};
		
		final Fen fen = new Fen();
		final StandardAlgebraicNotationReader moveReader = new StandardAlgebraicNotationReader();
		final Move move = new Move();
		
		for (MakeMoveTestCase testCase: testCaseArray) {
			fen.readFenFromString(testCase.previousPosition);
			final Position previousPosition = fen.getPosition().copy();
			
			fen.readFenFromString(testCase.nextPosition);
			final Position nextPosition = fen.getPosition().copy();
			
			final PushbackReader reader = new PushbackReader(new StringReader(testCase.move));
			moveReader.readMove(reader, previousPosition, move);
			
			final Position position = previousPosition.copy();
			
			position.makeMove(move);
			position.checkIntegrity();
			
			if (!nextPosition.equals (position)) {
				Assert.fail();
			}
			
			position.undoMove(move);
			position.checkIntegrity();
			
			if (!previousPosition.equals (position)) {
				Assert.fail();
			}
		}
	}

	private static class StaticExchangeTestCase {
		public final String position;
		public final int color;
		public final int square;
		public final int evaluation;
		
		public StaticExchangeTestCase(final String position, final int color, final int square, final int evaluation) {
			this.position = position;
			this.color = color;
			this.square = square;
			this.evaluation = evaluation;
		}
	}

	@Test
	public void staticExchangeTest() throws IOException {
		final StaticExchangeTestCase[] testCaseArray = {
			new StaticExchangeTestCase("3r3k/6b1/8/8/3r4/8/2N2Q2/7K w - - 0 1", Color.WHITE, Square.D4, pte.getPieceTypeEvaluation(PieceType.ROOK) - pte.getPieceTypeEvaluation(PieceType.KNIGHT)),
			new StaticExchangeTestCase("7k/2Q5/3B2n1/8/5n2/8/8/7K w - - 0 1", Color.WHITE, Square.F4, 2 * pte.getPieceTypeEvaluation(PieceType.KNIGHT) - pte.getPieceTypeEvaluation(PieceType.BISHOP)),
			new StaticExchangeTestCase("5k2/8/2p5/1N6/2P5/8/8/4K3 b - - 0 1", Color.BLACK, Square.B5, pte.getPieceTypeEvaluation(PieceType.KNIGHT) - pte.getPieceTypeEvaluation(PieceType.PAWN)),
			new StaticExchangeTestCase("8/8/4k3/3N4/8/8/8/4K2B b - - 0 1", Color.BLACK, Square.D5, 0),
			new StaticExchangeTestCase("8/8/4k3/3N4/8/5K2/8/7B b - - 0 1", Color.BLACK, Square.D5, pte.getPieceTypeEvaluation(PieceType.KNIGHT))
		};
		
		final Fen fen = new Fen();
		
		for (StaticExchangeTestCase testCase: testCaseArray) {
			fen.readFenFromString(testCase.position);
			
			final Position beginPosition = fen.getPosition();
			final Position position = beginPosition.copy();
			
			final int evaluation = position.getStaticExchangeEvaluation(testCase.color, testCase.square, pte);
			
			Assert.assertEquals(testCase.position, testCase.evaluation, evaluation);
			Assert.assertEquals(testCase.position, beginPosition, position);
		}
	}

	private static class StaticExchangeMoveTestCase {
		public final String position;
		public final String move;
		public final int evaluation;

		public StaticExchangeMoveTestCase(final String position, final String move, final int evaluation) {
			this.position = position;
			this.move = move;
			this.evaluation = evaluation;
		}
	}

	@Test
	public void staticExchangeMoveTest() throws IOException {
		final StaticExchangeMoveTestCase[] testCaseArray = {
				new StaticExchangeMoveTestCase("3r3k/6b1/8/8/3r4/8/2N2Q2/7K w - - 0 1", "Qxd4", pte.getPieceTypeEvaluation(PieceType.ROOK) - pte.getPieceTypeEvaluation(PieceType.QUEEN)),
				new StaticExchangeMoveTestCase("7k/2Q5/3B2n1/8/5n2/8/8/7K w - - 0 1", "Bxf4", 2 * pte.getPieceTypeEvaluation(PieceType.KNIGHT) - pte.getPieceTypeEvaluation(PieceType.BISHOP)),
				new StaticExchangeMoveTestCase("5k2/8/2p5/1N6/2P5/8/8/4K3 b - - 0 1", "cxb5", pte.getPieceTypeEvaluation(PieceType.KNIGHT) - pte.getPieceTypeEvaluation(PieceType.PAWN)),
				new StaticExchangeMoveTestCase("5k2/8/2p5/1N6/2P5/8/8/4K3 b - - 0 1", "c5", 0),
				new StaticExchangeMoveTestCase("8/8/4k3/3N4/8/5K2/8/7B b - - 0 1", "Kxd5", pte.getPieceTypeEvaluation(PieceType.KNIGHT)),
				new StaticExchangeMoveTestCase("QR6/7k/8/8/7q/8/6P1/6K1 b - - 0 1", "Qe1", 0),
				new StaticExchangeMoveTestCase("QR6/7k/8/8/8/8/6PK/4q3 b - - 0 1", "Qh4", 0)
		};

		final Fen fen = new Fen();

		for (StaticExchangeMoveTestCase testCase: testCaseArray) {
			fen.readFenFromString(testCase.position);

			final Position beginPosition = fen.getPosition();
			final Position position = beginPosition.copy();

			final StandardAlgebraicNotationReader moveReader = new StandardAlgebraicNotationReader();
			final Move move = new Move();
			moveReader.readMove(new PushbackReader(new StringReader(testCase.move)), position, move);

			final int evaluation = position.getStaticExchangeEvaluation(position.getOnTurn(), move, pte);

			Assert.assertEquals(testCase.position, testCase.evaluation, evaluation);
			Assert.assertEquals(testCase.position, beginPosition, position);
		}
	}
	
	@Test
	public void checkSpeedTest() {
		final Position position = new Position();
		position.setInitialPosition();
		
		final int count = 100000000;
		long t1 = System.currentTimeMillis();
		
		for (int i = 0; i < count; i++) {
			position.isCheck();
		}
		
		long t2 = System.currentTimeMillis();
		
		System.out.println(1000L * count / (t2 - t1) + " isCheck per second");
	}
}
