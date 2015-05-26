package bishop.tablebase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import bishop.base.Color;
import bishop.base.MaterialHash;

public class TableValidator {
	
	private final ExecutorService executor;
	private final int threadCount;
	private final TableSwitch resultSource;
	private BothColorPositionResultSource<? extends ITable> bothTables;
	
	
	public TableValidator(final TableSwitch resultSource, final ExecutorService executor, final int threadCount) {
		this.executor = executor;
		this.resultSource = resultSource;
		this.threadCount = threadCount;
	}
	
	
	public void setTable (final BothColorPositionResultSource<? extends ITable> bothTables) {
		this.bothTables = bothTables;
	}
	
	private boolean checkTable() throws Exception {
		final List<ValidationTaskProcessor> processorList = new ArrayList<ValidationTaskProcessor>();
		
		for (int i = 0; i < threadCount; i++) {
			processorList.add (new ValidationTaskProcessor(resultSource));
		}
		
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			if (!checkTablePart(processorList, onTurn))
				return false;
		}
		
		return true;
	}


	private boolean checkTablePart(final List<ValidationTaskProcessor> processorList, final int onTurn) throws Exception {
		final ITable table = bothTables.getBaseSource(onTurn);
		
		// Initialize
		final ITableIteratorRead it = new TableIteratorReadImpl(table, 0);
		long prevIndex = 0;
		
		final long itemCount = it.getPositionCount();
		System.out.println ("Validating " + itemCount + " positions");
		
		for (int i = 0; i < threadCount; i++) {
			final ValidationTaskProcessor processor = processorList.get(i);
			final long nextIndex = (i + 1) * itemCount / threadCount;
			final long count = nextIndex - prevIndex;
			
			processor.initialize(table.getDefinition(), it, count);
			
			it.moveForward(count);
			prevIndex = nextIndex;
		}
		
		if (it.isValid())
			throw new RuntimeException("Not all items was validated");
		
		// Execute
		final List<Future<Boolean>> futureList = executor.invokeAll(processorList);
		
		for (Future<Boolean> future: futureList) {
			if (!future.get())
				return false;
		}
		
		return true;
	}
	
	public boolean validateTable() throws Exception {
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final ITable table = bothTables.getBaseSource(onTurn);
			final TableDefinition definiton = table.getDefinition();
			final MaterialHash materialHash = definiton.getMaterialHash();
			
			resultSource.addTable(materialHash, table);
		}
		
		try {
			return checkTable();
		}
		finally {
			for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
				final ITable table = bothTables.getBaseSource(onTurn);
				final TableDefinition definiton = table.getDefinition();
				final MaterialHash materialHash = definiton.getMaterialHash();
				
				resultSource.removeSource(materialHash);
			}
		}
	}

	public void addSubTable(final MaterialHash materialHash, final ITableRead table) {
		resultSource.addTable(materialHash, table);
	}
}
