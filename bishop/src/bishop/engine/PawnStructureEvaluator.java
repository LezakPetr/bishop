package bishop.engine;

import java.util.function.Supplier;

import bishop.base.*;
import bishop.tables.FrontSquaresOnSameFileTable;

public class PawnStructureEvaluator {

	private static final PawnStructureCoeffs ENDING_COEFFS = PositionEvaluationCoeffs.PAWN_STRUCTURE_COEFFS.get(CombinedEvaluation.COMPONENT_ENDING);
	private static final PawnStructureCoeffs OPENING_COEFFS = PositionEvaluationCoeffs.PAWN_STRUCTURE_COEFFS.get(CombinedEvaluation.COMPONENT_OPENING);

	private static final int OPENING_COEFF_DIFF = OPENING_COEFFS.getFirstCoeff() - ENDING_COEFFS.getFirstCoeff();

	private final IPositionEvaluation evaluation;
	private final PawnStructureCache structureCache;
	private final IPositionEvaluation openingCachedEvaluation;
	private final IPositionEvaluation endingCachedEvaluation;
	private final IPositionEvaluation openingPositionDependentEvaluation;
	private final IPositionEvaluation endingPositionDependentEvaluation;
	private final PawnStructureData structureData = new PawnStructureData();

	public PawnStructureEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluation = evaluationFactory.get();
		this.openingCachedEvaluation = evaluationFactory.get();
		this.endingCachedEvaluation = evaluationFactory.get();
		this.openingPositionDependentEvaluation = evaluationFactory.get();
		this.endingPositionDependentEvaluation = evaluationFactory.get();
		this.structureCache = new PawnStructureCache(
			pawnStructure -> {
				evaluatePawnStructure(pawnStructure);

				return CombinedEvaluation.combine(openingCachedEvaluation.getEvaluation(), endingCachedEvaluation.getEvaluation());
			}
		);
	}

	public IPositionEvaluation evaluate(final Position position, final int gameStage) {
		final PawnStructure structure = position.getPawnStructure();

		if (evaluation instanceof CoeffCountPositionEvaluation) {
			evaluatePawnStructure(structure);

			final int alpha = CombinedEvaluation.getAlphaForGameStage(gameStage);

			evaluation.addSubEvaluation(openingCachedEvaluation, alpha);
			evaluation.addSubEvaluation(endingCachedEvaluation, CombinedEvaluation.MAX_ALPHA - alpha);

			evaluation.addSubEvaluation(openingPositionDependentEvaluation, alpha);
			evaluation.addSubEvaluation(endingPositionDependentEvaluation, CombinedEvaluation.MAX_ALPHA - alpha);

			evaluation.shiftRight(CombinedEvaluation.ALPHA_BITS);
		}
		else {
			long combinedEvaluation = CombinedEvaluation.ACCUMULATOR_BASE;
			combinedEvaluation += structureCache.getCombinedEvaluation(structure);

			evaluatePositionDependent(position);
			combinedEvaluation += CombinedEvaluation.combine(openingPositionDependentEvaluation.getEvaluation(), endingPositionDependentEvaluation.getEvaluation());

			final long multiplicator = CombinedEvaluation.getMultiplicatorForGameStage(gameStage);
			evaluation.addEvaluation(CombinedEvaluation.decode(combinedEvaluation, multiplicator));
		}

		return evaluation;
	}
		
	private void evaluatePawnStructure(final PawnStructure structure) {
		structureData.calculate();

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long ownPawnMask = structure.getPawnMask(color);
			final long passedPawnMask = structureData.getPassedPawnMask(color);
			final long connectedPawnMask = structureData.getConnectedPawnMask(color);
			final long protectedPawnMask = structureData.getProtectedPawnMask(color);
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

	private void addCachedEvaluation(int endingCoeff, int color) {
		endingCachedEvaluation.addCoeff(endingCoeff, color);
		openingCachedEvaluation.addCoeff(endingCoeff + OPENING_COEFF_DIFF, color);
	}

	private void evaluatePositionDependent(final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long oppositeRookMask = position.getPiecesMask(oppositeColor, PieceType.ROOK);
			
			if (oppositeRookMask != 0) {
				final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
				final long unprotectedPawnMask = ownPawnMask & ~BoardConstants.getPawnsAttackedSquares(color, ownPawnMask);
				final long frontSquares = structureData.getFrontSquares(color);

				final long unprotectedOpenPawnMask = unprotectedPawnMask & ~frontSquares;

				endingPositionDependentEvaluation.addCoeff(ENDING_COEFFS.getCoeffUnprotectedOpenFilePawnBonus(), color, BitBoard.getSquareCount(unprotectedOpenPawnMask));
				openingPositionDependentEvaluation.addCoeff(OPENING_COEFFS.getCoeffUnprotectedOpenFilePawnBonus(), color, BitBoard.getSquareCount(unprotectedOpenPawnMask));
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
		structureData.precalculate(position.getPawnStructure());
	}

	public void clear() {
		structureData.clear();
		evaluation.clear();
		openingCachedEvaluation.clear();
		endingCachedEvaluation.clear();
		openingPositionDependentEvaluation.clear();
		endingPositionDependentEvaluation.clear();
	}


}
