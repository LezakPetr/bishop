package bishop.engine;

import java.io.File;
import java.util.Set;

import bishop.base.MaterialHash;
import bishop.base.Position;
import bishop.tablebase.FileNameCalculator;
import bishop.tablebase.FilePositionResultSource;
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
		this.blockCache = new TableBlockCache(8192);
		
		if (directory != null && directory.exists()) {
			scanDirectory();
		}
	}

	private void scanDirectory() {
		final File[] files = directory.listFiles(new TablebaseFileNameFilter());
		
		for (File file: files) {
			final MaterialHash materialHash = FileNameCalculator.parseFileName(file.getName());
			final ITableRead table = new LazyFilePositionResultSource(file, blockCache);
			
			tableSwitch.addTable(materialHash, table);
		}
	}
	
	public boolean canEvaluate (final MaterialHash materialHash) {
		return tableSwitch.canProcessSource(materialHash);
	}
	
	public int evaluatePosition(final Position position, final int depth) {
		final int result = tableSwitch.getPositionResult(position);
		int evaluation = Evaluation.DRAW;
		
		if (result == TableResult.ILLEGAL) {
			evaluation = Evaluation.MAX;
		}
		else {
			if (TableResult.isWin(result)) {
				final int mateDepth = TableResult.getWinDepth(result);
				final int totalDepth =  2*mateDepth + depth + 1;
				
				evaluation = Evaluation.getMateEvaluation(totalDepth);
			}
			
			if (TableResult.isLose(result)) {
				final int mateDepth = TableResult.getLoseDepth(result);
				final int totalDepth =  2*mateDepth + depth;
				
				evaluation = -Evaluation.getMateEvaluation(totalDepth);
			}
		}
		
		return Evaluation.getAbsolute(evaluation, position.getOnTurn());
	}
	
	public Set<MaterialHash> getMaterialHashSet() {
		return tableSwitch.getMaterialHashSet();
	}

	public ITableRead getTable(final MaterialHash materialHash) {
		return tableSwitch.getTable (materialHash);
	}
}
