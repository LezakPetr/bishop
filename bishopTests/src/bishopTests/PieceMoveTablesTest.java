package bishopTests;

import org.junit.Test;

import bishop.base.BitBoard;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Square;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PawnMoveTable;

public class PieceMoveTablesTest {

	@Test
	public void figureAttackTest() {
		class TestValue {
			public final int pieceType;
			public final int beginSquare;
			public final long targetBoard;
			
			public TestValue (final int pieceType, final int beginSquare, final int[] targetSquares) {
				this.pieceType = pieceType;
				this.beginSquare = beginSquare;
				this.targetBoard = BitBoard.fromSquareArray(targetSquares);
			}
		}
		
		TestValue[] testValueArray = {
			new TestValue(PieceType.KING, Square.E5, new int[] {Square.D6, Square.E6, Square.F6, Square.D5, Square.F5, Square.D4, Square.E4, Square.F4}),
			new TestValue(PieceType.KING, Square.A1, new int[] {Square.A2, Square.B2, Square.B1}),
			new TestValue(PieceType.QUEEN, Square.C4, new int[] {Square.A4, Square.B4, Square.D4, Square.E4, Square.F4, Square.G4, Square.H4, Square.C1, Square.C2, Square.C3, Square.C5, Square.C6, Square.C7, Square.C8, Square.A6, Square.B5, Square.D3, Square.E2, Square.F1, Square.A2, Square.B3, Square.D5, Square.E6, Square.F7, Square.G8}),
			new TestValue(PieceType.ROOK, Square.D6, new int[] {Square.A6, Square.B6, Square.C6, Square.E6, Square.F6, Square.G6, Square.H6, Square.D1, Square.D2, Square.D3, Square.D4, Square.D5, Square.D7, Square.D8}),
			new TestValue(PieceType.BISHOP, Square.D3, new int[] {Square.B1, Square.C2, Square.E4, Square.F5, Square.G6, Square.H7, Square.A6, Square.B5, Square.C4, Square.E2, Square.F1}),
			new TestValue(PieceType.KNIGHT, Square.E4, new int[] {Square.C3, Square.D2, Square.F2, Square.G3, Square.G5, Square.F6, Square.D6, Square.C5}),
			new TestValue(PieceType.KNIGHT, Square.A7, new int[] {Square.C8, Square.C6, Square.B5})
		};
		
		for (TestValue testValue: testValueArray) {
			final long expectedBoard = testValue.targetBoard;
			final long returnedBoard = FigureAttackTable.getItem(testValue.pieceType, testValue.beginSquare);
			
			TestUtils.assertBitBoardsEqual (expectedBoard, returnedBoard);
		}
	}
	
	
	@Test
	public void pawnAttackTest() {
		class TestValue {
			public final int color;
			public final int beginSquare;
			public final long targetBoard;
			
			public TestValue (final int color, final int beginSquare, final int[] targetSquares) {
				this.color = color;
				this.beginSquare = beginSquare;
				this.targetBoard = BitBoard.fromSquareArray(targetSquares);
			}
		}
		
		TestValue[] testValueArray = {
				new TestValue(Color.WHITE, Square.E5, new int[] {Square.D6, Square.F6}),
				new TestValue(Color.WHITE, Square.A3, new int[] {Square.B4}),
				new TestValue(Color.WHITE, Square.H7, new int[] {Square.G8}),
				new TestValue(Color.WHITE, Square.F8, new int[] {}),
				new TestValue(Color.WHITE, Square.B1, new int[] {Square.A2, Square.C2}),
				new TestValue(Color.BLACK, Square.E5, new int[] {Square.D4, Square.F4}),
				new TestValue(Color.BLACK, Square.A3, new int[] {Square.B2}),
				new TestValue(Color.BLACK, Square.H7, new int[] {Square.G6}),
				new TestValue(Color.BLACK, Square.F8, new int[] {Square.E7, Square.G7}),
				new TestValue(Color.BLACK, Square.B1, new int[] {})
		};
		
		for (TestValue testValue: testValueArray) {
			final long expectedBoard = testValue.targetBoard;
			final long returnedBoard = PawnAttackTable.getItem(testValue.color, testValue.beginSquare);
			
			TestUtils.assertBitBoardsEqual (expectedBoard, returnedBoard);
		}
	}

	@Test
	public void pawnMoveTest() {
		class TestValue {
			public final int color;
			public final int beginSquare;
			public final long targetBoard;
			
			public TestValue (final int color, final int beginSquare, final int[] targetSquares) {
				this.color = color;
				this.beginSquare = beginSquare;
				this.targetBoard = BitBoard.fromSquareArray(targetSquares);
			}
		}
		
		TestValue[] testValueArray = {
				new TestValue(Color.WHITE, Square.E2, new int[] {Square.E3, Square.E4}),
				new TestValue(Color.WHITE, Square.C4, new int[] {Square.C5}),
				new TestValue(Color.WHITE, Square.F7, new int[] {Square.F8}),
				new TestValue(Color.WHITE, Square.A1, new int[] {}),
				new TestValue(Color.WHITE, Square.D8, new int[] {}),
				new TestValue(Color.BLACK, Square.E2, new int[] {Square.E1}),
				new TestValue(Color.BLACK, Square.C4, new int[] {Square.C3}),
				new TestValue(Color.BLACK, Square.F7, new int[] {Square.F6, Square.F5}),
				new TestValue(Color.BLACK, Square.A1, new int[] {}),
				new TestValue(Color.BLACK, Square.D8, new int[] {})
		};
		
		for (TestValue testValue: testValueArray) {
			final long expectedBoard = testValue.targetBoard;
			final long returnedBoard = PawnMoveTable.getItem(testValue.color, testValue.beginSquare);
			
			TestUtils.assertBitBoardsEqual (expectedBoard, returnedBoard);
		}

	}

	
}
