package bishop.engine;

import java.io.PrintWriter;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public class GeneralPositionEvaluator  implements IPositionEvaluator {
	private final TablePositionEvaluator[] tablePositionEvaluators;
	private final BishopColorPositionEvaluator[] bishopColorPositionEvaluators;
	private final PawnStructureEvaluator[] pawnStructureEvaluators;
	private final MobilityPositionEvaluator[] mobilityEvaluators;
	private final KingSafetyEvaluator[] kingSafetyEvaluators;

	// Ending only
	private final PawnRaceEvaluator pawnRaceEvaluator;

	// Supplementary
	private Position position;
	
	private PawnStructureEvaluator pawnStructureEvaluator;
	private final IPositionEvaluation evaluation;
	
	private final GeneralEvaluatorSettings settings;
	
	private int gameStage;
	private GameStageFeatures gameStageCoeffs;


	public GeneralPositionEvaluator(final GeneralEvaluatorSettings settings, final PawnStructureCache structureCache, final IPositionEvaluation evaluation) {
		this.settings = settings;
		this.evaluation = evaluation;
		this.tablePositionEvaluators = new TablePositionEvaluator[GameStage.COUNT];
		this.bishopColorPositionEvaluators = new BishopColorPositionEvaluator[GameStage.COUNT];
		this.mobilityEvaluators = new MobilityPositionEvaluator[GameStage.COUNT];
		this.pawnStructureEvaluators = new PawnStructureEvaluator[GameStage.COUNT];
		this.kingSafetyEvaluators = new KingSafetyEvaluator[GameStage.COUNT];
		this.pawnRaceEvaluator = new PawnRaceEvaluator(evaluation);
		
		for (int gameStage = GameStage.FIRST; gameStage < GameStage.LAST; gameStage++) {
			final GameStageFeatures coeffs = PositionEvaluationFeatures.GAME_STAGE_FEATURES.get(gameStage);
			
			tablePositionEvaluators[gameStage] = new TablePositionEvaluator(coeffs.tableEvaluatorFeatures, evaluation);
			bishopColorPositionEvaluators[gameStage] = new BishopColorPositionEvaluator(coeffs, evaluation);
			mobilityEvaluators[gameStage] = new MobilityPositionEvaluator(evaluation);
			pawnStructureEvaluators[gameStage] = new PawnStructureEvaluator(coeffs.pawnStructureFeatures, structureCache, evaluation);
			
			if (gameStage != GameStage.PAWNS_ONLY)
				kingSafetyEvaluators[gameStage] = new KingSafetyEvaluator(coeffs, evaluation);
		}
	}
	
	private void evaluateRooks() {
		// Rooks on open files
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK);
			final long openRooks = rookMask & ~pawnStructureEvaluator.getBackSquares(color);
			
			evaluation.addCoeff(gameStageCoeffs.rookOnOpenFileBonus, color, BitBoard.getSquareCount(openRooks));
		}
	}
	
	private void evaluatePawns(final AttackCalculator attackCalculator) {
		// Rule of square
		if (gameStage == GameStage.PAWNS_ONLY) {
			pawnRaceEvaluator.evaluate(position);
		}
	}
	
	@Override
	public IPositionEvaluation getEvaluation() {
		return evaluation;
	}
	
	@Override
	public void evaluate (final Position position, final AttackCalculator attackCalculator) {
		this.position = position;
		
		selectGameStage();
		
		calculateAttacks(attackCalculator);
		evaluatePawns(attackCalculator);
		
		if (gameStage != GameStage.PAWNS_ONLY) {
			final KingSafetyEvaluator kingSafetyEvaluator = kingSafetyEvaluators[gameStage];
			kingSafetyEvaluator.evaluate(position, attackCalculator);
		}
		
		// Positional evaluation
		pawnStructureEvaluator.calculate(position);
		
		final TablePositionEvaluator tablePositionEvaluator = tablePositionEvaluators[gameStage];
		tablePositionEvaluator.evaluatePosition(position);
		
		if (gameStage != GameStage.PAWNS_ONLY) {
			final BishopColorPositionEvaluator bishopColorPositionEvaluator = bishopColorPositionEvaluators[gameStage];
			bishopColorPositionEvaluator.evaluatePosition(position);
			
			evaluateRooks();
			
			evaluateQueenMove();
			evaluateSecureFigures();
		}
		
		pawnStructureEvaluator.evaluate(position, attackCalculator);
		
		final MobilityPositionEvaluator mobilityEvaluator = mobilityEvaluators[gameStage];
		mobilityEvaluator.evaluatePosition(position, attackCalculator);
	}
	
	private void selectGameStage() {
		gameStage = GameStage.fromMaterial (position.getMaterialHash());
		
		gameStageCoeffs = PositionEvaluationFeatures.GAME_STAGE_FEATURES.get(gameStage);
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
			
			evaluation.addCoeff(gameStageCoeffs.figureOnSecureSquareBonus, color, count);
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
				evaluation.addCoeff(gameStageCoeffs.queenMoveBonus, color);
			}
		}
		
		return queenMoveEvaluation;
	}	

	public void writeLog(final PrintWriter writer) {
	}

}
