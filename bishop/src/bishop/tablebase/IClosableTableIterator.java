package bishop.tablebase;

import java.io.IOException;

public interface IClosableTableIterator extends ITableIterator, AutoCloseable {

	@Override
	public void close() throws IOException;
}
