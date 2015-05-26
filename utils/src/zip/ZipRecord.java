package zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public class ZipRecord {
	private final ZipEntry entry;
	private final byte[] data;
	
	public ZipRecord (final ZipEntry entry, final ByteArrayOutputStream stream) {
		this.entry = entry;
		this.data = stream.toByteArray();
	}

	public ZipEntry getEntry() {
		return entry;
	}

	/**
	 * Returns stream with data. Stream must be closed.
	 * @return stream
	 */
	public InputStream getStream() {
		return new ByteArrayInputStream(data);
	}

}
