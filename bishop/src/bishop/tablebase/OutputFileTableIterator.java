package bishop.tablebase;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;

import sun.misc.IOUtils;
import utils.ChecksumStream;
import utils.IoUtils;
import utils.ShortRingBuffer;

public class OutputFileTableIterator extends TableIteratorBase implements ITableIterator, AutoCloseable {

	private final String path;
	private final InputFileTableIterator inputIterator;
	private final OutputStream stream;
	private int result;
	private final ShortRingBuffer buffer;
	private final FileTableIteratorChecksum checksum;
	private final Set<Short> resultSet;
	private long remainingResults;
	
	public OutputFileTableIterator(final InputFileTableIterator inputIterator, final String path, final TableDefinition tableDefinition, final long beginIndex, final long size) throws FileNotFoundException {
		super(tableDefinition, beginIndex);
		
		this.path = path;
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
				
				flushBuffer();
			}
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot write result", ex);
		}
	}

	private void flushBuffer() throws IOException {
		while (writeSequence())
			;
	}
	
	private boolean writeSequence() throws IOException {
		final int size = buffer.getSize();
		
		if (size <= 0)
			return false;
		
		final int firstSymbol = buffer.getAt(0);
		final int lastSymbol = buffer.getAt(size - 1);
		
		if (firstSymbol == TableResult.DRAW) {
			if (lastSymbol != TableResult.DRAW || size >= FileTableIteratorMode.DRAW.getMaxCount()) {
				stream.write(FileTableIteratorMode.DRAW.getDescriptor(size - 1));
				buffer.pop(size - 1);
				
				return true;
			}
			else
				return false;
		}
		
		if (firstSymbol == TableResult.ILLEGAL) {
			if (lastSymbol != TableResult.ILLEGAL || size >= FileTableIteratorMode.ILLEGAL.getMaxCount()) {
				stream.write(FileTableIteratorMode.ILLEGAL.getDescriptor(size - 1));
				buffer.pop(size - 1);
				
				return true;
			}
			else
				return false;
		}
		
		if (TableResult.canBeCompressed(firstSymbol)) {
			if (!TableResult.canBeCompressed(lastSymbol) || size >= FileTableIteratorMode.COMPRESSED.getMaxCount()) {
				writeCompressedSequence(size - 1);
				
				return true;
			}
			
			if (size >= 3) {
				final short preLastSymbol = buffer.getAt(size - 2);
				
				if ((lastSymbol == TableResult.ILLEGAL || lastSymbol == TableResult.DRAW) && preLastSymbol == lastSymbol) {
					writeCompressedSequence(size-2);
					
					return true;
				}
			}
			
			return false;
		}
		else {
			writeFullSequence(1);
			
			return true;
		}
	}

	private void writeCompressedSequence(final int size) throws IOException {
		stream.write(FileTableIteratorMode.COMPRESSED.getDescriptor(size));
		
		for (int i = 0; i < size; i++)
			stream.write(TableResult.compress(buffer.getAt(i)));
		
		buffer.pop(size);
	}

	private void writeFullSequence(final int size) throws IOException {
		for (int i = 0; i < size; i++)
			IoUtils.writeNumberBinary(stream, buffer.getAt(i) & 0x3FFF, 2);
		
		buffer.pop(size);
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
		
		if (buffer.getSize() > 0)
			writeFullSequence(buffer.getSize());
		
		checksum.writeCrcToStream(stream);
		IoUtils.writeNumberBinary(stream, resultSet.size(), 2);
		
		stream.close();
		
		if (inputIterator != null)
			inputIterator.close();
	}
}
