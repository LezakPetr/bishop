package bishopTests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import parallel.Parallel;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.tablebase.BothColorPositionResultSource;
import bishop.tablebase.IStagedTable;
import bishop.tablebase.ITable;
import bishop.tablebase.ITableRead;
import bishop.tablebase.TableCalculator;
import bishop.tablebase.TableReader;
import bishop.tablebase.TableSwitch;
import bishop.tablebase.TableValidator;
import bishop.tablebase.TableWriter;

public class TablebaseTest {

	private static final String[] MATERIAL_HASHES = {"00000-00000", "10000-00000", "01000-00000", "00100-00000", "00010-00000", "00001-00000", "20000-00000"};
	
	private void testWithUsePersistentTable (final boolean usePersistentTable, final boolean useCompressedTable) throws Exception {
		final long t1 = System.currentTimeMillis();
		
		final TableSwitch tableSwitch = new TableSwitch();
		final Parallel parallel = new Parallel();
		
		final Map<MaterialHash, ITableRead> subTables = new HashMap<>();

		for (String definition: MATERIAL_HASHES) {
			final MaterialHash materialHash = new MaterialHash(definition, Color.WHITE);
			final MaterialHash[] materialHashArray = materialHash.getBothSideHashes();
			final TableCalculator calculator = new TableCalculator(materialHashArray, parallel);
			
			calculator.setUsePersistentTable(usePersistentTable);
			calculator.setUseCompressedTable(useCompressedTable);
			
			for (Map.Entry<MaterialHash, ITableRead> entry: subTables.entrySet())
				calculator.addSubTable(entry.getKey(), entry.getValue());
			
			calculator.calculate();
			
			final BothColorPositionResultSource<IStagedTable> bothTables = new BothColorPositionResultSource<>();
			calculator.assignTablesTo(bothTables);
			
			final BothColorPositionResultSource<ITable> bothTablesRead = new BothColorPositionResultSource<ITable>();
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final IStagedTable table = bothTables.getBaseSource(color);
				table.switchToModeRead(parallel);
				
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
			
			final TableValidator validator = new TableValidator(tableSwitch, parallel);
			validator.setTable(bothTablesRead);
			
			for (Map.Entry<MaterialHash, ITableRead> entry: subTables.entrySet())
				validator.addSubTable(entry.getKey(), entry.getValue());

			Assert.assertTrue("Table is invalid", validator.validateTable());
			
			for (int color = Color.FIRST; color < Color.LAST; color++)
				subTables.put(materialHashArray[color], bothTablesRead.getBaseSource(color));
		}
		
		final long t2 = System.currentTimeMillis();
		System.out.println ("TablebaseTest: "  + (t2 - t1) + "ms");
		
		parallel.shutdown();
	}
	
	@Test
	public void testTablebaseWithPersistentTable() throws Exception {
		testWithUsePersistentTable(true, false);
	}
	
	@Test
	public void testTablebaseWithFullMemoryTable() throws Exception {
		testWithUsePersistentTable(false, false);
	}

	@Test
	public void testTablebaseWithCompressedMemoryTable() throws Exception {
		testWithUsePersistentTable(false, true);
	}

}
