package bishop.tablebaseGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.tablebase.BothColorPositionResultSource;
import bishop.tablebase.FileNameCalculator;
import bishop.tablebase.FilePositionResultSource;
import bishop.tablebase.IStagedTable;
import bishop.tablebase.ITable;
import bishop.tablebase.ITableRead;
import bishop.tablebase.TableBlockCache;
import bishop.tablebase.TableCalculator;
import bishop.tablebase.TableReader;
import bishop.tablebase.TableSwitch;
import bishop.tablebase.TableValidator;
import bishop.tablebase.TableWriter;
import parallel.Parallel;

public class GenerateTablebase {
	
	private static final int CACHE_BITS = 16;
	
	private TableBlockCache blockCache;
	private TableSwitch resultSource;
	private BothColorPositionResultSource<ITable> bothTables;
	private String action;
	private String directory;
	private String definition;
	private boolean usePersistentTable;
	private boolean useCompressedTable;
	private Map<MaterialHash, ITableRead> subtableMap;
	private Parallel parallel;
	private TableCalculator calculator;
	private MaterialHash[] materialHashArray;
	
	private void calculateTable() throws Exception {
		for (Entry<MaterialHash, ITableRead> entry: subtableMap.entrySet()) {
			calculator.addSubTable (entry.getKey(), entry.getValue());
		}

		calculator.setUsePersistentTable(usePersistentTable);
		calculator.setUseCompressedTable(useCompressedTable);
		calculator.calculate();
		
		bothTables = new BothColorPositionResultSource<>();
		calculator.assignTablesTo(bothTables);
	}

	private void readSubTables() throws IOException {
		resultSource = new TableSwitch();
		
		final MaterialHash materialHash = new MaterialHash(definition, Color.WHITE);
		materialHashArray = materialHash.getBothSideHashes();
		
		calculator = new TableCalculator(materialHashArray, parallel);
		
		final Collection<MaterialHash> neededSubtables = calculator.getNeededSubtables();
		subtableMap = new HashMap<MaterialHash, ITableRead>();
		
		for (MaterialHash neededMaterialHash: neededSubtables) {
			final ITableRead table = createResultSource(neededMaterialHash);
			subtableMap.put(neededMaterialHash, table);
		}
	}

	private String getFileName(final MaterialHash materialHash) {
		return FileNameCalculator.getAbsolutePath(directory, materialHash);
	}

	private void writeTable() throws IOException, InterruptedException, ExecutionException {
		System.out.println("Writing table");
		
		final TableWriter writer = new TableWriter();
		
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final FileOutputStream stream = new FileOutputStream(getFileName(materialHashArray[onTurn]));
			
			try {
				final IStagedTable table = (IStagedTable) bothTables.getBaseSource(onTurn);
				table.switchToModeRead(parallel);

				writer.writeTable(table, stream);
			}
			finally {
				stream.close();
			}
		}
		
		System.out.println("Table written");
	}

	private void readTable() throws IOException {
		System.out.println("Reading table");
		
		bothTables.clear();
		
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final ITable table = readOneTable(materialHashArray[onTurn]);
			
			bothTables.setBaseSource(onTurn, table);
		}
		
		System.out.println("Table read");
	}

	private ITable readOneTable(final MaterialHash materialHash) throws IOException {
		final File file = new File(getFileName(materialHash));
		final TableReader reader = new TableReader(file);
		reader.readTable();
		
		return reader.getTable();
	}

	private ITableRead createResultSource(final MaterialHash materialHash) throws IOException {
		final File file = new File(getFileName(materialHash));

		return new FilePositionResultSource(file, blockCache);
	}

	private void validateTable() throws Exception {
		final TableValidator validator = new TableValidator(resultSource, parallel);
		
		validator.setTable(bothTables);
		
		for (Entry<MaterialHash, ITableRead> entry: subtableMap.entrySet()) {
			validator.addSubTable (entry.getKey(), entry.getValue());
		}

		if (validator.validateTable()) {
			System.out.println ("Table is correct");
		}
		else {
			System.out.println ("Table is WRONG!!!");
		}
	}
	
	private void doGeneration(final String[] args) throws Exception {
		if (args.length != 3 && args.length != 4) {
			System.err.println("GenerateTablebase action tableType directory definition");
			throw new RuntimeException("Wrong parameters");
		}
		
		action = args[0];
		directory = args[1];
		definition = args[2];
		
		if (args.length == 4) {
			if (args[3].contains("p"))
				usePersistentTable = true;

			if (args[3].contains("c"))
				useCompressedTable = true;
		}

		parallel = new Parallel();

		System.out.println (parallel.getThreadCount() + " threads");
		
		blockCache = new TableBlockCache(CACHE_BITS);
		readSubTables();
		
		if (action.contains("g")) {
			calculateTable();
			writeTable();
		}
		
		if (action.contains("v")) {
			bothTables = new BothColorPositionResultSource<ITable>();
			
			readTable();
			validateTable();
		}
		
		parallel.shutdown();
	}
	
	public static void main (final String[] args) throws Exception {
		final GenerateTablebase generate = new GenerateTablebase();
		generate.doGeneration(args);
	}
}
