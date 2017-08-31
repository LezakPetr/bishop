package bishop.tablebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import parallel.Parallel;

import bishop.base.Color;
import bishop.base.MaterialHash;

public class TableValidator {
	
	private final Parallel parallel;
	private final TableSwitch resultSource;
	private final Map<MaterialHash, ITableRead> subTables = new HashMap<>();
	private BothColorPositionResultSource<? extends ITable> bothTables;
	
	
	public TableValidator(final TableSwitch resultSource, final Parallel parallel) {
		this.parallel = parallel;
		this.resultSource = resultSource;
	}
	
	
	public void setTable (final BothColorPositionResultSource<? extends ITable> bothTables) {
		this.bothTables = bothTables;
	}
	
	private boolean checkTable() throws Exception {
		final List<ValidationTaskProcessor> processorList = new ArrayList<ValidationTaskProcessor>();
		
		for (int i = 0; i < parallel.getThreadCount(); i++) {
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
		
		final int processorCount = processorList.size();
		
		for (int i = 0; i < processorCount; i++) {
			final ValidationTaskProcessor processor = processorList.get(i);
			final long nextIndex = (i + 1) * itemCount / processorCount;
			final long count = nextIndex - prevIndex;
			
			processor.initialize(table.getDefinition(), it, count);
			
			it.moveForward(count);
			prevIndex = nextIndex;
		}
		
		if (it.isValid())
			throw new RuntimeException("Not all items was validated");
		
		// Execute
		final List<Future<Boolean>> futureList = parallel.getExecutor().invokeAll(processorList);
		
		for (Future<Boolean> future: futureList) {
			if (!future.get())
				return false;
		}
		
		return true;
	}
	
	public boolean validateTable() throws Exception {
		final Map<MaterialHash, ITableRead> allTables = new HashMap<>(subTables);

		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final ITable table = bothTables.getBaseSource(onTurn);
			final TableDefinition definiton = table.getDefinition();
			final MaterialHash materialHash = definiton.getMaterialHash();
			
			allTables.put(materialHash, table);
		}
		
		resultSource.setTables(allTables);
		
		return checkTable();
	}

	public void addSubTable(final MaterialHash materialHash, final ITableRead table) {
		subTables.put(materialHash, table);
	}
}
