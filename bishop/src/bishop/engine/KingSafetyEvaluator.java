package bishop.engine;

import bishop.base.*;
import bishop.tables.MainKingProtectionPawnsTable;
import bishop.tables.SecondKingProtectionPawnsTable;

import java.util.function.Supplier;

public class KingSafetyEvaluator {
	

	private final GameStageCoeffs coeffs;
	private final IPositionEvaluation evaluation;
	
	public KingSafetyEvaluator (final GameStageCoeffs coeffs, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.coeffs = coeffs;
		this.evaluation = evaluationFactory.get();
	}
	
	public IPositionEvaluation evaluate(final Position position, final AttackCalculator attackCalculator) {
		evaluation.clear();
		
		evaluateKingFiles(position);
		evaluateAttack(position, attackCalculator);
		
		return evaluation;
	}
	
	private void evaluateAttack(final Position position, final AttackCalculator attackCalculator) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int attackEvaluation = attackCalculator.getAttackEvaluation(color);
			final int transformedEvaluation = transformAttackEvaluation(attackEvaluation);
			
			evaluation.addCoeff(coeffs.kingAttackBonus, color, transformedEvaluation);
		}	
	}

	private int transformAttackEvaluation(final int attackEvaluation) {
		return attackEvaluation;
	}
	
	private void evaluateConcreteKingFiles (final Position position, final int color, final int castlingType, final int shift) {
		final long pawnMask = position.getPiecesMask(color, PieceType.PAWN);
		
		final long mainPawnMask = pawnMask & MainKingProtectionPawnsTable.getItem(color, castlingType);
		final int mainPawnCount = BitBoard.getSquareCount(mainPawnMask);
		evaluation.addCoeff(coeffs.kingMainProtectionPawnBonus, color, mainPawnCount << shift);
		
		final long alreadyProtected = (mainPawnMask << File.COUNT) | (mainPawnMask >>> File.COUNT);
		final long secondPawnMask = pawnMask & SecondKingProtectionPawnsTable.getItem(color, castlingType) & ~alreadyProtected;
		final int secondPawnCount = BitBoard.getSquareCount(secondPawnMask);
		evaluation.addCoeff(coeffs.kingSecondProtectionPawnBonus, color, secondPawnCount << shift);
	}
	
	private void evaluateKingFiles(final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int kingSquare = position.getKingPosition(color);
			final int kingFile = Square.getFile(kingSquare);
			
			final int shift = (kingFile == File.FD || kingFile == File.FE) ? 0 : 1;
			
			if (kingFile >= File.FD)
				evaluateConcreteKingFiles (position, color, CastlingType.SHORT, shift);

			if (kingFile <= File.FE)
				evaluateConcreteKingFiles (position, color, CastlingType.LONG, shift);
		}
	}

}
