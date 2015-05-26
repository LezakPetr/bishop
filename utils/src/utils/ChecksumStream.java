package utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

public class ChecksumStream extends OutputStream {
	
	private final Checksum checksum;
	
	
	public ChecksumStream(final Checksum checksum) {
		this.checksum = checksum;
	}
	
	@Override
	public void write(final int b) throws IOException {
		checksum.update(b);
	}

	@Override
	public void write(final byte[] data) throws IOException {
		checksum.update(data, 0, data.length);
	}

	@Override
	public void write(final byte[] data, final int offset, final int len) throws IOException {
		checksum.update(data, offset, len);
	}

}
