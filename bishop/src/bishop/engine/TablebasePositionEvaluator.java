package bishop.engine;

import bishop.base.IMaterialHashRead;
import bishop.base.MaterialHash;
import bishop.base.Position;
import bishop.tablebase.ITableRead;
import bishop.tablebase.TableResult;
import bishop.tablebase.TableSwitch;

import java.io.File;
import java.util.Set;

public class TablebasePositionEvaluator {

	private final TableSwitch tableSwitch;
	
	public TablebasePositionEvaluator(final File directory) {
		this.tableSwitch = new TableSwitch(directory);
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
	
	public Set<IMaterialHashRead> getMaterialHashSet() {
		return tableSwitch.getMaterialHashSet();
	}

	public ITableRead getTable(final MaterialHash materialHash) {
		return tableSwitch.getTable (materialHash);
	}

	public boolean canEvaluateMaterial(final IMaterialHashRead materialHash) {
		return tableSwitch.canProcessSource(materialHash);
	}
}
