package bishop.engine;

import java.util.Arrays;
import java.util.function.Consumer;

import bishop.base.*;
import math.*;
import utils.IntHolder;

/**
 * Estimator that estimates the moves to order them in alpha-beta search.
 * This class maintains linear models of history evaluation -> probability of cutoff
 * for every combination of color, killer heuristic, moving and captured piece type.
 *  
 * @author Ing. Petr Ležák
 */
public class MoveEstimator {

	public static final int ESTIMATE_MULTIPLIER = 1_000_000;
	public static final int MAX_ESTIMATE = 1_000_000_000;

	private final HistoryTable historyTable = new HistoryTable();
	private final OnlineLogisticModel models[] = new OnlineLogisticModel[Color.LAST];
	private static final ConfusionMatrix confusionMatrix = new ConfusionMatrix(2);

	private static final utils.IntHolder FEATURE_OFFSET = new IntHolder();

	private static final int FEATURE_OFFSET_BEGIN_SQUARE = FEATURE_OFFSET.getAndAdd(Square.LAST);
	private static final int FEATURE_OFFSET_TARGET_SQUARE = FEATURE_OFFSET.getAndAdd(Square.LAST);
	private static final int FEATURE_OFFSET_MOVING_PIECE_TYPE = FEATURE_OFFSET.getAndAdd(PieceType.LAST);
	private static final int FEATURE_OFFSET_CAPTURED_PIECE_TYPE = FEATURE_OFFSET.getAndAdd(PieceType.LAST);
	private static final int FEATURE_OFFSET_ALL = FEATURE_OFFSET.getAndAdd(2 * PieceType.LAST * PieceType.LAST);
	private static final int FEATURE_OFFSET_ALL_HISTORY = FEATURE_OFFSET.getAndAdd(2 * PieceType.LAST * PieceType.LAST);

	private static final int FEATURE_OFFSET_HISTORY = FEATURE_OFFSET.getAndAdd(1);
	private static final int FEATURE_KILLER_MOVE = FEATURE_OFFSET.getAndAdd(1);
	private static final int FEATURE_PRINCIPAL_MOVE = FEATURE_OFFSET.getAndAdd(1);
	private static final int FEATURE_COUNT = FEATURE_OFFSET.getValue();

	private static final int FEATURE_INDEX_COUNT = 9;

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
		fillFeatureIndices(nodeRecord, color, move);

		final int excitation = (int) (ESTIMATE_MULTIPLIER * model.getExcitation(featureIndices, featureValues));
		final int estimate = Math.max(Math.min(excitation, MAX_ESTIMATE), -MAX_ESTIMATE);

		return estimate;
	}

	public void updateMove(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move, final int horizon, final boolean isBest) {
		confusionMatrix.addSample((isBest) ? 1 : 0, (getMoveEstimate(nodeRecord, color, move) > 0) ? 1 : 0);

		updateModel(nodeRecord, color, move, horizon, (isBest) ? 1 : 0);

		if (isBest)
			historyTable.addCutoff(color, move, horizon);
	}

	private void updateModel (final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move, final int horizon, final int estimate) {
		if (horizon <= 0)
			return;

		final OnlineLogisticModel model = getModel(nodeRecord, color, move);
		model.setGamma(1e-5);
		model.setLambda(1e-2);
		fillFeatureIndices(nodeRecord, color, move);

		model.addSample(featureIndices, featureValues, estimate, horizon * horizon * horizon);
	}

	private void fillFeatureIndices (final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		final int beginSquare = move.getBeginSquare();
		final int targetSquare = move.getTargetSquare();

		// King cannot be captured so we changes none to king to have continuous indices.
		final int capturedPieceType = move.getCapturedPieceType();
		final int updatedCapturedPieceType = (capturedPieceType == PieceType.NONE) ? PieceType.KING : capturedPieceType;
		final int movingPieceType = move.getMovingPieceType();
		final double history = historyTable.getEvaluation(color, move);
		final int isKiller = (move.equals(nodeRecord.getKillerMove())) ? 1 : 0;
		final int historyIndex = movingPieceType + updatedCapturedPieceType * PieceType.LAST + isKiller * PieceType.LAST * PieceType.LAST;

		featureIndices[0] = FEATURE_OFFSET_BEGIN_SQUARE + beginSquare;
		featureIndices[1] = FEATURE_OFFSET_TARGET_SQUARE + targetSquare;
		featureIndices[2] = FEATURE_OFFSET_MOVING_PIECE_TYPE + movingPieceType;
		featureIndices[3] = FEATURE_OFFSET_CAPTURED_PIECE_TYPE + updatedCapturedPieceType;

		featureIndices[4] = FEATURE_OFFSET_ALL + historyIndex;

		featureIndices[5] = FEATURE_OFFSET_ALL_HISTORY + historyIndex;
		featureValues[5] = history;

		featureIndices[6] = FEATURE_OFFSET_HISTORY;
		featureValues[6] = history;

		featureIndices[7] = FEATURE_KILLER_MOVE;
		featureValues[7] = isKiller;

		featureIndices[8] = FEATURE_PRINCIPAL_MOVE;
		featureValues[8] = (move.equals(nodeRecord.getPrincipalMove())) ? 1 : 0;
	}
	
	private OnlineLogisticModel getModel(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		return models[color];
	}

	public void clear() {
		historyTable.clear();
		forEachModel(OnlineLogisticModel::clear);

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			models[color].setIntercept(0.5);

			for (int i = 0; i < FEATURE_COUNT; i++)
				models[color].setSlope(i, 0.1);

			models[color].setSlope(FEATURE_KILLER_MOVE, 1.0);
			models[color].setSlope(FEATURE_PRINCIPAL_MOVE, 1.0);
		}
	}

	public void log() {
		System.out.println("Move estimator confusion matrix");

		confusionMatrix.log();
	}

}
