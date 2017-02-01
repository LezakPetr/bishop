package bishop.engine;

import java.util.function.Supplier;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Square;
import bishop.tables.FrontSquaresOnSameFileTable;
import bishop.tables.PawnAttackTable;

public class PawnStructureEvaluator {
	
	private final IPositionEvaluation evaluation;
	private final PawnStructureCoeffs coeffs;
	private final PawnStructureCache structureCache;
	private final PawnStructureData structureData;
	
	public PawnStructureEvaluator(final PawnStructureCoeffs coeffs, final PawnStructureCache structureCache, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluation = evaluationFactory.get();
		this.coeffs = coeffs;
		this.structureCache = structureCache;
		this.structureData = new PawnStructureData();
	}

	public IPositionEvaluation evaluate(final Position position, final AttackCalculator attackCalculator) {
		evaluation.clear();
		
		final PawnStructure structure = position.getPawnStructure();
		structureCache.getData(structure, structureData);
		
		evaluatePawnStructure(position, attackCalculator);
		evaluateUnprotectedOpenFilePawns(position, attackCalculator);
		
		return evaluation;		
	}
		
	private void evaluatePawnStructure(final Position position, final AttackCalculator attackCalculator) {
		final long whiteRookMask = position.getPiecesMask(Color.WHITE, PieceType.ROOK);
		final long blackRookMask = position.getPiecesMask(Color.BLACK, PieceType.ROOK);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
			final long passedPawnMask = structureData.getPassedPawnMask(color);
			final long connectedPawnMask = structureData.getConnectedPawnMask(color);
			final long protectedPawnMask = structureData.getProtectedPawnMask(color);
			
			for (BitLoop loop = new BitLoop(ownPawnMask); loop.hasNextSquare(); ) {
				final int square = loop.getNextSquare();
				final long squareMask = BitBoard.getSquareMask(square);
				final int rank = Square.getRank(square);
				final long frontSquaresOnSameFile = FrontSquaresOnSameFileTable.getItem(color, square);

				// Passed pawn
				if ((passedPawnMask & squareMask) != 0) {
					if ((connectedPawnMask & squareMask) != 0)
						evaluation.addCoeff(coeffs.getConnectedPassedPawnBonusCoeff(color, rank), color);
					else {
						if ((protectedPawnMask & squareMask) != 0)
							evaluation.addCoeff(coeffs.getProtectedPassedPawnBonusCoeff(color, rank), color);
						else
							evaluation.addCoeff(coeffs.getSinglePassedPawnBonusCoeff(color, rank), color);
					}
					
					// Pawn with rooks
					final long rearSquaresOnSameFile = FrontSquaresOnSameFileTable.getItem(oppositeColor, square);

					if ((whiteRookMask & rearSquaresOnSameFile) != 0 && (blackRookMask & frontSquaresOnSameFile) != 0) {
						evaluation.addCoeff(coeffs.getRookPawnBonusCoeff(color, rank), color, +1);
					}
					
					if ((blackRookMask & rearSquaresOnSameFile) != 0 && (whiteRookMask & frontSquaresOnSameFile) != 0) {
						evaluation.addCoeff(coeffs.getRookPawnBonusCoeff(color, rank), color, -1);
					}
				}
				else {
					if ((connectedPawnMask & squareMask) != 0)
						evaluation.addCoeff(coeffs.getConnectedNotPassedPawnBonusCoeff(color, rank), color);
					else {
						if ((protectedPawnMask & squareMask) != 0)
							evaluation.addCoeff(coeffs.getProtectedNotPassedPawnBonusCoeff(color, rank), color);
					}					
				}
				
				// Double pawn
				if ((frontSquaresOnSameFile & ownPawnMask) != 0) {
					evaluation.addCoeff(coeffs.getDoublePawnBonusCoeff(color, rank), color);
				}
			}
		}
	}
	
	private void evaluateUnprotectedOpenFilePawns(final Position position, final AttackCalculator attackCalculator) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long oppositeRookMask = position.getPiecesMask(oppositeColor, PieceType.ROOK);
			
			if (oppositeRookMask != 0) {
				final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
				final long unprotectedPawnMask = ownPawnMask & ~attackCalculator.getPawnAttackedSquares(color);
				final long unprotectedOpenPawnMask = unprotectedPawnMask & ~structureData.getFrontSquares(oppositeColor);
				
				evaluation.addCoeff(coeffs.getCoeffUnprotectedOpenFilePawnBonus(), color, BitBoard.getSquareCount(unprotectedOpenPawnMask));
			}
		}
	}


	public long getSecureSquares(final int color) {
		return structureData.getSecureSquares(color);
	}


	public long getBackSquares(final int color) {
		return structureData.getBackSquares(color);
	}


	public void clear() {
		structureData.clear();
	}


	public void calculate(final Position position) {
		final PawnStructure structure = position.getPawnStructure();
		
		structureData.calculate(structure);
	}


}
