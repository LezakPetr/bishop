package bishop.engine;

import java.io.PrintWriter;

import parallel.Parallel;

import bishop.base.Position;

public interface IPositionEvaluator {
	/**
	 * Returns evaluation of given position.
	 * @param parallel parallel
	 * @param position position to evaluate
	 * @param alpha lower boundary from view of white side
	 * @param beta upper boundary from view of white side
	 * @param attackCalculator calculator of attacks, it will be filled by current position
	 * @return evaluation from view of white side
	 */
	public int evaluatePosition (final Parallel parallel, final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator);
		
	/**
	 * Writes information about the position into given writer.
	 * @param writer target writer
	 */
	public void writeLog (final PrintWriter writer);
}
