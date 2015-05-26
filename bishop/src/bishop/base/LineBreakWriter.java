package bishop.base;

import java.io.IOException;
import java.io.Writer;

public class LineBreakWriter extends Writer {

	private final Writer baseWriter;
	private final int maxCodePointsOnLine;
	private final StringBuilder lineBuffer;
	private int codePointCount;
	private int lastWhiteSpaceIndex;
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	

	public LineBreakWriter(final Writer baseWriter, final int maxCodePointsOnLine) {
		this.baseWriter = baseWriter;
		this.maxCodePointsOnLine = maxCodePointsOnLine;
		this.lineBuffer = new StringBuilder();
		
		inititlize();
	}

	@Override
	public void write(final char[] buffer, final int offset, final int length) throws IOException {
		int actualIndex = lineBuffer.length();
		
		lineBuffer.append(buffer, offset, length);
		
		while (actualIndex < lineBuffer.length()) {
			final int actualCodePoint = lineBuffer.codePointAt(actualIndex);
			
			if (Character.isWhitespace(actualCodePoint)) {
				lastWhiteSpaceIndex = actualIndex;
			}
			
			codePointCount++;
			
			if (codePointCount > maxCodePointsOnLine && lastWhiteSpaceIndex > 0) {
				final String str = lineBuffer.substring(0, lastWhiteSpaceIndex);
				
				baseWriter.append(str);
				baseWriter.write(LINE_SEPARATOR);
				
				lineBuffer.delete(0, lastWhiteSpaceIndex+1);
				
				actualIndex = 0;
				codePointCount = 0;
			}
			
			
			actualIndex += Character.charCount(actualCodePoint);;
		}
	}

	@Override
	public void close() throws IOException {
		flush();
		baseWriter.close();
	}

	@Override
	public void flush() throws IOException {
		baseWriter.write(lineBuffer.toString());
		baseWriter.flush();
		
		inititlize();
	}

	private void inititlize() {
		lineBuffer.delete(0, lineBuffer.length());
		codePointCount = 0;
		lastWhiteSpaceIndex = 0;
	}

}
