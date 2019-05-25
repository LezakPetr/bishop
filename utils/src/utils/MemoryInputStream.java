package utils;

import java.io.InputStream;

public class MemoryInputStream extends InputStream {
	private final byte[] buffer;
	private int position;
	private int end;

	public MemoryInputStream (final byte[] buffer) {
		this.buffer = buffer;
		this.position = 0;
		this.end = buffer.length;
	}

	public MemoryInputStream (final byte[] buffer, final int offset, final int size) {
		this.buffer = buffer;
		this.position = offset;
		this.end = offset + size;
	}

	@Override
	public int read() {
		if (position >= end)
			return -1;

		final int result = buffer[position] & 0xFF;
		position++;

		return result;
	}

	@Override
	public int available() {
		return buffer.length - position;
	}
}
