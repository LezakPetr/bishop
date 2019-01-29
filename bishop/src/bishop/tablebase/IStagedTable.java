package bishop.tablebase;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import parallel.Parallel;

/**
 * Table that can be in read and write mode.
 * The table supports random access read in READ mode thru methods of ITable and
 * parallel sequential writing in WRITE mode thru iterators returned by method getOutputPage.
 * @author Ing. Petr Ležák
 */
public interface IStagedTable extends ITable {
	/**
	 * Returns iterator for next page.
	 * Pages can be written in parallel.
	 * Iterator has to be closed.
	 * @return iterator for next page
	 * @throws IOException
	 */
	public IClosableTableIterator getOutputPage() throws IOException;

	/**
	 * Clears the table.
	 */
	public void clear();

	/**
	 * Switches table to write mode and initializes output page queue.
	 */
	public void switchToModeWrite();

	/**
	 * Switches table to read mode.
	 * @param parallel parallel utility object
	 */
	public void switchToModeRead(final Parallel parallel) throws InterruptedException, ExecutionException;

}
