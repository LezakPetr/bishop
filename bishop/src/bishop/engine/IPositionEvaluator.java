package bishop.engine;

import java.io.PrintWriter;

import bishop.base.Position;

/**
 * Position evaluator which evaluates the position.
 * The evaluation is done in these steps:
 * - first the getEvaluation should be called to obtain evaluation object
 * - then the evaluation must be cleared by calling evaluation.clear()
 * - then the evaluate has to be called
 * - then the getMaterialEvaluationShift can be called    
 *  
 * @author Ing. Petr Ležák
 */
public interface IPositionEvaluator {
	
	/**
	 * Returns the object that obtains evaluation.
	 * @return evaluation
	 */
	public IPositionEvaluation getEvaluation();
	
	/**
	 * Evaluates the position from the white side point of view..
	 * @param position position to evaluate
	 * @param attackCalculator calculator of attacks, it will be filled by current position
	 */
	public void evaluate (final Position position, final AttackCalculator attackCalculator);

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
