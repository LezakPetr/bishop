package bishop.engine;

import java.io.PrintWriter;
import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.CastlingType;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.base.Square;
import bishop.tables.MainKingProtectionPawnsTable;
import bishop.tables.SecondKingProtectionPawnsTable;

public final class MiddleGamePositionEvaluator implements IPositionEvaluator {
	
	private static final int MAX_POSITIONAL_EVALUATION = PieceTypeEvaluations.getPawnMultiply (2.0);
	private static final AttackEvaluationTable ATTACK_TABLE = new AttackEvaluationTable(0.0, 0.0, new double[Square.LAST]);
	
	private final TablePositionEvaluator tablePositionEvaluator;
	private final BishopColorPositionEvaluator bishopColorPositionEvaluator;
	private final MobilityPositionEvaluator mobilityEvaluator;
	
	private final MiddleGameEvaluatorSettings settings;
	private final PawnStructureEvaluator pawnStructureCalculator;
	
	
	public MiddleGamePositionEvaluator(final MiddleGameEvaluatorSettings settings) {
		this.settings = settings;
		
		tablePositionEvaluator = new TablePositionEvaluator(settings.getTablePositionEvaluatorSettings());
		bishopColorPositionEvaluator = new BishopColorPositionEvaluator();
		mobilityEvaluator = new MobilityPositionEvaluator(settings.getMobilityEvaluatorSettings());
		pawnStructureCalculator = new PawnStructureEvaluator(settings.getPawnStructureEvaluatorSettings());
	}
	
	private void clear() {
		pawnStructureCalculator.clear();
	}
	
	
	private int evaluateRooks(final Position position) {
		int rookEvaluation = 0;
		
		// Rooks on open files
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK);
			final long openRooks = rookMask & pawnStructureCalculator.getOpenFileSquares(color);
			
			rookEvaluation += BitBoard.getSquareCount(openRooks) * settings.getRookOnOpenFileBonus(color);
		}
		
		return rookEvaluation;
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
		pawnStructureCalculator.calculate(position);

		int positionalEvaluation = 0;
		
		positionalEvaluation += tablePositionEvaluator.evaluatePosition(position);
		positionalEvaluation += bishopColorPositionEvaluator.evaluatePosition(position);
		
		positionalEvaluation += evaluateRooks(position);
		positionalEvaluation += evaluateKingFiles(position);
		positionalEvaluation += pawnStructureCalculator.evaluate(position, attackCalculator);
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
		pawnStructureCalculator.writeLog (writer);
	}

}
