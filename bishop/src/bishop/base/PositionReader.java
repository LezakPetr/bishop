package bishop.base;

import java.io.IOException;
import java.io.InputStream;

import range.IProbabilityModel;
import range.ProbabilityModelFactory;
import range.RangeDecoder;

public class PositionReader extends PositionIo {

	private RangeDecoder decoder;
	private final int[][] pieceCounts = new int[Color.LAST][RECORDED_PIECES.length];
	
	public void readPositionFromStream(final InputStream stream) throws IOException {
		final RangeDecoder decoder = new RangeDecoder();
		decoder.initialize(stream);
		readPositionFromDecoder(decoder);
		decoder.close();
	}
	
	public void readPositionFromDecoder(final RangeDecoder decoder) throws IOException {
		this.decoder = decoder;
		
		try {
			position.clearPosition();
			
			readPieceCounts();
			readOnTurn();
			readPiecePositions();
			position.refreshCachedData();
			
			readCastlingRights();
			readEpFile();
			
			position.refreshCachedData();
		}
		finally {
			this.decoder = null;
		}
	}

	private void readEpFile() throws IOException {
		final long possibleSquares = getPossibleEpSquares();
		
		if (possibleSquares != BitBoard.EMPTY) {
			final int count = BitBoard.getSquareCount(possibleSquares);
			final IProbabilityModel model = getEpFileProbabilityModel(count);
			final int symbol = decoder.decodeSymbol(model);
			final int epFile;
			
			if (symbol == 0)
				epFile = File.NONE;
			else {
				final int index = symbol - 1;
				final int epSquare = BitBoard.getNthSquare (possibleSquares, index);
				
				epFile = Square.getFile(epSquare);
			}
			
			position.setEpFile(epFile);
		}
	}

	private void readCastlingRights() throws IOException {
		final CastlingRights rights = position.getCastlingRights();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int type = CastlingType.FIRST; type < CastlingType.LAST; type++) {
				if (PositionValidator.isCastlingRightPossible(position, color, type)) {
					final int symbol = decoder.decodeSymbol(CASTLING_RIGHT_PROBABILITY_MODEL);
					rights.setRight(color, type, symbol != 0);
				}
			}
		}
	}

	private void readPiecePositions() throws IOException {
		long occupiedSquares = BitBoard.EMPTY;
		
		for (int recordedPieceIndex = 0; recordedPieceIndex < RECORDED_PIECES.length; recordedPieceIndex++) {
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				occupiedSquares |= readPiece (color, recordedPieceIndex, occupiedSquares);
			}
		}
	}
	
	private long readPiece (final int color, final int recordedPieceIndex, final long occupiedSquares) throws IOException {
		final RecordedPiece recordedPiece = RECORDED_PIECES[recordedPieceIndex];
		final long possibleMask = ~occupiedSquares & recordedPiece.getRecordedSquares();
		
		int remainingPieceCount = pieceCounts[color][recordedPieceIndex];
		int remainingSquareCount = BitBoard.getSquareCount(possibleMask);
		
		long pieceMask = BitBoard.EMPTY;
		
		for (int square = Square.FIRST; square < Square.LAST && remainingPieceCount > 0; square++) {
			final long squreMask = BitBoard.getSquareMask(square);
			
			if ((possibleMask & squreMask) != 0) {
				final IProbabilityModel model = getPiecePositionProbabilityModel (remainingPieceCount, remainingSquareCount);
				boolean isPiece;
				
				if (remainingPieceCount < remainingSquareCount)
					isPiece = (decoder.decodeSymbol(model) == SYMBOL_TRUE);
				else
					isPiece = true;
					
				if (isPiece) {
					position.setSquareContent(square, Piece.withColorAndType(color, recordedPiece.getPieceType()));
					pieceMask |= squreMask;
					remainingPieceCount--;
				}
				
				remainingSquareCount--;
			}
		}
		
		return pieceMask;
	}


	private void readPieceCounts() throws IOException {
		for (int recordedPieceIndex = 0; recordedPieceIndex < RECORDED_PIECES.length; recordedPieceIndex++) {
			final RecordedPiece recordedPiece = RECORDED_PIECES[recordedPieceIndex];
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final IProbabilityModel model = recordedPiece.getCountProbabilityModel();
				final int count = (model != null) ? decoder.decodeSymbol(model) : 1;
				
				pieceCounts[color][recordedPieceIndex] = count;
			}
		}
	}

	private void readOnTurn() throws IOException {
		final int onTurn = decoder.decodeSymbol(ON_TURN_PROBABILITY_MODEL);
		position.setOnTurn(onTurn);
	}

}
