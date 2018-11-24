package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public class GeneralPositionEvaluator  implements IPositionEvaluator {
	private final IGameStageTablePositionEvaluator tablePositionEvaluator;
	private final BishopColorPositionEvaluator[] bishopColorPositionEvaluators;
	private final MobilityPositionEvaluator mobilityEvaluator;
	private final KingSafetyEvaluator[] kingSafetyEvaluators;

	// Ending only
	private final PawnRaceEvaluator pawnRaceEvaluator;

	// Supplementary
	private Position position;
	
	private final PawnStructureEvaluator pawnStructureEvaluator;
	private final IPositionEvaluation tacticalEvaluation;
	private final IPositionEvaluation positionalEvaluation;
	private final AttackCalculator attackCalculator = new AttackCalculator();
	
	private final GeneralEvaluatorSettings settings;
	
	private int gameStage;
	private GameStageCoeffs gameStageCoeffs;


	public GeneralPositionEvaluator(final GeneralEvaluatorSettings settings, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.settings = settings;
		this.tacticalEvaluation = evaluationFactory.get();
		this.positionalEvaluation = evaluationFactory.get();

		if (positionalEvaluation instanceof CoeffCountPositionEvaluation)
			this.tablePositionEvaluator = new GameStageTablePositionEvaluator(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS, evaluationFactory);
		else
			this.tablePositionEvaluator = new IterativeGameStageTablePositionEvaluator(evaluationFactory);

		this.bishopColorPositionEvaluators = new BishopColorPositionEvaluator[GameStage.COUNT];
		this.mobilityEvaluator = new MobilityPositionEvaluator(evaluationFactory);
		this.kingSafetyEvaluators = new KingSafetyEvaluator[GameStage.COUNT];
		this.pawnRaceEvaluator = new PawnRaceEvaluator(evaluationFactory);
		this.pawnStructureEvaluator = new PawnStructureEvaluator(evaluationFactory);
		
		for (int gameStage = GameStage.FIRST; gameStage < GameStage.LAST; gameStage++) {
			final GameStageCoeffs coeffs = PositionEvaluationCoeffs.GAME_STAGE_COEFFS.get(gameStage);

			bishopColorPositionEvaluators[gameStage] = new BishopColorPositionEvaluator(coeffs, evaluationFactory);
			
			if (gameStage != GameStage.PAWNS_ONLY)
				kingSafetyEvaluators[gameStage] = new KingSafetyEvaluator(coeffs, evaluationFactory);
		}
	}

	private void clear() {
		tacticalEvaluation.clear();
		positionalEvaluation.clear();
		pawnStructureEvaluator.clear();
	}
	
	private void evaluateRooks() {
		// Rooks on open files
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK);
			final long openRooks = rookMask & ~pawnStructureEvaluator.getBackSquares(color);
			
			positionalEvaluation.addCoeff(gameStageCoeffs.rookOnOpenFileBonus, color, BitBoard.getSquareCount(openRooks));
		}
	}
	
	private void evaluatePawns() {
		// Rule of square
		if (gameStage == GameStage.PAWNS_ONLY) {
			tacticalEvaluation.addSubEvaluation(pawnRaceEvaluator.evaluate(position));
		}
	}
	
	@Override
	public IPositionEvaluation evaluateTactical(final Position position, final MobilityCalculator mobilityCalculator) {
		this.position = position;
		
		selectGameStage();
		
		clear();
		calculateAttacks(mobilityCalculator);
		evaluatePawns();
		
		if (gameStage != GameStage.PAWNS_ONLY) {
			final KingSafetyEvaluator kingSafetyEvaluator = kingSafetyEvaluators[gameStage];
			tacticalEvaluation.addSubEvaluation(kingSafetyEvaluator.evaluate(position, attackCalculator));
		}
		
		return tacticalEvaluation;
	}

	@Override
	public IPositionEvaluation evaluatePositional() {
		pawnStructureEvaluator.calculate(position);
		positionalEvaluation.addSubEvaluation(tablePositionEvaluator.evaluate(position, gameStage));
		
		if (gameStage != GameStage.PAWNS_ONLY) {
			final BishopColorPositionEvaluator bishopColorPositionEvaluator = bishopColorPositionEvaluators[gameStage];
			positionalEvaluation.addSubEvaluation(bishopColorPositionEvaluator.evaluatePosition(position));
			
			evaluateRooks();
			
			evaluateQueenMove();
			evaluateSecureFigures();
		}
		
		positionalEvaluation.addSubEvaluation(pawnStructureEvaluator.evaluate(position, gameStage));

		positionalEvaluation.addSubEvaluation(mobilityEvaluator.evaluatePosition(position, attackCalculator, gameStage));
		
		positionalEvaluation.addCoeff(gameStageCoeffs.onTurnBonus, position.getOnTurn());

		return positionalEvaluation;
	}
	
	private void selectGameStage() {
		gameStage = position.getGameStage();
		
		gameStageCoeffs = PositionEvaluationCoeffs.GAME_STAGE_COEFFS.get(gameStage);
	}
	
	private void calculateAttacks(final MobilityCalculator mobilityCalculator) {
		final int whiteKingSquare = position.getKingPosition(Color.WHITE);
		final int blackKingSquare = position.getKingPosition(Color.BLACK);
		
		attackCalculator.calculate(position, settings.getAttackTableGroup(whiteKingSquare, blackKingSquare), mobilityCalculator);
	}
	
	private void evaluateSecureFigures() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			long figures = BitBoard.EMPTY;
			
			for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++)
				figures |= position.getPiecesMask(color, pieceType);
			
			final long securedFigures = figures & pawnStructureEvaluator.getSecureSquares(color);
			final int count = BitBoard.getSquareCount(securedFigures);
			
			positionalEvaluation.addCoeff(gameStageCoeffs.figureOnSecureSquareBonus, color, count);
		}
	}
	
	private int evaluateQueenMove() {
		int queenMoveEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long figureMask = position.getPiecesMask(color, PieceType.BISHOP) | position.getPiecesMask(color, PieceType.KNIGHT);
			final long queenMask = position.getPiecesMask(color, PieceType.QUEEN);
			final long firstRankMask = BoardConstants.getFirstRankMask (color);
			final long figuresOnFirstRank = figureMask & firstRankMask;
			
			if (figuresOnFirstRank != 0 && (queenMask & ~firstRankMask) != 0) {
				positionalEvaluation.addCoeff(gameStageCoeffs.queenMoveBonus, color);
			}
		}
		
		return queenMoveEvaluation;
	}

	public void writeLog(final PrintWriter writer) {
	}

}
