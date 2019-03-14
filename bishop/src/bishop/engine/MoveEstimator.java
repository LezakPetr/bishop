package bishop.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
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
	private static final ConfusionMatrix confusionMatrix = new ConfusionMatrix(2);

	private static final AtomicInteger sampleWriterId = new AtomicInteger();
	private final PrintWriter sampleWriter;

	public MoveEstimator() {
		sampleWriter = createSampleWriter();

		clear();
	}

	private PrintWriter createSampleWriter() {
		if (GlobalSettings.isDebug()) {
			try {
				final int id = sampleWriterId.getAndIncrement();
				final PrintWriter writer = new PrintWriter("moveEstimator_" + id + ".csv");
				writer.println("depth,horizon,color,history,capturedPieceEvaluation,isKillerMove,isBest");

				return writer;
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}
	
	private void forEachModel(final Consumer<OnlineLogisticModel> modelConsumer) {
	}
	
	public int getMoveEstimate(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		final double history = historyTable.getEvaluation(color, move);
		final int isKiller = (move.equals(nodeRecord.getOriginalKillerMove())) ? 1 : 0;
		final int capturedPieceType = move.getCapturedPieceType();
		final int capturedPieceEvaluation = PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(capturedPieceType);

		final double estimate = ESTIMATE_MULTIPLIER * (
				-1.070e-01 +
				-8.883e-01 * history +
				3.804e-04 * capturedPieceEvaluation +
				1.181e+00 * isKiller +
				6.583e-04 * history * capturedPieceEvaluation
		);

		return (int) estimate;
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

		printSample(nodeRecord, color, move, horizon, estimate);
	}

	private void printSample(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move, final int horizon, final int estimate) {
		if (sampleWriter != null) {
			sampleWriter.print(nodeRecord.getDepth());
			sampleWriter.print(",");
			sampleWriter.print(horizon);
			sampleWriter.print(",");
			sampleWriter.print(Color.getNotation(color));
			sampleWriter.print(",");

			final double history = historyTable.getEvaluation(color, move);

			sampleWriter.print(history);
			sampleWriter.print(",");

			final int capturedPieceType = move.getCapturedPieceType();
			final int capturedPieceEvaluation = PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(capturedPieceType);

			sampleWriter.print(capturedPieceEvaluation);
			sampleWriter.print(",");

			sampleWriter.print(move.equals(nodeRecord.getOriginalKillerMove()));
			sampleWriter.print(",");

			sampleWriter.println(estimate);
		}
	}

	public void clear() {
		historyTable.clear();
		forEachModel(OnlineLogisticModel::clear);
	}

	public void log() {
		System.out.println("Move estimator confusion matrix");

		confusionMatrix.log();
	}

}
