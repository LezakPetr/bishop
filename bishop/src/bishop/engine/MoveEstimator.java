package bishop.engine;

import java.util.function.Consumer;

import bishop.base.Color;
import bishop.base.Move;
import bishop.base.PieceType;
import math.SimpleLinearModel;
import utils.IntArrayBuilder;

/**
 * Estimator that estimates the moves to order them in alpha-beta search.
 * This class maintains linear models of history evaluation -> probability of cutoff
 * for every combination of color, killer heuristic, moving and captured piece type.
 *  
 * @author Ing. Petr Ležák
 */
public class MoveEstimator {
	// Following must be true:
	//   2 * SAMPLE_COUNT_TO_RECALCULATE_ESTIMATES * REDUCTION_FREQUENCY * e^2 < 2^63
	// where e = min (MOVE_ESTIMATE, HistoryTable.MAX_EVALUATION)
	// to prevent potential overflows
	private static final int SAMPLE_COUNT_TO_RECALCULATE_ESTIMATES = 65536;
	private static final int REDUCTION_FREQUENCY = 256;
	private static final int MOVE_ESTIMATE = 1000;
	
	private static final int[] PIECE_TYPE_ESTIMATIONS = new IntArrayBuilder(PieceType.LAST)
			.put(PieceType.PAWN, 0)
			.put(PieceType.KNIGHT, 1)
			.put(PieceType.BISHOP, 1)
			.put(PieceType.ROOK, 2)
			.put(PieceType.QUEEN, 3)
			.put(PieceType.KING, 3)
			.build();
	
	private static final int MAX_KILLER = 16;
	private static final int MAX_CAPTURE_ESTIMATION = 16;
	

	private final HistoryTable historyTable = new HistoryTable();
	
	private final SimpleLinearModel models[][][] = new SimpleLinearModel[Color.LAST][MAX_KILLER][MAX_CAPTURE_ESTIMATION];	
	private int sampleCounter;
	private int reductionCounter;
	
	public MoveEstimator() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int killer = 0; killer < MAX_KILLER; killer++) {
				for (int capture = 0; capture < MAX_CAPTURE_ESTIMATION; capture++) 
					models[color][killer][capture] = new SimpleLinearModel();
			}
		}
	}
	
	private void forEachModel(final Consumer<SimpleLinearModel> modelConsumer) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int killer = 0; killer < MAX_KILLER; killer++) {
				for (int capture = 0; capture < MAX_CAPTURE_ESTIMATION; capture++) 
					modelConsumer.accept(models[color][killer][capture]);
			}
		}
	}
	
	public int getMoveEstimate(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		final SimpleLinearModel model = getModel(nodeRecord, color, move);
		
		final int history = historyTable.getEvaluation(color, move);
		final int estimate = model.estimate(history);
		
		return estimate;
	}

	public void addCutoff(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move cutoffMove, final int horizon) {
		if (cutoffMove.equals(nodeRecord.getFirstLegalMove()))
			return;
		
		updateModel(nodeRecord, color, cutoffMove, MOVE_ESTIMATE);
		updateModel(nodeRecord, color, nodeRecord.getFirstLegalMove(), -MOVE_ESTIMATE);
		
		historyTable.addCutoff(color, cutoffMove, horizon);

		sampleCounter++;
		
		if (sampleCounter >= SAMPLE_COUNT_TO_RECALCULATE_ESTIMATES) {
			recalculateEstimates();
			sampleCounter = 0;
		}
	}

	private void updateModel (final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move, final int estimate) {
		final SimpleLinearModel model = getModel(nodeRecord, color, move);
		final int history = historyTable.getEvaluation(color, move);
				
		model.addSample(history, estimate);
	}
	
	private SimpleLinearModel getModel(final SerialSearchEngine.NodeRecord nodeRecord, final int color, final Move move) {
		final int killer = (move.equals(nodeRecord.getKillerMove())) ? 1 : 0;
		final int capture = estimateCapture(move.getMovingPieceType(), move.getCapturedPieceType());
		final SimpleLinearModel model = models[color][killer][capture];
		
		return model;
	}
	
	private void recalculateEstimates() {
		historyTable.recalculateCoeff();
		
		forEachModel(SimpleLinearModel::recalculateModel);
		
		reductionCounter++;
		
		if (reductionCounter >= REDUCTION_FREQUENCY) {
			forEachModel(SimpleLinearModel::reduceWeightOfSamples);
			reductionCounter = 0;
		}
	}
	
	public void clear() {
		historyTable.clear();
		forEachModel(SimpleLinearModel::clear);
		
		sampleCounter = 0;
		reductionCounter = 0;
	}
		
	private static int estimateCapture (final int movingPieceType, final int capturedPieceType) {
		if (capturedPieceType == PieceType.NONE)
			return 0;
		else {
			final int movingPieceEvaluation = PIECE_TYPE_ESTIMATIONS[movingPieceType];
			final int capturedPieceEvaluation = PIECE_TYPE_ESTIMATIONS[capturedPieceType];
			
			return 4 * capturedPieceEvaluation + movingPieceEvaluation;
		}
	}
		

}
