package bishop.engine;

import java.io.PrintWriter;

import bishop.base.Position;

/**
 * Position evaluator which evaluates the position.
 * The evaluation is done in these steps:
 * - first the evaluateTactical has to be called
 * - then optionally in any order:
 *   - the evaluatePositional can be called at most once; the attackCalculator must be the same as for evaluateTactical method
 *   - the getMaterialEvaluationShift can be called    
 * Returned position evaluations are valid until evaluateTactical is called again.
 * This order is important because evaluateTactical can pre-calculate some stuff that evaluatePositional and getMaterialEvaluationShift needs
 * and also clears the evaluations.
 *  
 * @author Ing. Petr Ležák
 */
public interface IPositionEvaluator {
	
	/**
	 * Returns tactical evaluation of given position.
	 * @param position position to evaluate
	 * @param attackCalculator calculator of attacks, it will be filled by current position
	 * @param mobilityCalculator
	 * @return evaluation from view of white side
	 */
	public IPositionEvaluation evaluateTactical(final Position position, final AttackCalculator attackCalculator, final MobilityCalculator mobilityCalculator);

	/**
	 * Returns positional evaluation of given position.
	 * @param attackCalculator already filled calculator of attacks
	 * @return evaluation from view of white side
	 */
	public IPositionEvaluation evaluatePositional (final AttackCalculator attackCalculator);

	/**
	 * Returns number of bits that the material evaluation should be shifted right. 
	 * @return material evaluation shift
	 */
	public default int getMaterialEvaluationShift() {
		return 0;
	}

	/**
	 * Writes information about the position into given writer.
	 * @param writer target writer
	 */
	public void writeLog (final PrintWriter writer);

}
