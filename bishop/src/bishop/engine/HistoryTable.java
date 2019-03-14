package bishop.engine;

import java.util.Arrays;

import bishop.base.Color;
import bishop.base.Move;
import bishop.base.PieceType;
import bishop.base.Square;

/**
 * Table that holds weighted number of cutoffs per color, moving piece type and target square.
 *  
 * @author Ing. Petr Ležák
 */
public class HistoryTable {
	private static final double MAX_EVALUATION = 1.0;
	
	private final long[] historyTable = new long[Color.LAST * PieceType.LAST * Square.LAST];
	private long maxEntry;
	private double coeff;
	
	public void addCutoff(final int color, final Move move, final int horizon) {
		if (horizon > 0) {
			final int historyTableIndex = getHistoryTableIndex(color, move);
			final long oldEntry = historyTable[historyTableIndex];
			final long newEntry = oldEntry + horizon * horizon;
			historyTable[historyTableIndex] = newEntry;
			
			if (newEntry > maxEntry) {
				maxEntry = newEntry;
				recalculateCoeff();
			}
		}
	}
	
	public void recalculateCoeff() {
		if (maxEntry > 0)
			coeff = MAX_EVALUATION / (double) maxEntry;
	}
	
	public double getEvaluation(final int color, final Move move) {
		final int historyTableIndex = getHistoryTableIndex(color, move);
		final long entry = historyTable[historyTableIndex];
		
		return entry * coeff;
	}
	
	private static int getHistoryTableIndex (final int color, final Move move) {
		return move.getTargetSquare() +
		       (color << Square.BIT_COUNT) +
		       (move.getMovingPieceType() << (Square.BIT_COUNT + Color.BIT_COUNT));
	}
	
	public void clear() {
		Arrays.fill(historyTable, 0);
		maxEntry = 0;
		coeff = 1.0;
	}
	
}
