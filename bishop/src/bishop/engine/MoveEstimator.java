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
		this.sampleWriter = createSampleWriter();

		clear();
	}

	private PrintWriter createSampleWriter() {
		if (GlobalSettings.isDebug()) {
			try {
				final int id = sampleWriterId.getAndIncrement();
				final PrintWriter writer = new PrintWriter("moveEstimator_" + id + ".csv");
				writer.println("depth,horizon,color,history,capturedPieceEvaluation,lostMovingPieceEvaluation,isKillerMove,isBest");

				return writer;
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}

	public int getMoveEstimate(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		final double history = historyTable.getEvaluation(color, move);
		final int isKiller = (move.equals(nodeRecord.getOriginalKillerMove())) ? 1 : 0;
		final int capturedPieceEvaluation = getCapturedPieceEvaluation(move);

		final int lostMovingPieceEvaluation = getLostMovingPieceEvaluation(nodeRecord.getMobilityCalculator(), color, move);

		final double estimate = ESTIMATE_MULTIPLIER * (
				-1.590e-01 +
				3.016e+00 * history +
				3.901e-04 * capturedPieceEvaluation +
				-1.663e-04 * lostMovingPieceEvaluation +
				1.134e+00 * isKiller +
				-1.459e-04 * history * capturedPieceEvaluation +
				-9.947e-05 * history * lostMovingPieceEvaluation
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

			final int capturedPieceEvaluation = getCapturedPieceEvaluation(move);

			sampleWriter.print(capturedPieceEvaluation);
			sampleWriter.print(",");

			final int lostMovingPieceEvaluation = getLostMovingPieceEvaluation(nodeRecord.getMobilityCalculator(), color, move);

			sampleWriter.print(lostMovingPieceEvaluation);
			sampleWriter.print(",");

			sampleWriter.print(move.equals(nodeRecord.getOriginalKillerMove()));
			sampleWriter.print(",");

			sampleWriter.println(estimate);
		}
	}

	private int getLostMovingPieceEvaluation(final MobilityCalculator mobilityCalculator, final int color, final Move move) {
		final int oppositeColor = Color.getOppositeColor(color);
		final int targetSquare = move.getTargetSquare();

		return (mobilityCalculator.isSquareAttacked(oppositeColor, targetSquare)) ?
				PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(move.getMovingPieceType()) : 0;
	}

	private int getCapturedPieceEvaluation(final Move move) {
		final int capturedPieceType = move.getCapturedPieceType();

		return PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(capturedPieceType);
	}

	public void clear() {
		historyTable.clear();
	}

	public void log() {
		System.out.println("Move estimator confusion matrix");

		confusionMatrix.log();
	}

}
