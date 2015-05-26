package bishop.tablebase;

import java.io.File;
import java.io.FilenameFilter;

public class TablebaseFileNameFilter implements FilenameFilter {
	@Override
	public boolean accept(final File dir, final String name) {
		return FileNameCalculator.isCorrectFileName(name);
	}

}
