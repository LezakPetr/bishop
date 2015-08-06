package bishopTests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.tablebase.BothColorPositionResultSource;
import bishop.tablebase.FileNameCalculator;
import bishop.tablebase.ITable;
import bishop.tablebase.MemoryTable;
import bishop.tablebase.PersistentTable;
import bishop.tablebase.TableCalculator;
import bishop.tablebase.TableReader;
import bishop.tablebase.TableSwitch;
import bishop.tablebase.TableValidator;
import bishop.tablebase.TableWriter;

public class TablebaseTest {

	private static final String[] MATERIAL_HASHES = {"00000-00000", "10000-00000", "01000-00000", "00100-00000", "00010-00000", "00001-00000"};
	
	@Test
	public void testTablebase() throws Exception {
		final long t1 = System.currentTimeMillis();
		
		final int threadCount = Runtime.getRuntime().availableProcessors();
		final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		final TableSwitch tableSwitch = new TableSwitch();

		for (String definition: MATERIAL_HASHES) {
			final MaterialHash materialHash = new MaterialHash(definition, Color.WHITE);
			final MaterialHash[] materialHashArray = materialHash.getBothSideHashes();
			final TableCalculator calculator = new TableCalculator(materialHashArray, executor, threadCount);
			
			for (MaterialHash subHash: tableSwitch.getMaterialHashSet()) {
				calculator.addSubTable(subHash, tableSwitch.getTable(subHash));
			}
			
			calculator.calculate();
			
			final BothColorPositionResultSource<PersistentTable> bothTables = calculator.getTable();
			final BothColorPositionResultSource<ITable> bothTablesRead = new BothColorPositionResultSource<ITable>();
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final PersistentTable table = bothTables.getBaseSource(color);
				table.switchToModeRead();
				
				final TableWriter writer = new TableWriter();
				final File tmpFile = File.createTempFile("TablebaseTest", ".tbbs");
				
				try {
					writer.writeTable(table, tmpFile);
					
					final TableReader reader = new TableReader(tmpFile);
					reader.readTable();
					
					final ITable readTable = reader.getTable();
					bothTablesRead.setBaseSource(color, readTable);
				}
				finally {
					tmpFile.delete();
				}
			}
			
			final TableValidator validator = new TableValidator(tableSwitch, executor, threadCount);
			validator.setTable(bothTablesRead);
			
			Assert.assertTrue("Table is invalid", validator.validateTable());

			for (int color = Color.FIRST; color < Color.LAST; color++) {
				tableSwitch.addTable(materialHashArray[color], bothTablesRead.getBaseSource(color));	
			}			
		}
		
		final long t2 = System.currentTimeMillis();
		System.out.println ("TablebaseTest: "  + (t2 - t1) + "ms");
	}
}
