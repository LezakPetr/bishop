package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.text.DecimalFormat;

public class IoUtils {
	
	public static final String NEW_LINE = System.lineSeparator();
	
	public static final char QUOTE_CHAR = '"';
	public static final char BACKSLASH_CHAR = '\\';
	
	private static final String BACKSLASHED_CHARACTERS = "=\n";
	private static final String PLACEHOLDER_CHARACTERS = "=n";
	
	
	public static final int SHORT_BYTES = Short.SIZE / Byte.SIZE;
	public static final int INT_BYTES = Integer.SIZE / Byte.SIZE;
	public static final int LONG_BYTES = Long.SIZE / Byte.SIZE;

	private static final DecimalFormat COUNT_FORMAT = new DecimalFormat("###,###,###,###,###,###,###");

	/**
	 * Reads character from given reader and returns it.
	 * @param reader source reader
	 * @return read character
	 * @throws IOException when IO failure occurs or when end-of-stream is reached
	 */
	public static char readChar(final Reader reader) throws IOException {
		final int c = reader.read();

		if (c < 0)
			throw new IOException("End-of-stream reached");

		return (char) c;
	}

	/**
	 * Skips all white space characters in the reader.
	 * @param reader source reader
	 * @throws IOException when IO failure occurs
	 */
	public static void skipWhiteSpace (final PushbackReader reader) throws IOException {
		while (!IoUtils.isEndOfStream(reader)) {
			final char c = readChar(reader);

			if (!Character.isWhitespace(c)) {
				reader.unread(c);
				break;
			}
		}
	}

	/**
	 * Checks if there is end-of-stream .
	 * @param reader
	 * @return true if there is end-of-stream, false if at least one more character may be read
	 * @throws IOException when IO failure occurs
	 */
	public static boolean isEndOfStream(final PushbackReader reader) throws IOException {
		final int c = reader.read();

		if (c >= 0) {
			reader.unread((char) c);

			return false;
		}
		else
			return true;
	}

	/**
	 * Skips white space characters and reads integral number. 
	 * @param reader source reader
	 * @return read number
	 * @throws IOException when IO failure occurs or when there is no integral number in the stream
	 */
	public static int readInt (final PushbackReader reader) throws IOException {
		skipWhiteSpace(reader);

		// Read '+' or '-'
		boolean minus = false;

		{
			final char c = readChar(reader);

			switch (c) {
				case '+':
					break;

				case '-':
					minus = true;
					break;
				
				default:
					reader.unread(c);
					break;
			}
		}

		// Read number
		boolean digitRead = false;
		int number = 0;

		while (!IoUtils.isEndOfStream(reader)) {
			final char c = readChar(reader);

			if (c < '0' || c > '9') {
				reader.unread(c);
				break;
			}

			final int digit = (int) (c - '0');

			number = 10*number + digit;
			digitRead = true;
		}

		if (!digitRead)
			throw new IOException("There is no digit in the stream");

		// Process minus and return number
		if (minus)
			number = -number;

		return number;
	}
	
	public static void checkExpectedChar (final PushbackReader reader, final char expectedChar) throws IOException {
		skipWhiteSpace(reader);
		
		final char ch = IoUtils.readChar(reader);
		
		if (ch != expectedChar)
			throw new RuntimeException("Expected character '" + expectedChar + "', but read '" + ch + "'");
	}
	
	public static void writeQuotedString(final PrintWriter writer, final String value) {
		writer.print(QUOTE_CHAR);
		
		for (int i = 0; i < value.length(); i++) {
			final char ch = value.charAt(i);
			
			if (ch == QUOTE_CHAR || ch == BACKSLASH_CHAR)
				writer.print(BACKSLASH_CHAR);
			
			writer.print(ch);
		}
		
		writer.print(QUOTE_CHAR);
	}
	
	public static String readQuotedString(final PushbackReader reader) throws IOException {
		checkExpectedChar(reader, QUOTE_CHAR);
		
		final StringBuilder builder = new StringBuilder();
		
		while (true) {
			char ch = readChar(reader);
			
			if (ch == QUOTE_CHAR)
				break;
			
			if (ch == BACKSLASH_CHAR)
				ch = readChar(reader);
			
			builder.append(ch);
		}
		
		return builder.toString();
	}

	public static String readString(final PushbackReader reader, final ICharacterFilter filter) throws IOException {
		skipWhiteSpace(reader);
		
		final StringBuilder builder = new StringBuilder();
		
		while (!isEndOfStream(reader)) {
			final char ch = readChar(reader);
			
			if (filter.filterCharacter(ch))
				builder.append(ch);
			else {
				reader.unread(ch);
				break;
			}
		}
		
		return builder.toString();
	}
	
	public static String readString(final PushbackReader reader) throws IOException {
		return readString (reader, CharacterFilters.getNonWhiteSpaceFilter());
	}
	
	public static void writeBackslashedString (final Writer writer, final String str) throws IOException {
		for (int i = 0; i < str.length(); i++) {
			final char ch = str.charAt(i);
			final int index = BACKSLASHED_CHARACTERS.indexOf(ch);
			
			if (index >= 0) {
				writer.write(BACKSLASH_CHAR);
				writer.write(PLACEHOLDER_CHARACTERS.charAt(index));
			}
			else
				writer.write(ch);
		}
	}
	
	public static String readBackslashedString (final PushbackReader reader, final String terminationMarks) throws IOException {
		final StringBuilder builder = new StringBuilder();
		
		while (!isEndOfStream(reader)) {
			final char ch = readChar(reader);
			
			if (terminationMarks.indexOf(ch) >= 0) {
				reader.unread(ch);
				break;
			}
			
			if (ch == BACKSLASH_CHAR) {
				final char placeholderChar = readChar(reader);
				final int index = PLACEHOLDER_CHARACTERS.indexOf(placeholderChar);
				
				if (index < 0)
					throw new RuntimeException("Unknown placeholder character");
				
				builder.append(BACKSLASHED_CHARACTERS.charAt(index));
			}
			else
				builder.append(ch);
		}
		
		return builder.toString();
	}
	
