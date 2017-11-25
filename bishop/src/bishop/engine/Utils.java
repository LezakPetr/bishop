package bishop.engine;

import java.io.PrintWriter;

import utils.Logger;
import bishop.base.BitBoard;
import bishop.base.Color;

public class Utils {
	public static void joinThread (final Thread thread) {
		boolean joined = false;
		
		do {
			try {
				thread.join();
				joined = true;
			}
			catch (InterruptedException ex) {
				Logger.logException(ex);
			}
		} while (!joined);
	}
	
	public static void writeBoards (final PrintWriter writer, final long[] boards, final String message) {
		writer.print (message);
		
		writer.print ("white = {");
		BitBoard.write(writer, boards[Color.WHITE]);
		
		writer.print ("}, black = {");
		BitBoard.write(writer, boards[Color.BLACK]);
		
		writer.println("}");
	}
	
}
