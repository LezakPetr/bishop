package utils;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends InputStream {
	
	private final InputStream baseStream;
	private long position;

	public CountingInputStream(final InputStream baseStream) {
		this.baseStream = baseStream;
		this.position = 0;
	}
	
	public long getPosition() {
		return position;
	}
	
	@Override
	public int available() throws IOException {
		return baseStream.available();
	}
	
	@Override
	public void close() throws IOException {
		baseStream.close();
	}
	
	@Override
	public int read() throws IOException {
		final int ret = baseStream.read();
		
		if (ret >= 0)
			position++;
		
		return ret;
	}

	@Override
	public int read(final byte[] b) throws IOException {
		final int ret = baseStream.read(b);
		
		if (ret >= 0)
			position += ret;
		
		return ret;
	}

	@Override
	public int read(final byte[] b, final int offset, final int len) throws IOException {
		final int ret = baseStream.read(b, offset, len);
		
		if (ret >= 0)
			position += ret;
		
		return ret;
	}

	@Override
	public long skip(final long n) throws IOException {
		final long ret = baseStream.skip(n);
		position += ret;
		
		return ret;
	}

}
