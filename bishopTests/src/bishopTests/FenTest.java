package bishopTests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import bishop.base.Fen;
import bishop.base.Position;
import bishop.base.PositionReader;
import bishop.base.PositionWriter;


public class FenTest {
	private static final String[] TEST_STRING_LIST = {
		"rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
		"r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 10",
		"r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b - - 0 5",
		"r3k2r/ppp1pppp/8/8/3pP3/8/PPPP1PPP/R3K2R b - e3 0 5",
		"r3k2r/p1pp1ppp/8/1pP5/4P3/8/PP1P1PPP/R3K2R w - b6 10 5",
		"r3k2r/p1pp1ppp/8/1p6/4P3/8/PPPP1PPP/R3K2R w - - 7 10"
	};

	@Test
	public void fenTest() throws IOException {
		final Fen fen = new Fen();
		
		for (String testString: TEST_STRING_LIST) {
			fen.readFenFromString(testString);
	
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			
			fen.writeFen(printWriter);
			printWriter.flush();
			
			assertEquals (testString, stringWriter.toString());
		}
	}
	
	@Test
	public void positionIoTest() throws IOException {
		final Fen fen = new Fen();
		
		int totalSize = 0;
		
		for (String testString: TEST_STRING_LIST) {
			fen.readFenFromString(testString);
			
			final Position position = fen.getPosition();
			
			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			final PositionWriter writer = new PositionWriter();
			writer.getPosition().assign(position);
			writer.writePositionToStream(stream);
			
			final byte[] data = stream.toByteArray();
			totalSize += data.length;
			System.out.println (data.length);
			final PositionReader reader = new PositionReader();
			reader.readPositionFromStream(new ByteArrayInputStream(data));
			
			final Position readPosition = reader.getPosition();
			
			assertEquals (position, readPosition);
		}
		
		final double avgSize = (double) totalSize / (double) TEST_STRING_LIST.length;
		System.out.println ("Average size = " + avgSize + "B");
	}

}
