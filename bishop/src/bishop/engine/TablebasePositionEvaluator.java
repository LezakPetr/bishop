package bishop.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import bishop.base.MaterialHash;
import bishop.base.Position;
import bishop.tablebase.FileNameCalculator;
import bishop.tablebase.ITableRead;
import bishop.tablebase.LazyFilePositionResultSource;
import bishop.tablebase.TableBlockCache;
import bishop.tablebase.TableResult;
import bishop.tablebase.TableSwitch;
import bishop.tablebase.TablebaseFileNameFilter;

public class TablebasePositionEvaluator {

	private final File directory;
	private final TableSwitch tableSwitch;
	private final TableBlockCache blockCache;
	
	public TablebasePositionEvaluator(final File directory) {
		this.directory = directory;
		this.tableSwitch = new TableSwitch();
		this.blockCache = new TableBlockCache(16);
		
		if (directory != null && directory.exists()) {
			scanDirectory();
		}
	}

	private void scanDirectory() {
		final File[] files = directory.listFiles(new TablebaseFileNameFilter());
		final Map<MaterialHash, ITableRead> tableMap = new HashMap<>();
		
		for (File file: files) {
			final MaterialHash materialHash = FileNameCalculator.parseFileName(file.getName());
			final ITableRead table = new LazyFilePositionResultSource(file, blockCache);
			
			tableMap.put(materialHash, table);
		}
		
		tableSwitch.setTables(tableMap);
	}
	
	/**
	 * Returns relative evaluation of given position.
	 * @param position position to evaluate
	 * @param depth actual depth
	 * @return relative evaluation
	 */
	public int evaluatePosition(final Position position, final int depth) {
		final int result = tableSwitch.getPositionResultIfPossible(position);
		
		switch (result) {
			case TableResult.UNKNOWN_MATERIAL:
				return Evaluation.UNKNOWN;
				
			case TableResult.ILLEGAL:
				return Evaluation.MAX;
				
			default:
				if (TableResult.isWin(result)) {
					final int mateDepth = TableResult.getWinDepth(result);
					final int totalDepth = 2*mateDepth + depth + 1;
					
					return Evaluation.getMateEvaluation(totalDepth);
				}
				
				if (TableResult.isLose(result)) {
					final int mateDepth = TableResult.getLoseDepth(result);
					final int totalDepth = 2*mateDepth + depth;
					
					return -Evaluation.getMateEvaluation(totalDepth);
				}
				
				return Evaluation.DRAW;
		}
	}
	
	public Set<MaterialHash> getMaterialHashSet() {
		return tableSwitch.getMaterialHashSet();
	}

	public ITableRead getTable(final MaterialHash materialHash) {
		return tableSwitch.getTable (materialHash);
	}
}
