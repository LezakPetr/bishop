package bishop.tablebase;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import utils.IoUtils;

public class OutputFileTableIterator extends TableIteratorBase implements ITableIterator, AutoCloseable {

	private final InputFileTableIterator inputIterator;
	private final OutputStream stream;
	private int result;
	
	public OutputFileTableIterator(final InputFileTableIterator inputIterator, final String path, final TableDefinition tableDefinition, final long beginIndex) throws FileNotFoundException {
		super(tableDefinition, beginIndex);
		
		this.stream = new BufferedOutputStream(new FileOutputStream(path));
		this.inputIterator = inputIterator;
		
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
			IoUtils.writeNumberBinary(stream, result, 2);
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot write result", ex);
		}
	}

	@Override
	public void moveForward(final long count) {
		for (long i = 0; i < count; i++)
			next();
	}

	public void close() throws IOException {
		while (isValid())
			next();
		
		writeResult();
		
		stream.close();
		
		if (inputIterator != null)
			inputIterator.close();
	}
}
