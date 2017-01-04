package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.CastlingType;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.IMaterialEvaluator;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.base.Square;
import bishop.tables.MainKingProtectionPawnsTable;
import bishop.tables.SecondKingProtectionPawnsTable;

public final class MiddleGamePositionEvaluator implements IPositionEvaluator {
	
	private static final int MAX_POSITIONAL_EVALUATION = PieceTypeEvaluations.getPawnMultiply (2.0);
	
	private final IMaterialEvaluator materialEvaluator;
	private final TablePositionEvaluator tablePositionEvaluator;
	private final BishopColorPositionEvaluator bishopColorPositionEvaluator;
	private final MobilityPositionEvaluator mobilityEvaluator;
	
	private final MiddleGameEvaluatorSettings settings;
	private final PawnStructureEvaluator pawnStructureCalculator;
	private final IPositionEvaluation evaluation;
	private final IPositionEvaluation positionalEvaluation;
	
	
	public MiddleGamePositionEvaluator(final MiddleGameEvaluatorSettings settings, final IMaterialEvaluator materialEvaluator, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.materialEvaluator = materialEvaluator;
		this.settings = settings;
		this.evaluation = evaluationFactory.get();
		this.positionalEvaluation = evaluationFactory.get();
		
		tablePositionEvaluator = new TablePositionEvaluator(PositionEvaluationCoeffs.MIDDLE_GAME_TABLE_EVALUATOR_COEFFS, evaluationFactory);
		bishopColorPositionEvaluator = new BishopColorPositionEvaluator(evaluationFactory);
		mobilityEvaluator = new MobilityPositionEvaluator(evaluationFactory);
		pawnStructureCalculator = new PawnStructureEvaluator(PositionEvaluationCoeffs.MIDDLE_GAME_PAWN_STRUCTURE_COEFFS, evaluationFactory);
	}

	private void clear() {
		evaluation.clear();
		positionalEvaluation.clear();
		pawnStructureCalculator.clear();
	}
	
	private void evaluateRooks(final Position position) {
		// Rooks on open files
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK);
			final long openRooks = rookMask & ~pawnStructureCalculator.getBackSquares(color);
			
			positionalEvaluation.addCoeff(PositionEvaluationCoeffs.ROOK_ON_OPEN_FILE_BONUS, color, BitBoard.getSquareCount(openRooks));
		}
	}
	
	public IPositionEvaluation evaluatePosition(final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		clear();
		
		final int materialEvaluation = materialEvaluator.evaluateMaterial(position);
		
		// Speedup if materialEvaluation is not in <alpha, beta>
		final int lowerBound = materialEvaluation + MAX_POSITIONAL_EVALUATION;
		
		if (lowerBound < alpha) {
			evaluation.addEvaluation(lowerBound);
			return evaluation;
		}
		
		final int upperBound = materialEvaluation - MAX_POSITIONAL_EVALUATION;
		
		if (upperBound > beta) {
			evaluation.addEvaluation(upperBound);
			return evaluation;
		}

		
		// Regular evaluation
		evaluation.addEvaluation(materialEvaluation);

		calculatePositionalEvaluation(position, attackCalculator);
		final int positionalEvaluationValue = positionalEvaluation.getEvaluation();
		
		if (positionalEvaluationValue < -MAX_POSITIONAL_EVALUATION) {
			evaluation.addEvaluation(-MAX_POSITIONAL_EVALUATION);
			return evaluation;
		}
		
		if (positionalEvaluationValue > MAX_POSITIONAL_EVALUATION) {
			evaluation.addEvaluation(MAX_POSITIONAL_EVALUATION);
			return evaluation;
		}
		
		evaluation.addSubEvaluation(positionalEvaluation);
		
		return evaluation;
	}

	private void calculateAttacks(final Position position, final AttackCalculator attackCalculator) {
		final int whiteKingFile = Square.getFile(position.getKingPosition(Color.WHITE));
		final int blackKingFile = Square.getFile(position.getKingPosition(Color.BLACK));
		
		attackCalculator.calculate(position, settings.getAttackTable(whiteKingFile, blackKingFile));
	}

	private void calculatePositionalEvaluation(final Position position, final AttackCalculator attackCalculator) {
		pawnStructureCalculator.calculate(position);
		
		positionalEvaluation.addSubEvaluation(tablePositionEvaluator.evaluatePosition(position));
		positionalEvaluation.addSubEvaluation(bishopColorPositionEvaluator.evaluatePosition(position));
		
		evaluateRooks(position);
		evaluateKingFiles(position);
		
		evaluateQueenMove(position);
		evaluateSecureFigures(position);
		
		calculateAttacks(position, attackCalculator);
		
		positionalEvaluation.addSubEvaluation(pawnStructureCalculator.evaluate(position, attackCalculator));
		positionalEvaluation.addSubEvaluation(attackCalculator.getAttackEvaluation());
		positionalEvaluation.addSubEvaluation(mobilityEvaluator.evaluatePosition(position, attackCalculator));
	}
	
	private void evaluateSecureFigures(final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long securedFigures = position.getColorOccupancy(color) & pawnStructureCalculator.getSecureSquares(color);
			final int count = BitBoard.getSquareCount(securedFigures);
			
			positionalEvaluation.addCoeff(PositionEvaluationCoeffs.FIGURE_ON_SECURE_SQUARE_BONUS, color, count);
		}
	}

	private void evaluateConcreteKingFiles (final Position position, final int color, final int castlingType, final int shift) {
		final long pawnMask = position.getPiecesMask(color, PieceType.PAWN);
		
		final long mainPawnMask = pawnMask & MainKingProtectionPawnsTable.getItem(color, castlingType);
		final int mainPawnCount = BitBoard.getSquareCount(mainPawnMask);
		positionalEvaluation.addCoeff(PositionEvaluationCoeffs.KING_MAIN_PROTECTION_PAWN_BONUS, color, mainPawnCount << shift);
		
		final long secondPawnMask = pawnMask & SecondKingProtectionPawnsTable.getItem(color, castlingType);
		final int secondPawnCount = BitBoard.getSquareCount(secondPawnMask);
		positionalEvaluation.addCoeff(PositionEvaluationCoeffs.KING_SECOND_PROTECTION_PAWN_BONUS, color, secondPawnCount << shift);
	}
	
	private int evaluateKingFiles(final Position position) {
		int kingFilesEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int kingSquare = position.getKingPosition(color);
			final int kingFile = Square.getFile(kingSquare);
			
			final int shift = (kingFile == File.FD || kingFile == File.FE) ? 0 : 1;
			
			if (kingFile >= File.FD)
				evaluateConcreteKingFiles (position, color, CastlingType.SHORT, shift);

			if (kingFile <= File.FE)
				evaluateConcreteKingFiles (position, color, CastlingType.LONG, shift);
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
				positionalEvaluation.addCoeff(PositionEvaluationCoeffs.QUEEN_MOVE_BONUS, color);
			}
		}
		
		return queenMoveEvaluation;
	}	

	public void writeLog(final PrintWriter writer) {
		bishopColorPositionEvaluator.writeLog(writer);		
		pawnStructureCalculator.writeLog (writer);
	}

}
