package bishop.base;

import java.io.IOException;
import java.io.OutputStream;

import range.IProbabilityModel;
import range.RangeEncoder;


public class PositionWriter extends PositionIo {
	
	private RangeEncoder encoder;

	public void writePositionToStream(final OutputStream stream) throws IOException {
		final RangeEncoder encoder = new RangeEncoder();
		encoder.initialize(stream);
		writePositionToEncoder(encoder);
		encoder.close();
	}
	
	public void writePositionToEncoder(final RangeEncoder encoder) throws IOException {
		this.encoder = encoder;
		
		try {
			writePieceCounts();
			writeOnTurn();
			writePiecePositions();
			writeCastlingRights();
			writeEpFile();
		}
		finally {
			this.encoder = null;
		}
	}
	
	private void writePieceCounts() throws IOException {
		for (RecordedPiece recordedPiece: RECORDED_PIECES) {
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final long pieces = position.getPiecesMask(color, recordedPiece.getPieceType());
				final int count = BitBoard.getSquareCount(pieces & recordedPiece.getRecordedSquares());
				final IProbabilityModel model = recordedPiece.getCountProbabilityModel();
				
				if (model != null)
					encoder.encodeSymbol(model, count);
			}
		}
	}

	private void writeOnTurn() throws IOException {
		encoder.encodeSymbol(ON_TURN_PROBABILITY_MODEL, position.getOnTurn());
	}
	
	private void writePiecePositions() throws IOException {
		long occupiedSquares = BitBoard.EMPTY;
		
		for (RecordedPiece recordedPiece: RECORDED_PIECES) {
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				occupiedSquares |= writePiece (color, recordedPiece, occupiedSquares);
			}
		}
	}
	
	private long writePiece (final int color, final RecordedPiece recordedPiece, final long occupiedSquares) throws IOException {
		final long possibleMask = ~occupiedSquares & recordedPiece.getRecordedSquares();
		final long pieceMask = position.getPiecesMask(color, recordedPiece.getPieceType()) & possibleMask;
		
		int remainingPieceCount = BitBoard.getSquareCount(pieceMask);
		int remainingSquareCount = BitBoard.getSquareCount(possibleMask);
		
		for (int square = Square.FIRST; square < Square.LAST && remainingPieceCount > 0 && remainingPieceCount < remainingSquareCount; square++) {
			final long squreMask = BitBoard.getSquareMask(square);
			
			if ((possibleMask & squreMask) != 0) {
				final IProbabilityModel model = getPiecePositionProbabilityModel (remainingPieceCount, remainingSquareCount);
				
				if ((pieceMask & squreMask) != 0) {
					encoder.encodeSymbol(model, SYMBOL_TRUE);
					remainingPieceCount--;
				}
				else
					encoder.encodeSymbol(model, SYMBOL_FALSE);
				
				remainingSquareCount--;
			}
		}
		
		return pieceMask;
	}

	private void writeCastlingRights() throws IOException {
		final CastlingRights rights = position.getCastlingRights();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int type = CastlingType.FIRST; type < CastlingType.LAST; type++) {
				if (PositionValidator.isCastlingRightPossible(position, color, type)) {
					final int symbol = rights.isRight(color, type) ? 1 : 0;
					encoder.encodeSymbol(CASTLING_RIGHT_PROBABILITY_MODEL, symbol);
				}
			}
		}
	}

	private void writeEpFile() throws IOException {
		final long possibleSquares = getPossibleEpSquares();
		
		if (possibleSquares != BitBoard.EMPTY) {
			final int count = BitBoard.getSquareCount(possibleSquares);
			final int epFile = position.getEpFile(); 
			final int symbol;
			
			if (epFile == File.NONE)
				symbol = 0;
			else {
				final int epSquare = BoardConstants.getEpSquare(Color.getOppositeColor(position.getOnTurn()), epFile);
				final int index = BitBoard.getSquareIndex (possibleSquares, epSquare);
				
				symbol = index + 1;
			}
			
			final IProbabilityModel model = getEpFileProbabilityModel(count);
			encoder.encodeSymbol(model, symbol);
		}
	}

}
