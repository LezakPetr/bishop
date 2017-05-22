package bishop.base;

import range.IProbabilityModel;
import range.ProbabilityModelFactory;
import utils.ArrayUtils;

public class PositionIo {

	protected static final int SYMBOL_FALSE = 0;
	protected static final int SYMBOL_TRUE = 1;
	
	
	protected final Position position = new Position();
	
	// Order of piece types stored in positions.
	protected static class RecordedPiece {
		private final int pieceType;
		private final long recordedSquares;
		private final IProbabilityModel countProbabilityModel;
		
		public RecordedPiece (final int pieceType, final long recordedSquares, final IProbabilityModel countProbabilityModel) {
			this.pieceType = pieceType;
			this.recordedSquares = recordedSquares;
			this.countProbabilityModel = countProbabilityModel;
		}

		public int getPieceType() {
			return pieceType;
		}

		public long getRecordedSquares() {
			return recordedSquares;
		}
		
		public IProbabilityModel getCountProbabilityModel() {
			return countProbabilityModel;
		}
	}
	
	protected static final RecordedPiece[] RECORDED_PIECES = createRecordedPieces();
	
	protected static final IProbabilityModel ON_TURN_PROBABILITY_MODEL = ProbabilityModelFactory.fromProbabilities(32768, 32768);
	protected static final IProbabilityModel CASTLING_RIGHT_PROBABILITY_MODEL = ProbabilityModelFactory.fromProbabilities(6554, 58982);
	
	private static RecordedPiece[] createRecordedPieces() {
		final RecordedPiece[] result = new RecordedPiece[7];
		
		final IProbabilityModel modelOnePiece = ProbabilityModelFactory.fromProbabilities(ArrayUtils.copyItems(31926, 2, 1024, 1, 64, 7, 4, 53));
		final IProbabilityModel modelTwoPieces = ProbabilityModelFactory.fromProbabilities(ArrayUtils.copyItems(21285, 3, 1025, 1, 64, 7, 4, 52));
		final IProbabilityModel modelEightPieces = ProbabilityModelFactory.fromProbabilities(ArrayUtils.copyItems(7264, 9, 4, 40));
		
		result[0] = new RecordedPiece(PieceType.PAWN, ~BoardConstants.RANK_18_MASK, modelEightPieces);
		result[1] = new RecordedPiece(PieceType.KNIGHT, BitBoard.FULL, modelTwoPieces);
		result[2] = new RecordedPiece(PieceType.BISHOP, BoardConstants.WHITE_SQUARE_MASK, modelOnePiece);
		result[3] = new RecordedPiece(PieceType.BISHOP, BoardConstants.BLACK_SQUARE_MASK, modelOnePiece);
		result[4] = new RecordedPiece(PieceType.ROOK, BitBoard.FULL, modelTwoPieces);
		result[5] = new RecordedPiece(PieceType.QUEEN, BitBoard.FULL, modelOnePiece);
		result[6] = new RecordedPiece(PieceType.KING, BitBoard.FULL, null);
		
		return result;
		
	}
	
	protected static IProbabilityModel getPiecePositionProbabilityModel (final int pieceCount, final int squareCount) {
		return ProbabilityModelFactory.createBinaryFraction(squareCount - pieceCount, squareCount);
	}
	
	protected IProbabilityModel getEpFileProbabilityModel(final int fileCount) {
		final long[] frequencies = new long[fileCount + 1];
		frequencies[0] = 50;
		
		for (int i = 1; i <= fileCount; i++)
			frequencies[i] = 1;
		
		return ProbabilityModelFactory.fromUnnormalizedProbabilities(frequencies);
	}
	
	/*
	 * Returns mask of possible EP squares in given position. Possible EP square:
	 * - is on EP rank (4 or 5)
	 * - has opposite pawn on it
	 * - has own pawn on on of neighbor squares (squares on opposite files)
	 * - has two free squares below it (on rank 2 and 3 for black or 6 and 7 for white) 
	 */
	protected long getPossibleEpSquares() {
		final int onTurn = position.getOnTurn();
		final int notOnTurn = Color.getOppositeColor(onTurn);
		final long ownPawns = position.getPiecesMask(onTurn, PieceType.PAWN);
		final long oppositePawns = position.getPiecesMask(notOnTurn, PieceType.PAWN);
		final long epRankMask = BoardConstants.getRankMask(BoardConstants.getEpRank(notOnTurn));
		
		long result = oppositePawns & epRankMask & BoardConstants.getAllConnectedPawnSquareMask(ownPawns);
		
		final long freeSquares = ~position.getOccupancy();
		
		if (onTurn == Color.WHITE) {
			result &= freeSquares >>> File.LAST;
			result &= freeSquares >>> (2*File.LAST);
		}
		else {
			result &= freeSquares << File.LAST;
			result &= freeSquares << (2*File.LAST);
		}
		
		return result;
	}

	public Position getPosition() {
		return position;
	}

}
