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
	private final PawnStructureEvaluator[] pawnStructureEvaluators;
	private final MobilityPositionEvaluator[] mobilityEvaluators;
	private final KingSafetyEvaluator[] kingSafetyEvaluators;

	// Ending only
	private final PawnRaceEvaluator pawnRaceEvaluator;

	// Supplementary
	private Position position;
	
	private PawnStructureEvaluator pawnStructureEvaluator;
	private final IPositionEvaluation tacticalEvaluation;
	private final IPositionEvaluation positionalEvaluation;
	
	private final GeneralEvaluatorSettings settings;
	
	private int gameStage;
	private GameStageCoeffs gameStageCoeffs;


	public GeneralPositionEvaluator(final GeneralEvaluatorSettings settings, final PawnStructureCache structureCache, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.settings = settings;
		this.tacticalEvaluation = evaluationFactory.get();
		this.positionalEvaluation = evaluationFactory.get();

		if (positionalEvaluation instanceof CoeffCountPositionEvaluation)
			this.tablePositionEvaluator = new GameStageTablePositionEvaluator(PositionEvaluationCoeffs.TABLE_EVALUATOR_COEFFS, evaluationFactory);
		else
			this.tablePositionEvaluator = new IterativeGameStageTablePositionEvaluator(evaluationFactory);

		this.bishopColorPositionEvaluators = new BishopColorPositionEvaluator[GameStage.COUNT];
		this.mobilityEvaluators = new MobilityPositionEvaluator[GameStage.COUNT];
		this.pawnStructureEvaluators = new PawnStructureEvaluator[GameStage.COUNT];
		this.kingSafetyEvaluators = new KingSafetyEvaluator[GameStage.COUNT];
		this.pawnRaceEvaluator = new PawnRaceEvaluator(evaluationFactory);
		
		for (int gameStage = GameStage.FIRST; gameStage < GameStage.LAST; gameStage++) {
			final GameStageCoeffs coeffs = PositionEvaluationCoeffs.GAME_STAGE_COEFFS.get(gameStage);

			bishopColorPositionEvaluators[gameStage] = new BishopColorPositionEvaluator(coeffs, evaluationFactory);
			mobilityEvaluators[gameStage] = new MobilityPositionEvaluator(evaluationFactory);
			pawnStructureEvaluators[gameStage] = new PawnStructureEvaluator(coeffs.pawnStructureCoeffs, structureCache, evaluationFactory);
			
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
	
	private void evaluatePawns(final AttackCalculator attackCalculator) {
		// Rule of square
		if (gameStage == GameStage.PAWNS_ONLY) {
			tacticalEvaluation.addSubEvaluation(pawnRaceEvaluator.evaluate(position));
		}
	}
	
	@Override
	public IPositionEvaluation evaluateTactical (final Position position, final AttackCalculator attackCalculator) {
		this.position = position;
		
		selectGameStage();
		
		clear();
		calculateAttacks(attackCalculator);
		evaluatePawns(attackCalculator);
		
		if (gameStage != GameStage.PAWNS_ONLY) {
			final KingSafetyEvaluator kingSafetyEvaluator = kingSafetyEvaluators[gameStage];
			tacticalEvaluation.addSubEvaluation(kingSafetyEvaluator.evaluate(position, attackCalculator));
		}
		
		return tacticalEvaluation;
	}

	@Override
	public IPositionEvaluation evaluatePositional (final AttackCalculator attackCalculator) {
		pawnStructureEvaluator.calculate(position);

		positionalEvaluation.addSubEvaluation(tablePositionEvaluator.evaluate(position, gameStage));
		
		if (gameStage != GameStage.PAWNS_ONLY) {
			final BishopColorPositionEvaluator bishopColorPositionEvaluator = bishopColorPositionEvaluators[gameStage];
			positionalEvaluation.addSubEvaluation(bishopColorPositionEvaluator.evaluatePosition(position));
			
			evaluateRooks();
			
			evaluateQueenMove();
			evaluateSecureFigures();
		}
		
		positionalEvaluation.addSubEvaluation(pawnStructureEvaluator.evaluate(position, attackCalculator));
		
		final MobilityPositionEvaluator mobilityEvaluator = mobilityEvaluators[gameStage];
		positionalEvaluation.addSubEvaluation(mobilityEvaluator.evaluatePosition(position, attackCalculator));
		
		positionalEvaluation.addCoeff(gameStageCoeffs.onTurnBonus, position.getOnTurn());

		return positionalEvaluation;
	}
	
	private void selectGameStage() {
		gameStage = GameStage.fromMaterial (position.getMaterialHash());
		
		gameStageCoeffs = PositionEvaluationCoeffs.GAME_STAGE_COEFFS.get(gameStage);
		pawnStructureEvaluator = pawnStructureEvaluators[gameStage];
	}
	
	private void calculateAttacks(final AttackCalculator attackCalculator) {
		final int whiteKingSquare = position.getKingPosition(Color.WHITE);
		final int blackKingSquare = position.getKingPosition(Color.BLACK);
		
		attackCalculator.calculate(position, settings.getAttackTableGroup(whiteKingSquare, blackKingSquare));
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
