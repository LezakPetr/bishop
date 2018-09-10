package bishop.evaluationStatistics;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import bishop.base.*;
import bishop.engine.*;
import math.*;
import math.Utils;
import regression.*;

public class CoeffPositionProcessor implements IPositionProcessor {

	private static final double REGRESSION_LAMBDA = 1e-2;   // Regularization parameter
	private static final int MIN_NON_EXCHANGE_COUNTER = 4;
	private static final int MAX_MATERIAL_EVALUATION = PieceTypeEvaluations.getPawnMultiply(1.5);

	private static final Map<GameResult, Float> PROBABILITY_RIGHT_SIDES = createProbabilityRightSides();

	private final File coeffFile;
	private final LogisticRegression regression;
	private final PositionEvaluationCoeffs coeffs = new PositionEvaluationCoeffs();
	private final Supplier<IPositionEvaluation> evaluationFactory = () -> new CoeffCountPositionEvaluation(coeffs);
	private final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
	
	private final PositionEvaluatorSwitch evaluator = new PositionEvaluatorSwitch(settings, evaluationFactory);
	private final AttackCalculator attackCalculator = new AttackCalculator();
	private final Random rng = new Random();
	private final double positionTakeProbability = 1;
	private final IMaterialEvaluator defaultMaterialEvaluator = new DefaultAdditiveMaterialEvaluator(PieceTypeEvaluations.DEFAULT);
	private int sampleCount;
	private long memoryConsumption;
	private GameResult result;

	private int nonExchangeCounter;
	private int lastPieceCount;


	public CoeffPositionProcessor(final File coeffFile) {
		this.coeffFile = coeffFile;

		final List<Integer> regularizedFeatures = new ArrayList<>();

		for (int i = 0; i < PositionEvaluationCoeffs.LAST; i++)
			regularizedFeatures.add(i);

		final DirectFeatureCombination featureCombination = new DirectFeatureCombination(EvaluationSample.FEATURE_COUNT);

		regression = new LogisticRegression(EvaluationSample.FEATURE_COUNT, 0, featureCombination, regularizedFeatures);
		regression.setLambda(REGRESSION_LAMBDA);
	}
	
	@Override
	public void newGame(final GameResult result) {
		this.result = result;
		this.nonExchangeCounter = 0;
		this.lastPieceCount = 32;
	}

	private static Map<GameResult, Float> createProbabilityRightSides() {
		final Map<GameResult, Float> result = new EnumMap<>(GameResult.class);
		result.put(GameResult.WHITE_WINS, +1.0f);
		result.put(GameResult.DRAW, 0.5f);
		result.put(GameResult.BLACK_WINS, 0.0f);
		
		return Collections.unmodifiableMap(result);
	}

	@Override
	public void processPosition(final Position position) {
		final int pieceCount = BitBoard.getSquareCount(position.getOccupancy());

		if (pieceCount == lastPieceCount)
			nonExchangeCounter++;
		else
			nonExchangeCounter = 0;

		lastPieceCount = pieceCount;

		if (rng.nextDouble() <= positionTakeProbability &&
				nonExchangeCounter >= MIN_NON_EXCHANGE_COUNTER &&
				PROBABILITY_RIGHT_SIDES.containsKey(result) &&
				Math.abs(defaultMaterialEvaluator.evaluateMaterial(position.calculateMaterialHash())) <= MAX_MATERIAL_EVALUATION &&
				position.getStaticExchangeEvaluationOnTurn(PieceTypeEvaluations.DEFAULT) == 0) {
			final CoeffCountPositionEvaluation evaluation = (CoeffCountPositionEvaluation) evaluationFactory.get();
			
			final IPositionEvaluation tacticalEvaluation = evaluator.evaluateTactical(position, attackCalculator);
			evaluation.addSubEvaluation(tacticalEvaluation);
			
			final IPositionEvaluation positionalEvaluation = evaluator.evaluatePositional(attackCalculator);
			evaluation.addSubEvaluation(positionalEvaluation);

			if (evaluation.getConstantEvaluation() == 0) {
				final Set<Integer> nonZeroCoeffs = evaluation.getNonZeroCoeffs();
				final int nonZeroCoeffCount = nonZeroCoeffs.size();
				final int[] coeffs = new int[nonZeroCoeffCount];
				final int[] coeffCounts = new int[nonZeroCoeffCount];

				int index = 0;

				for (int coeff: nonZeroCoeffs) {
					coeffs[index] = coeff;

					final int count = evaluation.getCoeffCountRaw(coeff);
					coeffCounts[index] = count;

					index++;
				}

				final float probabilityRightSide = PROBABILITY_RIGHT_SIDES.get(result);   // We will update the right side vector later in calculate
				final EvaluationSample sample = new EvaluationSample(
						coeffs,
						coeffCounts,
						position.getMaterialHash(),
						probabilityRightSide
				);

				regression.addSamples(Collections.singletonList(sample));
				memoryConsumption += sample.estimateMemoryConsumption();
				sampleCount++;
			}
		}
	}

	@Override
	public void endGame() {
		result = null;
	}

	private double[] calculateMaterialProbabilities(final IVectorRead results) {
		final double[] materialEvaluations = new double[PieceType.LAST];

		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++)
			materialEvaluations[pieceType] = results.getElement(EvaluationSample.getIndexOfPieceTypeFeature(pieceType));

		return materialEvaluations;
	}

	public void calculate() throws IOException {
		System.out.println ("Sample count = " + sampleCount + ", memory consumption: " + memoryConsumption);
		final IVectorRead results = regression.optimize();

		final double[] materialProbabilities = calculateMaterialProbabilities(results);
		final double pawnProbability = materialProbabilities[PieceType.PAWN];

		final double[] materialEvaluations = IntStream.range(0, materialProbabilities.length)
				.mapToDouble(i -> PieceTypeEvaluations.PAWN_EVALUATION / pawnProbability * materialProbabilities[i])
				.toArray();
		
		final double[] bestCoeffs = IntStream.range(0, results.getDimension())
				.mapToDouble(i -> PieceTypeEvaluations.PAWN_EVALUATION / pawnProbability * results.getElement(i))
				.toArray();

		for (int i = 0; i < PositionEvaluationCoeffs.LAST; i++) {
			System.out.println(PositionEvaluationCoeffs.getCoeffRegistry().getName(i) + " " + bestCoeffs[i]);			
		}

		final int[] pieceTypeEvaluations = Arrays.stream(materialEvaluations)
				.mapToInt(Utils::roundToInt)
				.toArray();

		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++)
			System.out.println(PieceType.getName(pieceType) + " = " + materialEvaluations[pieceType]);
	
		for (int i = 0; i < PositionEvaluationCoeffs.LAST; i++)
			coeffs.setEvaluationCoeff(i, Utils.roundToInt(bestCoeffs[i]));

		coeffs.setPieceTypeEvaluations(PieceTypeEvaluations.of(pieceTypeEvaluations));
		
		try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(coeffFile))) {
			coeffs.write(stream);
		}
	}
}
