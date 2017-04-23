package bishop.evaluationStatistics;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import bishop.base.DefaultAdditiveMaterialEvaluator;
import bishop.base.GameResult;
import bishop.base.IMaterialEvaluator;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.engine.AttackCalculator;
import bishop.engine.CoeffCountPositionEvaluation;
import bishop.engine.CoeffLink;
import bishop.engine.CoeffRegistry;
import bishop.engine.Evaluation;
import bishop.engine.IPositionEvaluation;
import bishop.engine.PositionEvaluationCoeffs;
import bishop.engine.PositionEvaluatorSwitch;
import bishop.engine.PositionEvaluatorSwitchSettings;
import math.EquationSystemSolver;
import math.Utils;

public class CoeffPositionProcessor implements IPositionProcessor {
	
	private static final Map<GameResult, Double> PROBABILITY_RIGHT_SIDES = createProbabilityRightSides();

	private final File coeffFile;
	private final EquationSystemSolver equationSolver = new EquationSystemSolver(PositionEvaluationCoeffs.LAST, 2);
	private final PositionEvaluationCoeffs coeffs = new PositionEvaluationCoeffs();
	private final Supplier<IPositionEvaluation> evaluationFactory = () -> new CoeffCountPositionEvaluation(coeffs);
	private final IMaterialEvaluator materialEvaluator = DefaultAdditiveMaterialEvaluator.getInstance();
	private final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
	
	private final PositionEvaluatorSwitch evaluator = new PositionEvaluatorSwitch(settings, evaluationFactory);
	private final AttackCalculator attackCalculator = new AttackCalculator(evaluationFactory);
	private GameResult result;
 
	
	public CoeffPositionProcessor(final File coeffFile) {
		this.coeffFile = coeffFile;
		
		final CoeffRegistry registry = PositionEvaluationCoeffs.getCoeffRegistry();
		final double[] rightSides = {0.0, 0.0};
		
		for (CoeffLink link: registry.getCoeffLinks()) {
			final double[] equationCoeffs = new double[PositionEvaluationCoeffs.LAST];
			equationCoeffs[link.getCoeff1()] = +1;
			equationCoeffs[link.getCoeff2()] = -1;
			
			equationSolver.addEquation(equationCoeffs, rightSides, link.getWeight());
		}
		
		PositionEvaluationCoeffs.MIDDLE_GAME_TABLE_EVALUATOR_COEFFS.addZeroSumEquation(equationSolver);
		PositionEvaluationCoeffs.ENDING_TABLE_EVALUATOR_COEFFS.addZeroSumEquation(equationSolver);
	}
	
	@Override
	public void newGame(final GameResult result) {
		this.result = result;
	}

	private static Map<GameResult, Double> createProbabilityRightSides() {
		final Map<GameResult, Double> result = new EnumMap<>(GameResult.class);
		result.put(GameResult.WHITE_WINS, +1.0);
		result.put(GameResult.DRAW, 0.0);
		result.put(GameResult.BLACK_WINS, -1.0);
		
		return Collections.unmodifiableMap(result);
	}

	@Override
	public void processPosition(final Position position) {
		if (position.getMaterialHash().isBalancedExceptFor(PieceType.NONE) && PROBABILITY_RIGHT_SIDES.containsKey(result)) {
			final CoeffCountPositionEvaluation evaluation = (CoeffCountPositionEvaluation) evaluationFactory.get();
			
			final IPositionEvaluation tacticalEvaluation = evaluator.evaluateTactical(position, attackCalculator);
			evaluation.addSubEvaluation(tacticalEvaluation);
			
			final IPositionEvaluation positionalEvaluation = evaluator.evaluatePositional(attackCalculator);
			evaluation.addSubEvaluation(positionalEvaluation);

			final double[] equationCoeffs = new double[PositionEvaluationCoeffs.LAST];
			
			for (int i = 0; i < PositionEvaluationCoeffs.LAST; i++) {
				equationCoeffs[i] = evaluation.getCoeffCount(i);
			}
			
			final double probabilityRightSide = PROBABILITY_RIGHT_SIDES.get(result);   // We will update the right side vector later in calculate
			final double evaluationRightSide = -evaluation.getConstantEvaluation();
			
			final double[] rightSide = new double[] {probabilityRightSide, evaluationRightSide};
			
			equationSolver.addEquation(equationCoeffs, rightSide, 1.0);
		}
	}

	@Override
	public void endGame() {
		result = null;
	}

	public void calculate(final MaterialStatisticsPositionProcessor materialProcessor) throws IOException {
		final List<List<Double>> results = equationSolver.solveEquations();
		final double pawnProbability = materialProcessor.getSinglePawnBalance();
		
		final double[] bestCoeffs = IntStream.range(0, equationSolver.getVariableCount())
				.mapToDouble(i -> PieceTypeEvaluations.PAWN_EVALUATION * pawnProbability * results.get(0).get(i).doubleValue() + results.get(1).get(i).doubleValue())
				.toArray();
		
		fixCoeffs(bestCoeffs);
		
		for (int i = 0; i < PositionEvaluationCoeffs.LAST; i++) {
			System.out.println(PositionEvaluationCoeffs.getCoeffRegistry().getName(i) + " " + bestCoeffs[i]);			
		}
	
		for (int i = 0; i < PositionEvaluationCoeffs.LAST; i++)
			coeffs.setEvaluationCoeff(i, Utils.roundToInt(bestCoeffs[i]));
		
		try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(coeffFile))) {
			coeffs.write(stream);
		}
	}

	private void fixCoeffs(final double[] bestCoeffs) {
		bestCoeffs[PositionEvaluationCoeffs.RULE_OF_SQUARE_BONUS] = 5.0 * PieceTypeEvaluations.PAWN_EVALUATION;
	}

}
