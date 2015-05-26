package bishop.interpreter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackReader;
import java.util.Arrays;

import utils.IoUtils;

public class Bytecode {
	
	private static final int DATA_LENGTH_LENGTH = 4;
	
	public static final byte OPCODE_BYTE = 0;
	public static final byte OPCODE_LONG = 1;
	public static final byte OPCODE_SWITCH = 2;
	
	public static final String TOKEN_SWITCH = "switch";
	
		
	private final byte[] data;
	
	public Bytecode(final byte[] data) {
		this.data = Arrays.copyOf(data, data.length);
	}
	
	public void writeToStream (final OutputStream stream) throws IOException {
		IoUtils.writeNumberBinary(stream, data.length, DATA_LENGTH_LENGTH);
		stream.write(data);
	}
	
	public static Bytecode readFromStream(final InputStream stream) throws IOException {
		final int length = (int) IoUtils.readNumberBinary(stream, DATA_LENGTH_LENGTH);
		final byte[] data = IoUtils.readByteArray(stream, length);
		
		return new Bytecode(data);
	}
	
	public static Bytecode parse (final PushbackReader reader) throws IOException {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		while (!IoUtils.isEndOfStream(reader)) {
			final String token = IoUtils.readString(reader);
			
			if (!token.isEmpty())
				parseToken(token, stream);
		}
		
		return new Bytecode(stream.toByteArray());
	}
	
	private static void parseToken (final String token, final OutputStream stream) throws IOException {
		final OperatorRecord record = Operators.getRecordForToken(token);
		
		if (record != null) {
			stream.write(record.getOpcode());
			return;
		}
		
		if (token.startsWith(TOKEN_SWITCH)) {
			processSwitch(token, stream);
			return;
		}

		final char ch = token.charAt(0);
		
		if (ch == '+' || ch == '-' || Character.isDigit(ch)) {
			processNumber(token, stream);
			return;
		}
		
		throw new RuntimeException("Unknown token: " + token);
	}

	private static void processNumber(final String token, final OutputStream stream) throws IOException {
		final long num = Long.decode(token);
		
		if (num >= Byte.MIN_VALUE && num <= Byte.MAX_VALUE) {
			stream.write(OPCODE_BYTE);
			stream.write((byte) num);
		}
		else {
			stream.write(OPCODE_LONG);
			IoUtils.writeNumberBinary(stream, num, Long.SIZE);
		}
	}

	private static void processSwitch(final String token, final OutputStream stream) throws IOException {
		final String caseCountStr = token.substring(TOKEN_SWITCH.length());
		final int caseCount = Integer.parseInt(caseCountStr);
		
		stream.write(OPCODE_SWITCH);
		stream.write((byte) caseCount);
	}

	public InputStream getStream() {
		return new ByteArrayInputStream(data);
	}
}
