package bishop.tablebase;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import parallel.Parallel;

public interface IStagedTable extends ITable {
	
	public IClosableTableIterator getOutputBlock() throws IOException;

	public void clear();

	public void switchToModeWrite();

	public void moveOutputToInput();

	public void switchToModeRead(Parallel parallel) throws IOException, InterruptedException, ExecutionException;

}
