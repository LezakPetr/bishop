package bishopTests;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.Assert;

import bishop.base.Fen;
import bishop.base.Position;
import bishop.base.PositionReader;
import bishop.base.PositionWriter;


public class PositionIoTest {
	
	private static final String[] TEST_POSITIONS = {
		"b3R1N1/4n3/4k3/n7/7r/1B6/5K2/3q4 b - - 0 1",
		"1q2k1r1/8/3QB3/8/5K2/1N6/3N4/2b4R w - - 0 1",
		"5k2/7P/6p1/p1pppp2/PpP3P1/1P1P1P2/4P2p/5K2 w - - 0 1",
		"8/4p2P/4k1p1/p1pp1p2/PpP3P1/1P1P1P2/4P2p/5KB1 b - - 0 1",
		"k7/8/8/3PpP2/8/8/8/K7 w - e6 0 1",
		"k7/8/8/8/6pP/8/8/K7 b - h3 0 1",
		"4k3/8/8/8/8/5b2/8/R3K2R w KQ - 0 1",
		"4k3/8/8/8/2b5/n7/8/R3K2R w KQ - 0 1",
		"r3k2r/8/8/8/8/8/4Q3/4K3 b kq - 0 1",
		"r3k2r/8/8/8/8/8/8/4K3 b - - 0 1",
		"4k3/8/8/8/8/8/1p4p1/R3K2R b KQ - 0 1",
		"r1bq1rk1/pp4bp/3p1np1/n1pPpp2/2P5/2N3P1/PPQNPPBP/R1B2RK1 w - c6 0 1",
		"r1bq1rk1/pp4bp/3p1np1/n1pPpp2/2P5/2N3P1/PPQNPPBP/R1B2RK1 w - e6 0 1"
	};
	
	@Test
	public void test() throws IOException {
		for (String positionStr: TEST_POSITIONS) {
			final Fen fen = new Fen();
			fen.readFenFromString(positionStr);
			
			final Position position = fen.getPosition();
			
			final PositionWriter writer = new PositionWriter();
			writer.getPosition().assign(position);
			
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			writer.writePositionToStream(outputStream);
			
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			final PositionReader reader = new PositionReader();
			reader.readPositionFromStream(inputStream);
			
			Assert.assertTrue("Not all bytes read", inputStream.read() < 0);
			
			final Position givenPosition = reader.getPosition();
			
			Assert.assertEquals(position, givenPosition);
		}
	}
}
