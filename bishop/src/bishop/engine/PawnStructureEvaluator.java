package bishop.engine;

import java.util.function.Supplier;

import bishop.base.*;
import bishop.tables.FrontSquaresOnSameFileTable;

abstract public class PawnStructureEvaluator {

	protected static final PawnStructureCoeffs ENDING_COEFFS = PositionEvaluationCoeffs.PAWN_STRUCTURE_COEFFS.get(CombinedEvaluation.COMPONENT_ENDING);
	protected static final PawnStructureCoeffs OPENING_COEFFS = PositionEvaluationCoeffs.PAWN_STRUCTURE_COEFFS.get(CombinedEvaluation.COMPONENT_OPENING);
	protected static final PawnStructureCoeffs MIDDLE_GAME_COEFFS = PositionEvaluationCoeffs.PAWN_STRUCTURE_COEFFS.get(CombinedEvaluation.COMPONENT_MIDDLE_GAME);

	protected static final int OPENING_COEFF_DIFF = OPENING_COEFFS.getFirstCoeff() - ENDING_COEFFS.getFirstCoeff();
	protected static final int MIDDLE_GAME_COEFF_DIFF = MIDDLE_GAME_COEFFS.getFirstCoeff() - ENDING_COEFFS.getFirstCoeff();

	protected final IPositionEvaluation evaluation;
	protected final PawnStructureData structureData = new PawnStructureData();

	protected PawnStructureEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluation = evaluationFactory.get();
	}

	abstract public IPositionEvaluation evaluate(final Position position, final int gameStage);

	protected void evaluatePawnStructure() {
		structureData.calculate();

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long ownPawnMask = structureData.getPawnMask(color);
			final long passedPawnMask = structureData.getPassedPawnMask(color);
			final long connectedPawnMask = ownPawnMask & BoardConstants.getAllConnectedPawnSquareMask(ownPawnMask);
			final long protectedPawnMask = ownPawnMask & BoardConstants.getPawnsAttackedSquares(color, ownPawnMask);
			final long singleDisadvantageAttackPawnMask = structureData.getSingleDisadvantageAttackPawnMask(color);
			final long doubleDisadvantageAttackPawnMask = structureData.getDoubleDisadvantageAttackPawnMask(color);
			final long blockedPawnMask = structureData.getBlockedPawnMask(color);
			final int oppositePawnFiles = structureData.getPawnFiles(oppositeColor);
			
			for (BitLoop loop = new BitLoop(ownPawnMask); loop.hasNextSquare(); ) {
				final int square = loop.getNextSquare();
				final long squareMask = BitBoard.getSquareMask(square);
				final int rank = Square.getRank(square);
				final long frontSquaresOnSameFile = FrontSquaresOnSameFileTable.getItem(color, square);

				// Passed pawn
				if ((passedPawnMask & squareMask) != 0) {
					if ((connectedPawnMask & squareMask) != 0)
						addCachedEvaluation(ENDING_COEFFS.getConnectedPassedPawnBonusCoeff(color, rank), color);
					else {
						if ((protectedPawnMask & squareMask) != 0)
							addCachedEvaluation(ENDING_COEFFS.getProtectedPassedPawnBonusCoeff(color, rank), color);
						else
							addCachedEvaluation(ENDING_COEFFS.getSinglePassedPawnBonusCoeff(color, rank), color);
					}
					
					// Outside passed pawn bonus
					final int file = Square.getFile(square);
					final int minOppositePawnFileDistance = BoardConstants.getMinFileDistance (oppositePawnFiles, file);
					addCachedEvaluation(ENDING_COEFFS.getOutsidePassedPawnBonusCoeff(minOppositePawnFileDistance), color);
				}
				else {
					if ((connectedPawnMask & squareMask) != 0)
						addCachedEvaluation(ENDING_COEFFS.getConnectedNotPassedPawnBonusCoeff(color, rank), color);
					else {
						if ((protectedPawnMask & squareMask) != 0)
							addCachedEvaluation(ENDING_COEFFS.getProtectedNotPassedPawnBonusCoeff(color, rank), color);
					}
					
					if ((singleDisadvantageAttackPawnMask & squareMask) != 0)
						addCachedEvaluation(ENDING_COEFFS.getSingleDisadvantageAttackPawnBonusCoeff(color, rank), color);
					
					if ((doubleDisadvantageAttackPawnMask & squareMask) != 0)
						addCachedEvaluation(ENDING_COEFFS.getDoubleDisadvantageAttackPawnBonusCoeff(color, rank), color);
					
					if ((blockedPawnMask & squareMask) != 0)
						addCachedEvaluation(ENDING_COEFFS.getBlockedPawnBonusCoeff(color, rank), color);
				}
				
				// Double pawn
				if ((frontSquaresOnSameFile & ownPawnMask) != 0)
					addCachedEvaluation(ENDING_COEFFS.getDoublePawnBonusCoeff(color, rank), color);
			}
		}
		
		// Pawn islands
		for (int i = 0; i < PawnStructureData.MAX_PAWN_ISLAND_COUNT; i++) {
			final int whitePawnCount = structureData.getIslandPawnCount(i, Color.WHITE);
			final int blackPawnCount = structureData.getIslandPawnCount(i, Color.BLACK);
							
			if (whitePawnCount > blackPawnCount && structureData.isIslandAlive(i, Color.WHITE))
				addCachedEvaluation(ENDING_COEFFS.getPawnMajorityCoeff(blackPawnCount), Color.WHITE);
			
			if (blackPawnCount > whitePawnCount && structureData.isIslandAlive(i, Color.BLACK))
				addCachedEvaluation(ENDING_COEFFS.getPawnMajorityCoeff(whitePawnCount), Color.BLACK);
		}
	}

	abstract protected void addCachedEvaluation(final int endingCoeff, final int color);

	abstract protected void addPositionDependentEvaluation(final int endingCoeff, final int color, final int coeffCount);


	protected void evaluatePositionDependent(final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long oppositeRookMask = position.getPiecesMask(oppositeColor, PieceType.ROOK);
			
			if (oppositeRookMask != 0) {
				final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
				final long unprotectedPawnMask = ownPawnMask & ~BoardConstants.getPawnsAttackedSquares(color, ownPawnMask);
				final long frontSquares = structureData.getFrontSquares(color);

				final long unprotectedOpenPawnMask = unprotectedPawnMask & ~frontSquares;
				final int endingCoeff = ENDING_COEFFS.getCoeffUnprotectedOpenFilePawnBonus();
				final int coeffCount = BitBoard.getSquareCount(unprotectedOpenPawnMask);

				addPositionDependentEvaluation(endingCoeff, color, coeffCount);
			}
		}
	}


	public long getSecureSquares(final int color) {
		return structureData.getSecureSquares(color);
	}


	public long getBackSquares(final int color) {
		return structureData.getBackSquares(color);
	}

	public void calculate(final Position position) {
		structureData.precalculate(position.getPiecesMask(Color.WHITE, PieceType.PAWN), position.getPiecesMask(Color.BLACK, PieceType.PAWN));
	}

	public void clear() {
		structureData.clear();
		evaluation.clear();
	}


}
