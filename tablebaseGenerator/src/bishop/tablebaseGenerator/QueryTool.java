package bishop.tablebaseGenerator;

import java.io.IOException;
import java.io.PushbackReader;
import utils.IoUtils;

import bishop.base.Color;
import bishop.base.Piece;
import bishop.base.Square;

public class QueryTool {
		
	public static void main(final String[] args) throws IOException {
		if (args.length != 4) {
			System.out.println ("Usage: QueryTool directory fixed variable onTurn");
			return;
		} 
		
		final QueryProcessor processor = new QueryProcessor();
		final PushbackReader fixedReader = IoUtils.getPushbackReader(args[1]);
		
		while (!IoUtils.isEndOfStream(fixedReader)) {
			final Piece piece = Piece.read(fixedReader);
			final int square = Square.read(fixedReader);
			
			processor.addFixedPiece(piece, square);
		}
		
		final PushbackReader variableReader = IoUtils.getPushbackReader(args[2]);
		
		while (!IoUtils.isEndOfStream(variableReader)) {
			final Piece piece = Piece.read(variableReader);
			
			processor.addVariablePiece(piece);
		}
		
		final int onTurn = Color.parseNotation(args[3].charAt(0));
		processor.setOnTurn(onTurn);
		
		processor.setDirectory (args[0]);
		
		processor.run();
	}
	
}