	public static byte[] readFile (final java.io.File file, final int maxLength) throws IOException {
		final long length = file.length();
		
		if (length > maxLength) {
			throw new RuntimeException("File is too long");
		}
		
		final int truncatedLength = (int) length;
		final byte[] data = new byte[truncatedLength];
		
		final FileInputStream stream = new FileInputStream(file);
		
		try {
			int bytesRead = 0;
			
			while (bytesRead < truncatedLength) {
				final int ret = stream.read(data, bytesRead, truncatedLength - bytesRead);
				
				if (ret <= 0)
					throw new IOException("End of file reached");
				
				bytesRead += ret;
			}
		}
		finally {
			stream.close();
		}
		
		return data;
	}
	
	public static void copyStream (final InputStream inputStream, final OutputStream outputStream) throws IOException {
		final byte[] buffer = new byte[1024];
		
		while (true) {
			final int read = inputStream.read(buffer);
			
			if (read <= 0)
				break;
			
			outputStream.write(buffer, 0, read);
		}
	}
	
	public static void downloadFile (final URL url, final File file) throws IOException {
		final InputStream inputStream = url.openStream();
		
		try {
			final FileOutputStream outputStream = new FileOutputStream (file);
			
			try {
				copyStream(inputStream, outputStream);
			}
			finally {
				outputStream.close();
			}
		}
		finally {
			inputStream.close();
		}
	}
	
	public static File downloadFileToTemp (final URL url) throws IOException {
		final File file = File.createTempFile("bishop", "");
		file.deleteOnExit();
		
		downloadFile(url, file);
		
		return file;
	}
	
	/**
	 * Reads byte from given stream and returns it.
	 * @param stream source stream
	 * @return read byte
	 * @throws IOException when IO failure occurs or when end-of-stream is reached
	 */
	public static byte readByteBinary(final InputStream stream) throws IOException {
		final int b = stream.read();

		if (b < 0)
			throw new IOException("End-of-stream reached");

		return (byte) b;
	}
	
	/**
	 * Reads byte array from given stream and returns it.
	 * @param stream source stream
	 * @param length number of bytes
	 * @return array with read bytes
	 * @throws IOException when IO failure occurs or when end-of-stream is reached
	 */
	public static byte[] readByteArray(final InputStream stream, final int length) throws IOException {
		final byte[] array = new byte[length];
		int bytesRead = tryReadByteArray(stream, array);
		
		if (bytesRead < length)
			throw new IOException("End of stream reached");
		
		return array;
	}

	/**
	 * Tries to read byte array from given input stream.
	 * Method tries to read as much as possible and can read smaller number of bytes only in case of end-of-stream.
	 * @param stream source stream
	 * @param array target array
	 * @return number of bytes read
	 * @throws IOException when IO failure occurs
	 */
	public static int tryReadByteArray(final InputStream stream, final byte[] array) throws IOException {
		int bytesRead = 0;
		
		while (bytesRead < array.length) {
			final int ret = stream.read(array, bytesRead, array.length - bytesRead);
			
			if (ret < 0)
				break;
			
			bytesRead += ret;
		}
		
		return bytesRead;
	}
	
	public static void writeNumberBinary(final OutputStream stream, final long num, final int length) throws IOException {
		for (int i = length - 1; i >= 0; i--) {
			final int digit = (int) (num >>> (8 * i));
				
			stream.write(digit);
		}
	}

	private static long readNumberBinary(final InputStream stream, final int length) throws IOException {
		long num = 0;
		
		for (int i = 0; i < length; i++) {
			final long digit = readByteBinary(stream);
			
			num = (num << 8) | (digit & 0xFF);
		}
		
		return num;
	}
	
	public static long readUnsignedNumberBinary(final InputStream stream, final int length) throws IOException {
		return readNumberBinary(stream, length);
	}
	
	public static long readSignedNumberBinary(final InputStream stream, final int length) throws IOException {
		final long number = readNumberBinary(stream, length);
		final int shift = 8 * (LONG_BYTES - length);
		
		return (number << shift) >> shift;   // Sign extension
	}
	
	public static void skip (final InputStream stream, final long count) throws IOException {
		long skipped = 0;
		
		while (skipped < count) {
			final long ret = stream.skip(count - skipped);
			
			if (ret <= 0)
				throw new IOException("Cannot skip bytes");
			
			skipped += ret;
		}
	}

	public static PushbackReader getPushbackReader(final String str) {
		return new PushbackReader(new StringReader(str));
	}

	public static void writeSize (final PrintStream stream, final double size) {
		final double kilo = 1024;
		final double mega = kilo * kilo;
		final double giga = kilo * mega;

		if (size < kilo) {
			stream.format("%1$.2fB", size);
			return;
		}

		if (size < mega) {
			stream.format("%1$.2fkiB", size / kilo);
			return;
		}

		if (size < giga) {
			stream.format("%1$.2fMeB", size / mega);
			return;
		}

		stream.format("%1$.2fGiB", size / giga);
	}

	public static String countToString(final long count) {
		return COUNT_FORMAT.format(count);
	}

	public static boolean hasExpectedBytes (final InputStream stream, final byte[] expectedBytes) throws IOException {
		boolean expected = true;
		
		for (int i = 0; i < expectedBytes.length; i++) {
			final int readByte = IoUtils.readByteBinary(stream);
			
			expected = expected && (readByte == expectedBytes[i]);
		}
		
		return expected;
	}
}
