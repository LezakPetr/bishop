package bishop.engine;

import java.util.Arrays;
import java.util.function.Consumer;

import bishop.base.*;
import math.*;

/**
 * Estimator that estimates the moves to order them in alpha-beta search.
 * This class maintains linear models of history evaluation -> probability of cutoff
 * for every combination of color, killer heuristic, moving and captured piece type.
 *  
 * @author Ing. Petr Ležák
 */
public class MoveEstimator {

	private static final int MAX_KILLER = 2;
	private static final int MAX_CAPTURE_ESTIMATION = PieceType.COUNT * PieceType.COUNT;

	public static final int ESTIMATE_MULTIPLIER = 1_000_000;
	public static final int MAX_ESTIMATE = 1_000_000_000;
	
	private final OnlineLogisticModel models[] = new OnlineLogisticModel[Color.LAST];
	private static final ConfusionMatrix confusionMatrix = new ConfusionMatrix(2);

	private static final int FEATURE_OFFSET_BEGIN_SQUARE = 0;
	private static final int FEATURE_OFFSET_TARGET_SQUARE = FEATURE_OFFSET_BEGIN_SQUARE + Square.LAST;
	private static final int FEATURE_OFFSET_MOVING_PIECE_TYPE = FEATURE_OFFSET_TARGET_SQUARE + Square.LAST;
	private static final int FEATURE_OFFSET_CAPTURED_PIECE_TYPE = FEATURE_OFFSET_MOVING_PIECE_TYPE + PieceType.LAST;
	private static final int FEATURE_OFFSET_HISTORY = FEATURE_OFFSET_CAPTURED_PIECE_TYPE + PieceType.LAST * Square.LAST;
	private static final int FEATURE_KILLER_MOVE = FEATURE_OFFSET_HISTORY + PieceType.LAST;
	private static final int FEATURE_COUNT = FEATURE_KILLER_MOVE + 1;

	private static final int FEATURE_INDEX_COUNT = 6;

	private final int[] featureIndices = new int[FEATURE_INDEX_COUNT];
	private final double[] featureValues = new double[FEATURE_INDEX_COUNT];

	public MoveEstimator() {
		final IErrorAccumulator costAccumulator = (GlobalSettings.isDebug()) ?
				new MeanSquareErrorAccumulator() :
				NullErrorAccumulator.getInstance();

		for (int color = Color.FIRST; color < Color.LAST; color++)
			models[color] = new OnlineLogisticModel(FEATURE_COUNT, costAccumulator);

		Arrays.fill(featureValues, 1.0);

		clear();
	}
	
	private void forEachModel(final Consumer<OnlineLogisticModel> modelConsumer) {
		for (int color = Color.FIRST; color < Color.LAST; color++)
			modelConsumer.accept(models[color]);
	}
	
	public int getMoveEstimate(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		final OnlineLogisticModel model = getModel(nodeRecord, color, move);
		fillFeatureIndices(nodeRecord, move);

		final int excitation = (int) (ESTIMATE_MULTIPLIER * model.getExcitation(featureIndices, featureValues));
		final int estimate = Math.max(Math.min(excitation, MAX_ESTIMATE), -MAX_ESTIMATE);

		return estimate;
	}

	public void updateMove(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move, final int horizon, final boolean isBest) {
		confusionMatrix.addSample((isBest) ? 1 : 0, (getMoveEstimate(nodeRecord, color, move) > 0) ? 1 : 0);

		updateModel(nodeRecord, color, move, horizon, (isBest) ? 1 : 0);
	}

	private void updateModel (final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move, final int horizon, final int estimate) {
		final OnlineLogisticModel model = getModel(nodeRecord, color, move);
		fillFeatureIndices(nodeRecord, move);
				
		model.addSample(featureIndices, featureValues, estimate, horizon * horizon);
	}

	private void fillFeatureIndices (final SerialSearchEngine.NodeRecord nodeRecord, final Move move) {
		final int beginSquare = move.getBeginSquare();
		final int targetSquare = move.getTargetSquare();

		// King cannot be captured so we changes none to king to have continuous indices.
		final int capturedPieceType = move.getCapturedPieceType();
		final int updatedCapturedPieceType = (capturedPieceType == PieceType.NONE) ? PieceType.KING : capturedPieceType;

		featureIndices[0] = FEATURE_OFFSET_BEGIN_SQUARE + beginSquare;
		featureIndices[1] = FEATURE_OFFSET_TARGET_SQUARE + targetSquare;
		featureIndices[2] = FEATURE_OFFSET_MOVING_PIECE_TYPE + move.getMovingPieceType();
		featureIndices[3] = FEATURE_OFFSET_CAPTURED_PIECE_TYPE + updatedCapturedPieceType;
		featureIndices[4] = FEATURE_OFFSET_HISTORY + targetSquare + move.getMovingPieceType() * Square.LAST;
		featureIndices[5] = FEATURE_KILLER_MOVE;
		featureValues[5] = (nodeRecord.getKillerMove().equals(move)) ? 1 : 0;
	}
	
	private OnlineLogisticModel getModel(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		return models[color];
	}

	public void clear() {
		forEachModel(OnlineLogisticModel::clear);

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			models[color].setIntercept(0.5);

			for (int i = 0; i < FEATURE_COUNT; i++)
				models[color].setSlope(i, 0.1);

			models[color].setSlope(FEATURE_KILLER_MOVE, 1.0);
		}
	}

	public void log() {
		System.out.println("Move estimator confusion matrix");

		confusionMatrix.log();
	}

}
