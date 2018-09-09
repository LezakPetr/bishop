package bishop.engine;

import java.util.function.Supplier;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Rank;
import bishop.base.Square;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import math.Utils;

public class PawnRaceEvaluator {

	private static final int MIN_REMAINDER = 2;
	private static final int MAX_EVALUATION = 2;

	private final IPositionEvaluation positionEvaluation;
	private final long[] pawnsMask = new long[Color.LAST];
	private final int[] kingPosition = new int[Color.LAST];
	private final long[] unstoppablePawns = new long[Color.LAST];
	private long occupancy;
	private int onTurn;
	private int notOnTurn;
	
	public PawnRaceEvaluator (final Supplier<IPositionEvaluation> evaluationFactory) {
		this.positionEvaluation = evaluationFactory.get();
	}

	public IPositionEvaluation evaluate (final Position position) {
		positionEvaluation.clear();
		occupancy = BitBoard.EMPTY;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			pawnsMask[color] = position.getPiecesMask(color, PieceType.PAWN);
			kingPosition[color] = position.getKingPosition(color);
		}
		
		occupancy = position.getOccupancy();
		onTurn = position.getOnTurn();
		notOnTurn = Color.getOppositeColor(onTurn);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			unstoppablePawns[color] = determineUnstoppablePawns (color);
		}
		
		final boolean someWhiteUnstoppablePawn = (unstoppablePawns[Color.WHITE] != 0);
		final boolean someBlackUnstoppablePawn = (unstoppablePawns[Color.BLACK] != 0);
		
		if (someWhiteUnstoppablePawn || someBlackUnstoppablePawn) {
			if (someWhiteUnstoppablePawn && someBlackUnstoppablePawn) {
				final int evaluation = evaluatePawnRaces();
				positionEvaluation.addCoeff(PositionEvaluationCoeffs.RULE_OF_SQUARE_PAWN_RACE_BONUS, Color.WHITE, evaluation);
			}
			else {
				final int evaluation = (someWhiteUnstoppablePawn) ? +1 : -1;
				positionEvaluation.addCoeff(PositionEvaluationCoeffs.RULE_OF_SQUARE_SINGLE_PAWN_BONUS, Color.WHITE, evaluation);
			}
		}
		
		return positionEvaluation;
	}
	
	private long determineUnstoppablePawns (final int color) {
		final int oppositeColor = Color.getOppositeColor(color);
		final long oppositePawnsMask = pawnsMask[oppositeColor];
		final long directlyBlockedSquares = occupancy | BoardConstants.getPawnsAttackedSquares(oppositeColor, oppositePawnsMask);
		final long blockedSquares = (color == Color.WHITE) ?
				BitBoard.extendBackward(directlyBlockedSquares) >>> File.LAST :
				BitBoard.extendForward(directlyBlockedSquares) << File.LAST;
		final long sourceMask = pawnsMask[color] & ~blockedSquares;

		long result = BitBoard.EMPTY;
		
		for (BitLoop loop = new BitLoop(sourceMask); loop.hasNextSquare(); ) {
			final int pawnSquare = loop.getNextSquare();
			
			if (evaluatePawnVsKing(color, pawnSquare))
				result |= BitBoard.getSquareMask(pawnSquare);
		}
		
		return result;
	}
	
	private boolean evaluatePawnVsKing(final int pawnColor, final int pawnSquare) {
		final int kingColor = Color.getOppositeColor(pawnColor);
		final int pawnsKingSquare = kingPosition[pawnColor];
		final long attendableSquares = 
				~pawnsMask[kingColor] &
				~BitBoard.getSquareMask(pawnsKingSquare) &
				~FigureAttackTable.getItem(PieceType.KING, pawnsKingSquare) &
				~BoardConstants.getPawnsAttackedSquares(kingColor, pawnsMask[pawnColor]);
		
		final int pawnFile = Square.getFile(pawnSquare);
		int pawnRank = Square.getRank(pawnSquare);
		
		long possibleKingMask = BitBoard.getSquareMask(kingPosition[kingColor]);
		boolean notFirstIteration = false;
		
		while (pawnRank > Rank.R1 && pawnRank < Rank.R8) {
			if (notFirstIteration || pawnColor == onTurn) {
				if (pawnColor == Color.WHITE)
					pawnRank += (pawnRank == Rank.R2) ? 2 : 1;
				else
					pawnRank -= (pawnRank == Rank.R7) ? 2 : 1;
			}
			
			final int currentPawnSquare = Square.onFileRank(pawnFile, pawnRank);
			final long possibleTargetSquares = BoardConstants.getKingsAttackedSquares (possibleKingMask);
			final long currentAttendableSquares = attendableSquares & ~PawnAttackTable.getItem(pawnColor, currentPawnSquare);
			final long frontSquares = BoardConstants.getSquaresInFrontInclusive(pawnColor, currentPawnSquare);
			
			possibleKingMask |= possibleTargetSquares & currentAttendableSquares;
			
			if ((possibleKingMask & frontSquares) != 0)
				return false;
			
			notFirstIteration = true;
		}
		
		return true;
	}
	
	private int evaluatePawnRaces() {
		int firstEvaluation = -MAX_EVALUATION;

		for (BitLoop firstLoop = new BitLoop(unstoppablePawns[onTurn]); firstLoop.hasNextSquare(); ) {
			final int firstPawnSquare = firstLoop.getNextSquare();
			final int firstPawnFile = Square.getFile(firstPawnSquare);
			final int firstPawnDistance = BoardConstants.getPawnPromotionDistance(onTurn, firstPawnSquare);
			
			int secondEvaluation = +MAX_EVALUATION;
			
			for (BitLoop secondLoop = new BitLoop(unstoppablePawns[notOnTurn]); secondLoop.hasNextSquare(); ) {
				final int secondPawnSquare = secondLoop.getNextSquare();
				final int secondPawnFile = Square.getFile(secondPawnSquare);
				final int secondPawnDistance = BoardConstants.getPawnPromotionDistance(notOnTurn, secondPawnSquare);

				final boolean onTurnPromoted;
				final int remainder;
				final int firstPawnTargetRank;
				final int secondPawnTargetRank;
				
				// Determine which pawn is promoted first by promotion distance. If the promotion distance is equal then the pawn on turn.
				if (firstPawnDistance <= secondPawnDistance) {
					remainder = secondPawnDistance - firstPawnDistance + 1;   // +1 = pawn on turn was promoted  
					firstPawnTargetRank = BoardConstants.getPawnPromotionRank(onTurn);
					secondPawnTargetRank = (firstPawnDistance == 0) ? Square.getRank(secondPawnSquare) : Rank.getAbsolute (Rank.R1 + remainder, onTurn);
					onTurnPromoted = true;
				}
				else {
					remainder = firstPawnDistance - secondPawnDistance;
					firstPawnTargetRank = (secondPawnDistance == 0) ? Square.getRank(firstPawnSquare) :  Rank.getAbsolute (Rank.R8 - remainder, onTurn);
					secondPawnTargetRank = BoardConstants.getPawnPromotionRank(onTurn);
					onTurnPromoted = false;
				}
				
				final int firstPawnTargetSquare = Square.onFileRank(firstPawnFile, firstPawnTargetRank);
				final long onTurnChangeMask = BitBoard.getSquareMask(firstPawnSquare) ^  BitBoard.getSquareMask(firstPawnTargetSquare);
				
				final int secondPawnTargetSquare = Square.onFileRank(secondPawnFile, secondPawnTargetRank);
				final long oppositeChangeMask = BitBoard.getSquareMask(secondPawnSquare) ^  BitBoard.getSquareMask(secondPawnTargetSquare);
				
				final int promotedSquare = (onTurnPromoted) ? firstPawnTargetSquare : secondPawnTargetSquare;
				
				pawnsMask[onTurn] ^= onTurnChangeMask;
				pawnsMask[notOnTurn] ^= oppositeChangeMask;
				occupancy ^= onTurnChangeMask ^ oppositeChangeMask;
				
				final int evaluation = evaluatePromotedPawnRace (onTurnPromoted, remainder, promotedSquare);
				secondEvaluation = Math.min(secondEvaluation, evaluation);
				
				pawnsMask[onTurn] ^= onTurnChangeMask;
				pawnsMask[notOnTurn] ^= oppositeChangeMask;
				occupancy ^= onTurnChangeMask ^ oppositeChangeMask;
			}
			
			firstEvaluation = Math.max(firstEvaluation, secondEvaluation);
		}
		
		return Evaluation.getAbsolute(firstEvaluation, onTurn);
	}

	private int evaluatePromotedPawnRace(final boolean onTurnPromoted, final int remainder, final int promotionSquare) {
		int effectiveRemainder = remainder;
		
		final int kingSquare = (onTurnPromoted) ? kingPosition[notOnTurn] : kingPosition[onTurn];
		
		if (isCheck(promotionSquare, kingSquare))
			effectiveRemainder++;

		if (effectiveRemainder < MIN_REMAINDER)
			return 0;

		final int evaluation = Math.min(effectiveRemainder - MIN_REMAINDER + 1, MAX_EVALUATION);

		return (onTurnPromoted) ? +evaluation : -evaluation;
	}
	
	private boolean isCheck(final int promotionSquare, final int kingSquare) {
		final long queenMask = FigureAttackTable.getItem(PieceType.QUEEN, promotionSquare);
		
		return (queenMask & BitBoard.getSquareMask(kingSquare)) != 0 && (BetweenTable.getItem(promotionSquare, kingSquare) & occupancy) == 0;
	}
			
}
