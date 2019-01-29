package bishop.evaluationStatistics;

import bishop.base.GameResult;
import bishop.base.Position;
import bishop.engine.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SampleWriter implements IPositionProcessor, Closeable {

	private static final Map<GameResult, Double> PROBABILITY_RIGHT_SIDES = createProbabilityRightSides();

	private final PrintWriter sampleList;
	private final PositionEvaluationCoeffs coeffs = new PositionEvaluationCoeffs();
	private final Supplier<IPositionEvaluation> evaluationFactory = () -> new CoeffCountPositionEvaluation(coeffs);
	private final MobilityCalculator mobilityCalculator = new MobilityCalculator();

	private final PositionEvaluatorSwitch evaluator = new PositionEvaluatorSwitch(evaluationFactory);
	private GameResult result;


	private static Map<GameResult, Double> createProbabilityRightSides() {
		final Map<GameResult, Double> result = new EnumMap<>(GameResult.class);
		result.put(GameResult.WHITE_WINS, 1.0);
		result.put(GameResult.DRAW, 0.5);
		result.put(GameResult.BLACK_WINS, 0.0);

		return Collections.unmodifiableMap(result);
	}

	public SampleWriter(final File sampleListFile) throws IOException {
		this.sampleList = new PrintWriter(sampleListFile);
	}
	
	@Override
	public void newGame(final GameResult result) {
		this.result = result;
	}

	@Override
	public void processPosition(final Position position) {
		if (PROBABILITY_RIGHT_SIDES.containsKey(result)) {
			final CoeffCountPositionEvaluation evaluation = (CoeffCountPositionEvaluation) evaluationFactory.get();
			mobilityCalculator.calculate(position);
			
			final IPositionEvaluation tacticalEvaluation = evaluator.evaluateTactical(position, mobilityCalculator);
			evaluation.addSubEvaluation(tacticalEvaluation);
			
			final IPositionEvaluation positionalEvaluation = evaluator.evaluatePositional();
			evaluation.addSubEvaluation(positionalEvaluation);

			final double probabilityRightSide = PROBABILITY_RIGHT_SIDES.get(result);   // We will update the right side vector later in calculate
			final double evaluationRightSide = -evaluation.getConstantEvaluation();

			sampleList.print(probabilityRightSide);
			sampleList.print(", ");
			sampleList.print(evaluationRightSide);

			final Set<Integer> nonZeroCoeffs = evaluation.getNonZeroCoeffs();

			for (Integer coeff: nonZeroCoeffs) {
				sampleList.print(", ");
				sampleList.print (coeff);
				sampleList.print(", ");
				sampleList.print(evaluation.getCoeffCount(coeff));
			}

			sampleList.println();
		}
	}

	@Override
	public void endGame() {
		result = null;
	}

	public void close() {
		sampleList.close();
	}

}
