package bishop.engine;


import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Square;
import bishop.tables.FrontSquaresOnSameFileTable;

public class PawnStructureEvaluator {
	
	private final IPositionEvaluation evaluation;
	private final PawnStructureFeatures coeffs;
	private final PawnStructureCache structureCache;
	private final PawnStructureData structureData;
	
	public PawnStructureEvaluator(final PawnStructureFeatures coeffs, final PawnStructureCache structureCache, final IPositionEvaluation evaluation) {
		this.evaluation = evaluation;
		this.coeffs = coeffs;
		this.structureCache = structureCache;
		this.structureData = new PawnStructureData();
	}

	public void evaluate(final Position position, final AttackCalculator attackCalculator) {
		final PawnStructure structure = position.getPawnStructure();
		structureCache.getData(structure, structureData);
		
		evaluatePawnStructure(position, attackCalculator);
		evaluateUnprotectedOpenFilePawns(position, attackCalculator);
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
						evaluation.addCoeff(coeffs.getConnectedPassedPawnBonusFeature(color, rank), color);
					else {
						if ((protectedPawnMask & squareMask) != 0)
							evaluation.addCoeff(coeffs.getProtectedPassedPawnBonusFeature(color, rank), color);
						else
							evaluation.addCoeff(coeffs.getSinglePassedPawnBonusFeature(color, rank), color);
					}
					
					// Outside passed pawn bonus
					final int file = Square.getFile(square);
					final int minOppositePawnFileDistance = BoardConstants.getMinFileDistance (oppositePawnFiles, file);
					evaluation.addCoeff(coeffs.getOutsidePassedPawnBonusFeature(minOppositePawnFileDistance), color);
					
					// Pawn with rooks
					final long rearSquaresOnSameFile = FrontSquaresOnSameFileTable.getItem(oppositeColor, square);

					if ((whiteRookMask & rearSquaresOnSameFile) != 0 && (blackRookMask & frontSquaresOnSameFile) != 0) {
						evaluation.addCoeff(coeffs.getRookPawnBonusFeature(color, rank), color, +1);
					}
					
					if ((blackRookMask & rearSquaresOnSameFile) != 0 && (whiteRookMask & frontSquaresOnSameFile) != 0) {
						evaluation.addCoeff(coeffs.getRookPawnBonusFeature(color, rank), color, -1);
					}
				}
				else {
					if ((connectedPawnMask & squareMask) != 0)
						evaluation.addCoeff(coeffs.getConnectedNotPassedPawnBonusFeature(color, rank), color);
					else {
						if ((protectedPawnMask & squareMask) != 0)
							evaluation.addCoeff(coeffs.getProtectedNotPassedPawnBonusFeature(color, rank), color);
					}
					
					if ((singleDisadvantageAttackPawnMask & squareMask) != 0)
						evaluation.addCoeff(coeffs.getSingleDisadvantageAttackPawnBonusFeature(color, rank), color);
					
					if ((doubleDisadvantageAttackPawnMask & squareMask) != 0)
						evaluation.addCoeff(coeffs.getDoubleDisadvantageAttackPawnBonusFeature(color, rank), color);
					
					if ((blockedPawnMask & squareMask) != 0)
						evaluation.addCoeff(coeffs.getBlockedPawnBonusFeature(color, rank), color);
				}
				
				// Double pawn
				if ((frontSquaresOnSameFile & ownPawnMask) != 0) {
					evaluation.addCoeff(coeffs.getDoublePawnBonusFeature(color, rank), color);
				}
			}
		}
		
		// Pawn islands
		for (int i = 0; i < PawnStructureData.MAX_PAWN_ISLAND_COUNT; i++) {
			final int whitePawnCount = structureData.getIslandPawnCount(i, Color.WHITE);
			final int blackPawnCount = structureData.getIslandPawnCount(i, Color.BLACK);
							
			if (whitePawnCount > blackPawnCount && structureData.isIslandAlive(i, Color.WHITE))
				evaluation.addCoeff(coeffs.getPawnMajorityFeature(blackPawnCount), Color.WHITE);
			
			if (blackPawnCount > whitePawnCount && structureData.isIslandAlive(i, Color.BLACK))
				evaluation.addCoeff(coeffs.getPawnMajorityFeature(whitePawnCount), Color.BLACK);
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
				
				evaluation.addCoeff(coeffs.getFeatureUnprotectedOpenFilePawnBonus(), color, BitBoard.getSquareCount(unprotectedOpenPawnMask));
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
