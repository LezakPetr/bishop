package bishop.tablebase;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;

import utils.IoUtils;

public class InputFileTableIterator extends TableIteratorBase implements AutoCloseable {
	
	private final PushbackInputStream stream;
	private short result;
	private boolean initialized;
	private FileTableIteratorMode mode;
	private int remainingCount;
	private final FileTableIteratorChecksum checksum;
	private long remainingResults;
	
	public InputFileTableIterator (final String path, final TableDefinition tableDefinition, final long beginIndex, final long size) throws FileNotFoundException {
		super(tableDefinition, beginIndex);
		
		this.stream = new PushbackInputStream(new BufferedInputStream(new FileInputStream(path)));
		this.initialized = true;
		this.mode = null;
		this.remainingCount = 0;
		this.remainingResults = size;
		this.checksum = new FileTableIteratorChecksum();

		readResult();
	}
	
	@Override
	public int getResult() {
		return result;
	}

	@Override
	public ITableIteratorRead copy() {
		throw new RuntimeException("InputStreamTableIterator.copy not implemented");
	}
	
	@Override
	public void next() {
		super.next();
		
		if (initialized) {
			readResult();
		}
	}

	private void readResult() {
		if (isValid() && remainingResults > 0) {
			try {
				if (remainingCount <= 0) {
					final int descriptor = IoUtils.readByteBinary(stream) & 0xFF;
					mode = FileTableIteratorMode.forDescriptor (descriptor);
					
					if (mode == FileTableIteratorMode.FULL) {
						stream.unread(descriptor);
						remainingCount = 1;
					}
					else
						remainingCount = mode.getCount (descriptor);
				}
				
				result = mode.read (stream);
				
				remainingCount--;
				remainingResults--;
				checksum.addResult(result);
			}
			catch (IOException ex) {
				throw new RuntimeException("Cannot read result", ex);
			}
		}
		else
			result = TableResult.ILLEGAL;
	}

	@Override
	public void moveForward(final long count) {
		if (initialized) {
			for (int i = 0; i < count; i++)
				next();
		}
		else
			super.moveForward(count);
	}

	public void close() throws IOException {
		try {
			if (!checksum.validateCrcFromStream(stream))
				throw new RuntimeException("Corrupted results");
		}
		finally {
			stream.close();
		}
	}
}
