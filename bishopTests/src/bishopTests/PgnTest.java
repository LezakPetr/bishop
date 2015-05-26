package bishopTests;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.Assert;
import org.junit.Test;
import bishop.base.PgnReader;
import bishop.base.PgnWriter;

public class PgnTest {
	
	@Test
	public void readWriteTest() throws IOException {
		final String[] testCaseArray = {
			"[Event \"Test event\"]\n" +
			"[Site \"Test site\"]\n" +
			"[Date \"2012.01.21\"]\n" +
			"[Round \"1\"]\n" +
			"[White \"White player\"]\n" +
			"[Black \"Black player\"]\n" +
			"[Result \"*\"]\n" +
			"\n" +
			"1. e4 e5 2. Nf3 Nc6 3. Bc4 Nf6 4. Ng5 d5 5. exd5 Nxd5 6. Nxf7 Kxf7 7. Qf3+ Ke6 *\n" +
			"\n",
			
			"[Event \"Test event\"]\n" +
			"[Site \"Test site\"]\n" +
			"[Date \"2012.01.21\"]\n" +
			"[Round \"1\"]\n" +
			"[White \"White player\"]\n" +
			"[Black \"Black player\"]\n" +
			"[Result \"*\"]\n" +
			"\n" +
			"1. e4 ( 1. d4 ( 1. c3 ) 1... e6 ) 1... e5 2. Nf3 ( 2. Nf3 f6 ) 2... d6 ( 2... c6\n" +
			"3. c3 c5 ( 3... b5 ) ) *\n" +
			"\n" +
			"[Event \"ee\"]\n" +
			"[Site \"ss\"]\n" +
			"[Date \"2012.01.21\"]\n" +
			"[Round \"2\"]\n" +
			"[White \"wh\"]\n" +
			"[Black \"bl\"]\n" +
			"[Result \"1-0\"]\n" +
			"\n" +
			"1. d4 d5 2. c3 c6 3. Nf3 e6 4. Bf4 Qb6 5. Qc2 Nf6 6. e3 Bd7 7. Nbd2 Na6 ( 7...\n" + 
			"g6 ( 7... Bc8 8. Qb3 ) 8. Bd3 Be7 9. b3 ( 9. a4 a5 ) ) 8. Bd3 Be7 9. O-O O-O 1-0\n" +
			"\n",
			
			"[Event \"FEN test\"]\n" +
			"[Site \"site\"]\n" +
			"[Date \"2012.01.29\"]\n" +
			"[Round \"3\"]\n" +
			"[White \"White player\"]\n" +
			"[Black \"Black player\"]\n" +
			"[Result \"1-0\"]\n" +
			"[FEN \"4k3/8/4K3/8/4R3/8/8/8 w - - 0 1\"]\n" +
			"\n" +
			"1. Re3 Kd8 2. Rc3 $1 Ke8 $2 3. Rc8# 1-0\n" +
			"\n",
			
			"[Event \"FEN test\"]\n" +
			"[Site \"site\"]\n" +
			"[Date \"2012.01.29\"]\n" +
			"[Round \"3\"]\n" +
			"[White \"White player\"]\n" +
			"[Black \"Black player\"]\n" +
			"[Result \"1-0\"]\n" +
			"[FEN \"4k3/8/4K3/8/4R3/8/8/8 w - - 0 1\"]\n" +
			"\n" +
			"1. Re3 {Commentary1} Kd8 2. Rc3 $1 {Commentary2} Ke8 $2 3. Rc8# 1-0\n" +
			"\n"
		};
		
		final PgnReader pgnReader = new PgnReader();
		final PgnWriter pgnWriter = new PgnWriter();
		
		for (String testCase: testCaseArray) {
			// Read PGN
			final StringReader stringReader = new StringReader(testCase);
			final PushbackReader pushbackReader = new PushbackReader(stringReader);
			
			pgnReader.readPgn(pushbackReader);
			
			// Write PGN
			pgnWriter.getGameList().clear();
			pgnWriter.getGameList().addAll(pgnReader.getGameList());
			
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			
			pgnWriter.writePgn(printWriter);
			printWriter.flush();
			
			final String writterString = stringWriter.toString();
			Assert.assertEquals(testCase, writterString);
		}
	}
	
}
