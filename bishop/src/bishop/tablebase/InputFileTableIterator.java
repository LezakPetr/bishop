package bishop.tablebase;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import utils.IoUtils;

public class InputFileTableIterator extends TableIteratorBase implements AutoCloseable {

	private final String path;
	private final InputStream stream;
	private short result;
	private boolean initialized;
	
	public InputFileTableIterator (final String path, final TableDefinition tableDefinition, final long beginIndex) throws FileNotFoundException {
		super(tableDefinition, beginIndex);
		
		this.path = path;
		this.stream = new BufferedInputStream(new FileInputStream(path));
		this.initialized = true;
		
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
		if (isValid()) {
			try {
				result = (short) IoUtils.readNumberBinary(stream, 2);
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
			super.moveForward(count - 1);
			
			try {
				IoUtils.skip(stream, (count - 1) * 2);
			}
			catch (IOException ex) {
				throw new RuntimeException("Cannot read result");
			}
	
			next();
		}
		else
			super.moveForward(count);
	}

	public void close() throws IOException {
		stream.close();
	}
}
