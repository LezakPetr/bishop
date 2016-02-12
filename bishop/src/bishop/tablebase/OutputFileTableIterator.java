package bishop.tablebase;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import utils.IoUtils;
import utils.ShortRingBuffer;

public class OutputFileTableIterator extends TableIteratorBase implements IClosableTableIterator {

	private final InputFileTableIterator inputIterator;
	private final OutputStream stream;
	private int result;
	private final ShortRingBuffer buffer;
	private final FileTableIteratorChecksum checksum;
	private final Set<Short> resultSet;
	private long remainingResults; 
	
	public OutputFileTableIterator(final InputFileTableIterator inputIterator, final String path, final TableDefinition tableDefinition, final long beginIndex, final long size) throws FileNotFoundException {
		super(tableDefinition, beginIndex);
		
		this.buffer = new ShortRingBuffer(8);
		this.stream = new BufferedOutputStream(new FileOutputStream(path));
		this.inputIterator = inputIterator;
		this.resultSet = new HashSet<>();
		this.checksum = new FileTableIteratorChecksum();
		this.remainingResults = size;
		
		readNextResult();
	}

	@Override
	public void setResult(final int result) {
		this.result = result;
	}
	
	@Override
	public int getResult() {
		return result;
	}

	@Override
	public OutputFileTableIterator copy() {
		throw new RuntimeException("Method OutputStreamTableIterator.copy is not implemented");
	}

	@Override
	public void next() {
		writeResult();

		super.next();
		
		readNextResult();
	}

	private void readNextResult() {
		if (inputIterator != null && inputIterator.isValid()) {
			result = inputIterator.getResult();
			inputIterator.next();
		}
		else {
			result = TableResult.ILLEGAL;
		}
	}

	private void writeResult() {
		try {
			if (remainingResults > 0) {
				buffer.push((short) result);
				checksum.addResult(result);
				resultSet.add((short) result);
				remainingResults--;
				
				if (buffer.getSize() >= buffer.getCapacity())
					writeSequence();
			}
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot write result", ex);
		}
	}

	private void flushBuffer() throws IOException {
		while (buffer.getSize() > 0)
			writeSequence();
	}
	
	private void writeSequence() throws IOException {
		final int size = buffer.getSize();
		
		if (size <= 0)
			return;
		
		final int firstResult = buffer.getAt(0);
		final FileTableIteratorMode mode;
		
		switch (firstResult) {
			case TableResult.DRAW:
				mode = FileTableIteratorMode.DRAW;
				break;
				
			case TableResult.ILLEGAL:
				mode = FileTableIteratorMode.ILLEGAL;
				break;
				
			default:
				if (TableResult.canBeCompressed(firstResult))
					mode = FileTableIteratorMode.COMPRESSED;
				else
					mode = FileTableIteratorMode.FULL;
				break;
		}
		
		mode.write(buffer, stream);
	}

	@Override
	public void moveForward(final long count) {
		for (long i = 0; i < count; i++)
			next();
	}

	public void close() throws IOException {
		while (remainingResults > 0)
			next();
		
		writeResult();
		flushBuffer();
		
		checksum.writeCrcToStream(stream);
		IoUtils.writeNumberBinary(stream, resultSet.size(), 2);
		
		stream.close();
		
		if (inputIterator != null)
			inputIterator.close();
	}
}
