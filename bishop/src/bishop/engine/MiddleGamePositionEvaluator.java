package bishop.engine;

import java.io.PrintWriter;
import java.util.Arrays;
import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.CastlingType;
import bishop.base.Color;
import bishop.base.CrossDirection;
import bishop.base.File;
import bishop.base.LineAttackTable;
import bishop.base.LineIndexer;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.base.Square;
import bishop.tables.FigureAttackTable;
import bishop.tables.MainKingProtectionPawnsTable;
import bishop.tables.SecondKingProtectionPawnsTable;

public final class MiddleGamePositionEvaluator implements IPositionEvaluator {
	
	private static final int MAX_POSITIONAL_EVALUATION = PieceTypeEvaluations.getPawnMultiply (2.0);
	private static final AttackEvaluationTable ATTACK_TABLE = new AttackEvaluationTable(0.0, 0.0, new double[Square.LAST]);
	
	private final TablePositionEvaluator tablePositionEvaluator;
	private final BishopColorPositionEvaluator bishopColorPositionEvaluator;
	private final MobilityPositionEvaluator mobilityEvaluator;
	
	private final MiddleGameEvaluatorSettings settings;
	
	// Contains 1 on squares without own pawn in front of them.
	// Front pawn itself is also considered open. 
	private final long[] openFileSquares;
	
	private final long[] oppositeFileSquares;

	
	public MiddleGamePositionEvaluator(final MiddleGameEvaluatorSettings settings) {
		this.settings = settings;
		
		openFileSquares = new long[Color.LAST];
		oppositeFileSquares = new long[Color.LAST];
		
		tablePositionEvaluator = new TablePositionEvaluator(settings.getTablePositionEvaluatorSettings());
		bishopColorPositionEvaluator = new BishopColorPositionEvaluator();
		mobilityEvaluator = new MobilityPositionEvaluator(settings.getMobilityEvaluatorSettings());
	}
	
	private void clear() {
		Arrays.fill(openFileSquares, 0);
		Arrays.fill(oppositeFileSquares, 0);
	}
	
	private void fillOpenFileSquares(final Position position) {
		// White
		long whiteNotFileOpenSquares = position.getPiecesMask(Color.WHITE, PieceType.PAWN);
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 8;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 16;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 32;
		
		openFileSquares[Color.WHITE] = ~(whiteNotFileOpenSquares >>> 8);
		
		// Black
		long blackNotFileOpenSquares = position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 8;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 16;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 32;
		
		openFileSquares[Color.BLACK] = ~(blackNotFileOpenSquares << 8);
	}

	private void fillOppositeFileSquares(final Position position) {
		// White
		long whiteNotFileSquares = position.getPiecesMask(Color.WHITE, PieceType.PAWN);
		whiteNotFileSquares |= whiteNotFileSquares << 8;
		whiteNotFileSquares |= whiteNotFileSquares << 16;
		whiteNotFileSquares |= whiteNotFileSquares << 32;
		
		oppositeFileSquares[Color.WHITE] = ~whiteNotFileSquares;
		
		// Black
		long blackNotFileSquares = position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		blackNotFileSquares |= blackNotFileSquares >>> 8;
		blackNotFileSquares |= blackNotFileSquares >>> 16;
		blackNotFileSquares |= blackNotFileSquares >>> 32;
		
		oppositeFileSquares[Color.BLACK] = ~blackNotFileSquares;
	}

	private int evaluateRooks(final Position position) {
		int rookEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK);
			final long openRooks = rookMask & openFileSquares[color];
			
