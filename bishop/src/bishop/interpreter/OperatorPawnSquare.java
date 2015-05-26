package bishop.interpreter;

import bishop.base.BitBoard;
import bishop.base.Color;
import bishop.engine.RuleOfSquare;

public class OperatorPawnSquare implements IExpression {

	private final IExpression operandPawnColor;
	private final IExpression operandOnTurn;
	private final IExpression operandPawnMask;
	
	public OperatorPawnSquare (final IExpression operandPawnColor, final IExpression operandOnTurn, final IExpression operandPawnMask) {
		this.operandPawnColor = operandPawnColor;
		this.operandOnTurn = operandOnTurn;
		this.operandPawnMask = operandPawnMask;
	}
	
	@Override
	public long evaluate(final Context context) {
		final int pawnColor = (int) operandPawnColor.evaluate(context);
		final int onTurn = (int) operandOnTurn.evaluate(context);
		final long pawnMask = operandPawnMask.evaluate(context);
		final int pawnSquare;
		
		if (pawnColor == Color.WHITE)
			pawnSquare = BitBoard.getLastSquare(pawnMask);
		else
			pawnSquare = BitBoard.getFirstSquare(pawnMask);
		
		return RuleOfSquare.getKingDefendingSquares(pawnColor, onTurn, pawnSquare);
	}
	
}
