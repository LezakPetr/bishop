package bishop.tablebaseGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import bishop.tablebase.ITable;
import bishop.tablebase.TableReader;
import bishop.tablebase.TableStatistics;
import bishop.tablebase.TableWriter;

public class TablebaseOptimizer {
	
	private static File path;
	private static ITable table;
	
	
	public static void main (final String args[]) {
		if (args.length != 1) {
			System.out.println ("Arguments: path");
		}
		
		final String pathStr = args[0];
		path = new File (pathStr);
		
		System.out.println ("Original size: " + path.length());
		
		try {
			compressTable();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	private static void compressTable() throws IOException {
		readTable();
		writeTable();
	}

	private static void readTable() throws IOException {
		final TableReader tableReader = new TableReader(path);
		tableReader.readTable();
		
		table = tableReader.getTable();
	}

	private static void writeTable() throws IOException {
		final TableWriter tableWriter = new TableWriter();
		tableWriter.writeTable(table, path);
	}

}
