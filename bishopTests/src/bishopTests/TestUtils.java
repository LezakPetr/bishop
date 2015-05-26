package bishopTests;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.AssertionFailedError;
import bishop.base.BitBoard;

public class TestUtils {
	
	/**
	 * Checks if given bit boards are same. If not AssertionFailedError is thrown.
	 * @param expectedBoard expected bit board
	 * @param calculatedBoard calculated bit board
	 */
	public static void assertBitBoardsEqual (final long expectedBoard, final long calculatedBoard) {
		if (expectedBoard != calculatedBoard) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			
			printWriter.print("Expected: {");
			BitBoard.write (printWriter, expectedBoard);
			
			printWriter.print("}, returned: {");
			BitBoard.write (printWriter, calculatedBoard);
			printWriter.print("}");
			
			printWriter.flush();
			
			throw new AssertionFailedError(stringWriter.toString());
		}
	}
}
