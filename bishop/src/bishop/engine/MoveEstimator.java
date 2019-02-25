package bishop.engine;

import java.util.function.Consumer;

import bishop.base.Color;
import bishop.base.GlobalSettings;
import bishop.base.Move;
import bishop.base.PieceType;
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

	private final HistoryTable historyTable = new HistoryTable();
	
	private final OnlineLogisticModel models[][][] = new OnlineLogisticModel[Color.LAST][MAX_KILLER][MAX_CAPTURE_ESTIMATION];
	private static final ConfusionMatrix confusionMatrix = new ConfusionMatrix(2);

	public MoveEstimator() {
		final IErrorAccumulator costAccumulator = (GlobalSettings.isDebug()) ?
				new MeanSquareErrorAccumulator() :
				NullErrorAccumulator.getInstance();

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int killer = 0; killer < MAX_KILLER; killer++) {
				for (int capture = 0; capture < MAX_CAPTURE_ESTIMATION; capture++) 
					models[color][killer][capture] = new OnlineLogisticModel(costAccumulator);
			}
		}

		clear();
	}
	
	private void forEachModel(final Consumer<OnlineLogisticModel> modelConsumer) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int killer = 0; killer < MAX_KILLER; killer++) {
				for (int capture = 0; capture < MAX_CAPTURE_ESTIMATION; capture++) 
					modelConsumer.accept(models[color][killer][capture]);
			}
		}
	}
	
	public int getMoveEstimate(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		final OnlineLogisticModel model = getModel(nodeRecord, color, move);
		
		final int history = historyTable.getEvaluation(color, move);
		final int excitation = (int) (ESTIMATE_MULTIPLIER * model.getExcitation(history));
		final int estimate = Math.max(Math.min(excitation, MAX_ESTIMATE), -MAX_ESTIMATE);

		return estimate;
	}

	public void updateMove(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move, final int horizon, final boolean isBest) {
		confusionMatrix.addSample((isBest) ? 1 : 0, (getMoveEstimate(nodeRecord, color, move) > 0) ? 1 : 0);

		updateModel(nodeRecord, color, move, (isBest) ? 1 : 0);

		if (isBest)
			historyTable.addCutoff(color, move, horizon);
	}

	private void updateModel (final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move, final int estimate) {
		final OnlineLogisticModel model = getModel(nodeRecord, color, move);
		final int history = historyTable.getEvaluation(color, move);
				
		model.addSample(history, estimate);
	}
	
	private OnlineLogisticModel getModel(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		final int killer = (move.equals(nodeRecord.getKillerMove())) ? 1 : 0;
		final int capture = estimateCapture(move.getMovingPieceType(), move.getCapturedPieceType());

		return models[color][killer][capture];
	}

	public void clear() {
		historyTable.clear();
		forEachModel(OnlineLogisticModel::clear);

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int capture = 0; capture < MAX_CAPTURE_ESTIMATION; capture++) {
				models[color][0][capture].setIntercept(0.6);
				models[color][0][capture].setSlope(0.22);
				models[color][1][capture].setIntercept(1.65);
				models[color][1][capture].setSlope(0.31);
			}
		}
	}
		
	private static int estimateCapture (final int movingPieceType, final int capturedPieceType) {
		// King cannot be captured so we changes none to king to have continuous indices.
		final int updatedCapturedPieceType = (capturedPieceType == PieceType.NONE) ? PieceType.KING : capturedPieceType;

		return PieceType.COUNT * movingPieceType + updatedCapturedPieceType;
	}

	public void log() {
		System.out.println("Move estimator confusion matrix");

		confusionMatrix.log();
	}

}
