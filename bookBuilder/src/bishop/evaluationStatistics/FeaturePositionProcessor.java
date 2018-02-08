package bishop.evaluationStatistics;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import bishop.base.Color;
import bishop.base.GameResult;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.engine.AttackCalculator;
import bishop.engine.FeatureCountPositionEvaluation;
import bishop.engine.IPositionEvaluation;
import bishop.engine.PositionEvaluationFeatures;
import bishop.engine.PositionEvaluatorSwitch;
import bishop.engine.PositionEvaluatorSwitchSettings;
import neural.FastSigmoidActivationFunction;
import neural.IActivationFunction;
import neural.LearningPerceptronNetwork;
import neural.Optimizer;
import neural.PerceptronLayerSettings;
import neural.PerceptronNetworkSettings;
import neural.Sample;
import neural.TanhActivationFunction;

public class FeaturePositionProcessor implements IPositionProcessor {
	
	private final Optimizer optimizer;
	
	private static final Map<GameResult, Float> NORMALIZED_OUTPUTS = createNormalizedOutputs();
 
	private final File featureFile;
	private final Supplier<IPositionEvaluation> evaluationFactory = () -> new FeatureCountPositionEvaluation(new PositionEvaluationFeatures());
	private final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
	
	private final PositionEvaluatorSwitch evaluator = new PositionEvaluatorSwitch(settings, evaluationFactory.get());
	private final AttackCalculator attackCalculator = new AttackCalculator();
	private GameResult result;
 
	
	public FeaturePositionProcessor(final File featureFile) {
		this.featureFile = featureFile;
		
		final int[] layerSizes = { PositionEvaluationFeatures.LAST, 20, 10, 1 };

		final LearningPerceptronNetwork network = LearningPerceptronNetwork.create(FeaturePositionProcessor::activationFunctionSupplier, layerSizes);
		optimizer = new Optimizer();
		optimizer.setNetwork(network);
		optimizer.setInitialAlpha(0.01f);
		optimizer.setAlphaDropdown(5.0f);
	}
	
	private static IActivationFunction activationFunctionSupplier (final Integer layerIndex, final Integer layerCount) {
		if ((int) layerIndex == layerCount - 1)
			return TanhActivationFunction.getInstance();
		else
			return FastSigmoidActivationFunction.getInstance();
	}
	
	@Override
	public void newGame(final GameResult result) {
		this.result = result;
	}

	private static Map<GameResult, Float> createNormalizedOutputs() {
		final Map<GameResult, Float> result = new EnumMap<>(GameResult.class);
		result.put(GameResult.WHITE_WINS, +1.0f);
		result.put(GameResult.DRAW, 0.0f);
		result.put(GameResult.BLACK_WINS, -1.0f);
		
		return Collections.unmodifiableMap(result);
	}

	@Override
	public void processPosition(final Position position) {
		if (position.getMaterialHash().isBalancedExceptFor(PieceType.NONE) && NORMALIZED_OUTPUTS.containsKey(result)) {
			final FeatureCountPositionEvaluation evaluation = (FeatureCountPositionEvaluation) evaluator.getEvaluation();
			
			evaluation.clear(position.getOnTurn());
			evaluator.evaluate(position, attackCalculator);
	
			final int featureCount = evaluation.genNonZeroFeatureCount();
			
			final int[] inputIndices = new int[featureCount];
			final float[] inputValues = new float[featureCount];
			final float colorCoeff = (position.getOnTurn() == Color.WHITE) ? 1 : -1;
			
			for (int i = 0; i < featureCount; i++) {
				final int feature = evaluation.getNonZeroFeatureAt(i);
				inputIndices[i] = feature;
				inputValues[i] = (float) evaluation.getFeatureCount(feature) * colorCoeff;
			}
			
			final float output = NORMALIZED_OUTPUTS.get(result) * colorCoeff;
			optimizer.addSample(new EvaluationSample(evaluation, colorCoeff, output));
		}
	}

	@Override
	public void endGame() {
		result = null;
	}

	public void calculate(final MaterialStatisticsPositionProcessor materialProcessor) throws IOException {
		optimizer.learn();
		multiplyLastLayer(materialProcessor);
		
		try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(featureFile))) {
			optimizer.getNetwork().getSettings().write (stream);
		}
	}
	
	public void multiplyLastLayer(final MaterialStatisticsPositionProcessor materialProcessor) {
		final double pawnProbability = materialProcessor.getSinglePawnBalance();
		final PerceptronNetworkSettings networkSettings = optimizer.getNetwork().getSettings();
		final PerceptronLayerSettings lastLayerSettings = networkSettings.getInnerLayerAt(networkSettings.getInnerLayerCount() - 1);
		final double coeff = PieceTypeEvaluations.PAWN_EVALUATION * pawnProbability;
		
		lastLayerSettings.multiply((float) coeff);
	}

}
