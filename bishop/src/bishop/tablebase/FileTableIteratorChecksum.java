package bishop.tablebase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

import utils.ChecksumStream;
import utils.IoUtils;

public class FileTableIteratorChecksum {
	
	private static final int CRC_SIZE = 4;
	
	private final CRC32 crc;
	private final ChecksumStream checksumStream;
	
	public FileTableIteratorChecksum() {
		this.crc = new CRC32();
		this.checksumStream = new ChecksumStream(crc);
	}
	
	public void addResult (final int result) throws IOException {
		IoUtils.writeNumberBinary(checksumStream, result, 2);
	}
	
	public void writeCrcToStream(final OutputStream stream) throws IOException {
		checksumStream.close();
		IoUtils.writeNumberBinary(stream, crc.getValue(), CRC_SIZE);
	}
	
	public boolean validateCrcFromStream (final InputStream stream) throws IOException {
		final long crcVal = IoUtils.readUnsignedNumberBinary(stream, CRC_SIZE);
		checksumStream.close();
		
		return crcVal == crc.getValue();
	}
}