			rookEvaluation += BitBoard.getSquareCount(openRooks) * settings.getRookOnOpenFileBonus(color);
		}
		
		return rookEvaluation;
	}
	
	private int evaluateUnprotectedOpenFilePawns(final Position position, final AttackCalculator attackCalculator) {
		int unprotectedOpenFilePawnsEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long oppositeRookMask = position.getPiecesMask(oppositeColor, PieceType.ROOK);
			
			if (oppositeRookMask != 0) {
				final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
				final long unprotectedPawnMask = ownPawnMask & ~attackCalculator.getPawnAttackedSquares(color);
				final long unprotectedOpenPawnMask = unprotectedPawnMask & oppositeFileSquares[oppositeColor];
				
				unprotectedOpenFilePawnsEvaluation += BitBoard.getSquareCount(unprotectedOpenPawnMask) * settings.getUnprotectedOpenFilePawnBonus(color);
			}
		}
		
		return unprotectedOpenFilePawnsEvaluation;
	}
	
	private int evaluateDoublePawns(final Position position) {
		int doublePawnEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
			final long doublePawns = ownPawnMask & ~openFileSquares[color];
			doublePawnEvaluation += BitBoard.getSquareCount(doublePawns) * settings.getDoublePawnBonus(color);
		}
		
		return doublePawnEvaluation;
	}

	public int evaluatePosition(final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		clear();
		
		attackCalculator.calculate(position, ATTACK_TABLE);
		
		final int materialEvaluation = position.getMaterialEvaluation();
		
		final int lowerBound = materialEvaluation + MAX_POSITIONAL_EVALUATION;
		
		if (lowerBound < alpha)
			return lowerBound;
		
		final int upperBound = materialEvaluation - MAX_POSITIONAL_EVALUATION;
		
		if (upperBound > beta)
			return upperBound;
		
		final int positionalEvaluation = calculatePositionalEvaluation(position, attackCalculator);
		
		final int reducedPositionalEvaluation = Math.min (Math.max (positionalEvaluation, -MAX_POSITIONAL_EVALUATION), MAX_POSITIONAL_EVALUATION);
		final int evaluation = materialEvaluation + reducedPositionalEvaluation;
		
		return evaluation;
	}

	private int calculatePositionalEvaluation(final Position position, final AttackCalculator attackCalculator) {
		fillOpenFileSquares(position);
		fillOppositeFileSquares(position);

		int positionalEvaluation = 0;
		
		positionalEvaluation += tablePositionEvaluator.evaluatePosition(position);
		positionalEvaluation += bishopColorPositionEvaluator.evaluatePosition(position);
		
		positionalEvaluation += evaluateRooks(position);
		positionalEvaluation += evaluateKingFiles(position);
		positionalEvaluation += evaluateUnprotectedOpenFilePawns(position, attackCalculator);
		positionalEvaluation += evaluateDoublePawns(position);
		positionalEvaluation += attackCalculator.getAttackEvaluation();
		positionalEvaluation += mobilityEvaluator.evaluatePosition(position, attackCalculator);
		positionalEvaluation += evaluateQueenMove(position);
		
		return positionalEvaluation;
	}
	
	private int evaluateConcreteKingFiles (final Position position, final int color, final int castlingType) {
		final long pawnMask = position.getPiecesMask(color, PieceType.PAWN);
		int evaluation = 0;
		
		final long mainPawnMask = pawnMask & MainKingProtectionPawnsTable.getItem(color, castlingType);
		final int mainPawnCount = BitBoard.getSquareCount(mainPawnMask);
		evaluation += settings.getKingMainProtectionPawnBonus(color) * mainPawnCount;
		
		final long secondPawnMask = pawnMask & SecondKingProtectionPawnsTable.getItem(color, castlingType);
		final int secondPawnCount = BitBoard.getSquareCount(secondPawnMask);
		evaluation += settings.getKingSecondProtectionPawnBonus(color) * secondPawnCount;
		
		return evaluation;
	}
	
	private int evaluateKingFiles(final Position position) {
		int kingFilesEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int kingSquare = position.getKingPosition(color);
			final int kingFile = Square.getFile(kingSquare);
			
			int evaluation = 0;
			int count = 0;
			
			if (kingFile >= File.FD) {
				evaluation += evaluateConcreteKingFiles (position, color, CastlingType.SHORT);
				count++;
			}

			if (kingFile <= File.FE) {
				evaluation += evaluateConcreteKingFiles (position, color, CastlingType.LONG);
				count++;
			}
			
			if (count == 2) {
				evaluation = evaluation >> 1;
			}
			
			kingFilesEvaluation += evaluation;
		}
		
		return kingFilesEvaluation;
	}
	
	private int evaluateQueenMove(final Position position) {
		int queenMoveEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long figureMask = position.getPiecesMask(color, PieceType.BISHOP) | position.getPiecesMask(color, PieceType.QUEEN);
			final long queenMask = position.getPiecesMask(color, PieceType.QUEEN);
			final long firstRankMask = BoardConstants.getFirstRankMask (color);
			final long figuresOnFirstRank = figureMask & firstRankMask;
			
			if (figuresOnFirstRank != 0 && (queenMask & ~firstRankMask) != 0) {
				queenMoveEvaluation += settings.getQueenMoveBonus(color);
			}
		}
		
		return queenMoveEvaluation;
	}	

	public void writeLog(final PrintWriter writer) {
		tablePositionEvaluator.writeLog(writer);
		bishopColorPositionEvaluator.writeLog(writer);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Open file squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, openFileSquares[color]);
			writer.println();			
		}
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Opposite file squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, oppositeFileSquares[color]);
			writer.println();			
		}
	}

}
