package bishop.tablebaseGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.tablebase.BothColorPositionResultSource;
import bishop.tablebase.FileNameCalculator;
import bishop.tablebase.FilePositionResultSource;
import bishop.tablebase.IPositionResultSource;
import bishop.tablebase.ITable;
import bishop.tablebase.ITableRead;
import bishop.tablebase.MemoryTable;
import bishop.tablebase.TableBlockCache;
import bishop.tablebase.TableCalculator;
import bishop.tablebase.TableDefinition;
import bishop.tablebase.TableReader;
import bishop.tablebase.TableSwitch;
import bishop.tablebase.TableValidator;
import bishop.tablebase.TableWriter;

public class GenerateTablebase {
	
	private static final int CACHE_SIZE = 20000;
	
	private TableBlockCache blockCache;
	private TableSwitch resultSource;
	private BothColorPositionResultSource<ITable> bothTables;
	private String action;
	private String directory;
	private String definition;
	private Map<MaterialHash, ITableRead> subtableMap;
	private ExecutorService executor;
	private int threadCount;
	private TableCalculator calculator;
	private MaterialHash[] materialHashArray;
	
	private void calculateTable() throws Exception {
		for (Entry<MaterialHash, ITableRead> entry: subtableMap.entrySet()) {
			calculator.addSubTable (entry.getKey(), entry.getValue());
		}

		calculator.calculate();
		
		bothTables = calculator.<ITable>getTable();
	}

	private void readSubTables() throws IOException {
		resultSource = new TableSwitch();
		
		final MaterialHash materialHash = new MaterialHash(definition, Color.WHITE);
		materialHashArray = materialHash.getBothSideHashes();
		
		calculator = new TableCalculator(materialHashArray, executor, threadCount);
		
		final List<MaterialHash> neededSubtables = calculator.getNeededSubtables();
		subtableMap = new HashMap<MaterialHash, ITableRead>();
		
		for (MaterialHash neededMaterialHash: neededSubtables) {
			final ITableRead table = createResultSource(neededMaterialHash);
			subtableMap.put(neededMaterialHash, table);
		}
	}
	
	private String getFileName(final MaterialHash materialHash) {
		final String fileName = FileNameCalculator.getFileName(materialHash);
		final java.io.File file = new java.io.File (directory, fileName);
		
		return file.getAbsolutePath();
	}
	
	private void writeTable() throws IOException {
		System.out.println("Writing table");
		
		final TableWriter writer = new TableWriter();
		
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			final FileOutputStream stream = new FileOutputStream(getFileName(materialHashArray[onTurn]));
			
			try {
				writer.writeTable(bothTables.getBaseSource(onTurn), stream);
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
		final TableValidator validator = new TableValidator(resultSource, executor, threadCount);
		
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
		if (args.length != 3) {
			System.err.println("GenerateTablebase action directory definition");
			throw new RuntimeException("Wrong parameters");
		}
		
		action = args[0];
		directory = args[1];
		definition = args[2];
		threadCount = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool(threadCount);

		System.out.println (threadCount + " threads");
		
		blockCache = new TableBlockCache(CACHE_SIZE);
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
		
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);
	}
	
	public static void main (final String[] args) throws Exception {
		final GenerateTablebase generate = new GenerateTablebase();
		generate.doGeneration(args);
	}
}
