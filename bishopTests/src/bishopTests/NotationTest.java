package bishopTests;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.Assert;
import org.junit.Test;
import bishop.base.Fen;
import bishop.base.INotationReader;
import bishop.base.INotationWriter;
import bishop.base.Move;
import bishop.base.MoveParser;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Square;
import bishop.base.StandardAlgebraicNotationReader;
import bishop.base.StandardAlgebraicNotationWriter;

public class NotationTest {

	private static class TestValue {
		public final String fen;
		public final int beginSquare;
		public final int targetSquare;
		public final int promotionPieceType;
		public final String notation;
		
		public TestValue(final String fen, final int beginSquare, final int targetSquare, final int promotionPieceType, final String notation) {
			this.fen = fen;
			this.beginSquare = beginSquare;
			this.targetSquare = targetSquare;
			this.promotionPieceType = promotionPieceType;
			this.notation = notation;
		}
	}
	
	@Test
	public void standardAlgebraicNotationTest() throws IOException {
		final TestValue[] testValueArray = {
			new TestValue ("8/8/4k3/8/7R/3K4/2N2N2/8 w - - 0 1", Square.C2, Square.D4, PieceType.NONE, "Nd4+"),
			new TestValue ("8/8/5k2/8/8/8/8/1R2R1K1 w - - 0 1", Square.B1, Square.D1, PieceType.NONE, "Rbd1"),
			new TestValue ("6k1/8/8/r7/8/8/r7/6K1 b - - 0 1", Square.A5, Square.A4, PieceType.NONE, "R5a4"),
			new TestValue ("6k1/8/2n1n3/8/8/8/2n5/6K1 b - - 0 1", Square.C6, Square.D4, PieceType.NONE, "Nc6d4"),
			new TestValue ("6k1/8/2n1n3/8/3B4/8/2n5/6K1 b - - 0 1", Square.C2, Square.D4, PieceType.NONE, "N2xd4"),
			new TestValue ("5k2/8/2B5/8/8/5B2/8/5K2 w - - 0 1", Square.C6, Square.D5, PieceType.NONE, "Bcd5"),
			new TestValue ("8/3P2k1/8/8/8/8/8/6K1 w - - 0 1", Square.D7, Square.D8, PieceType.QUEEN, "d8=Q"),
			new TestValue ("6k1/8/8/8/8/8/4p3/3Q2K1 b - - 0 1", Square.E2, Square.D1, PieceType.ROOK, "exd1=R+"),
			new TestValue ("k7/8/8/8/8/8/8/4K2R w K - 0 1", Square.E1, Square.G1, PieceType.NONE, "O-O"),
			new TestValue ("r3k3/8/8/8/8/8/8/4K3 b q - 0 1", Square.E8, Square.C8, PieceType.NONE, "O-O-O"),
			new TestValue ("4k3/8/8/5pP1/8/8/8/4K3 w - f6 0 1", Square.G5, Square.F6, PieceType.NONE, "gxf6")   // En-passant
		};
		
		final Fen fen = new Fen();
		final INotationWriter notationWriter = new StandardAlgebraicNotationWriter();
		final INotationReader notationReader = new StandardAlgebraicNotationReader();
		final MoveParser parser = new MoveParser();
		
		for (TestValue testValue: testValueArray) {
			// Read test case
			fen.readFen(new PushbackReader(new StringReader(testValue.fen)));
			
			final Position position = fen.getPosition();
			
			parser.initPosition(position);
			parser.filterByBeginSquare(testValue.beginSquare);
			parser.filterByTargetSquare(testValue.targetSquare);
			parser.filterByPromotionPieceType(testValue.promotionPieceType);
			
			final Move move = parser.getMoveList().get(0);

			// Test notation writer
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			
			notationWriter.writeMove(printWriter, position, move);
			printWriter.flush();

			final String notationStr = stringWriter.toString();
			Assert.assertEquals(testValue.notation, notationStr);
			
			// Test notation reader
			final StringReader moveStringReader = new StringReader(testValue.notation);
			final PushbackReader movePushbackReader = new PushbackReader(moveStringReader);
			final Move readMove = new Move();
			notationReader.readMove(movePushbackReader, position, readMove);
			
			Assert.assertEquals(move, readMove);
		}
	}
}
