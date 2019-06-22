package bishop.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import bishop.base.*;
import math.*;
import math.Utils;

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

	private static final int INTERCEPT = Utils.roundToInt(-1.590e-01 * ESTIMATE_MULTIPLIER);
	private static final double HISTORY_COEFF = 3.016e+00 * ESTIMATE_MULTIPLIER;
	private static final double CAPTURED_PIECE_COEFF = 3.901e-04 * ESTIMATE_MULTIPLIER;
	private static final double LOST_PIECE_COEFF = -1.663e-04 * ESTIMATE_MULTIPLIER;
	private static final int KILLER_COEFF = Utils.roundToInt(1.134e+00 * ESTIMATE_MULTIPLIER);
	private static final double CAPTURED_PIECE_HISTORY_COEFF = -1.459e-04 * ESTIMATE_MULTIPLIER;
	private static final double LOST_PIECE_HISTORY_COEFF = -9.947e-05 * ESTIMATE_MULTIPLIER;

	private static final int[] CAPTURED_PIECE_ESTIMATES = buildPieceTypeIntEstimates(CAPTURED_PIECE_COEFF);
	private static final int[] LOST_PIECE_ESTIMATES = buildPieceTypeIntEstimates(LOST_PIECE_COEFF);

	private static final double[] CAPTURED_PIECE_HISTORY_ESTIMATES = buildPieceTypeDoubleEstimates(CAPTURED_PIECE_HISTORY_COEFF);
	private static final double[] LOST_PIECE_HISTORY_ESTIMATES = buildPieceTypeDoubleEstimates(LOST_PIECE_HISTORY_COEFF);

	private static int[] buildPieceTypeIntEstimates(final double coeff) {
		return IntStream.rangeClosed(0, PieceType.NONE)
				.map(pieceType -> Utils.roundToInt(coeff * PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(pieceType)))
				.toArray();
	}

	private static double[] buildPieceTypeDoubleEstimates(final double coeff) {
		return IntStream.rangeClosed(0, PieceType.NONE)
				.mapToDouble(pieceType -> coeff * PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(pieceType))
				.toArray();
	}


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
		final int killerEstimate = (move.equals(nodeRecord.getOriginalKillerMove())) ? KILLER_COEFF : 0;
		final int capturedPieceType = move.getCapturedPieceType();
		final int lostPieceType = getLostPieceType(nodeRecord.getMobilityCalculator(), color, move);

		final double historyDependentEstimate = history * (
				HISTORY_COEFF +
				CAPTURED_PIECE_HISTORY_ESTIMATES[capturedPieceType] +
				LOST_PIECE_HISTORY_ESTIMATES[lostPieceType]
		);

		return INTERCEPT +
		       CAPTURED_PIECE_ESTIMATES[capturedPieceType] +
		       LOST_PIECE_ESTIMATES[lostPieceType] +
		       killerEstimate +
		       (int) historyDependentEstimate;
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

			final int capturedPieceEvaluation = PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(move.getCapturedPieceType());

			sampleWriter.print(capturedPieceEvaluation);
			sampleWriter.print(",");

			final int lostMovingPieceEvaluation = PieceTypeEvaluations.DEFAULT.getPieceTypeEvaluation(getLostPieceType(nodeRecord.getMobilityCalculator(), color, move));

			sampleWriter.print(lostMovingPieceEvaluation);
			sampleWriter.print(",");

			sampleWriter.print(move.equals(nodeRecord.getOriginalKillerMove()));
			sampleWriter.print(",");

			sampleWriter.println(estimate);
		}
	}

	private static int getLostPieceType(final MobilityCalculator mobilityCalculator, final int color, final Move move) {
		final int oppositeColor = Color.getOppositeColor(color);
		final int targetSquare = move.getTargetSquare();

		return (mobilityCalculator.isSquareAttacked(oppositeColor, targetSquare)) ?
				move.getMovingPieceType() : PieceType.NONE;
	}

	public void clear() {
		historyTable.clear();
	}

	public void log() {
		System.out.println("Move estimator confusion matrix");

		confusionMatrix.log();
	}

}
