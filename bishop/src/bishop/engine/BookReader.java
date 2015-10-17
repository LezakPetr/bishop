package bishop.engine;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import utils.CountingInputStream;
import utils.IoUtils;

import bishop.base.Annotation;
import bishop.base.Move;
import bishop.base.Position;
import bishop.base.PositionReader;

public class BookReader extends BookIo implements IBook<EvaluatedBookRecord> {
	

	private final String path;
	private CountingInputStream stream;
	private int hashBits;
	private long hashMask;
	private int bytesPerSize;
	private long recordListBegin;
	private long recordListEnd;
	
	public BookReader (final String path) {
		this.path = path;
	}
	
	@Override
	public EvaluatedBookRecord getRecord(final Position position) {
		try {
			try {
				stream = new CountingInputStream(new BufferedInputStream(new FileInputStream(path)));
			
				readHeader();
				findRecordList(position);
				
				return findRecordInList(position);
			}
			finally {
				if (stream != null) {
					stream.close();
					stream = null;
				}
			}
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot read record", ex);
		}
	}

	public void readHeader() throws IOException {
		if (!IoUtils.hasExpectedBytes(stream, HEADER_MAGIC))
			throw new RuntimeException("Wrong magic");

		/*final byte version = */IoUtils.readByteBinary(stream);
		
		hashBits = IoUtils.readByteBinary(stream);
		hashMask = (1L << hashBits) - 1;
		
		bytesPerSize = IoUtils.readByteBinary(stream);
	}

	private void findRecordList(final Position position) throws IOException {
		final long hash = position.getHash();
	
		stream.skip((hash & hashMask) * bytesPerSize);
		
		recordListBegin = IoUtils.readUnsignedNumberBinary(stream, bytesPerSize);
		recordListEnd = IoUtils.readUnsignedNumberBinary(stream, bytesPerSize);
		
		stream.skip(recordListBegin - stream.getPosition());
	}
	

	private EvaluatedBookRecord findRecordInList(final Position position) throws IOException {
		while (stream.getPosition() < recordListEnd) {
			final EvaluatedBookRecord record = readRecord();
			
			if (record.getPosition().equals(position))
				return record;
		}
		
		return null;
	}

	private EvaluatedBookRecord readRecord() throws IOException {
		final EvaluatedBookRecord record = new EvaluatedBookRecord();
		
		final PositionReader positionReader = new PositionReader();
		positionReader.readPositionFromStream(stream);
		
		final Position position = positionReader.getPosition();
		record.getPosition().assign(position);
		
		final int evaluation = (int) IoUtils.readSignedNumberBinary(stream, EVALUATION_BYTES);
		record.setEvaluation(evaluation);
		
		final int moveCount = IoUtils.readByteBinary(stream) & 0xFF;
		
		for (int i = 0; i < moveCount; i++)
			record.addMove(readMove(position));
		
		return record;
	}

	private BookMove readMove(final Position position) throws IOException {
		final int data = (int) IoUtils.readUnsignedNumberBinary(stream, MOVE_SIZE);
		final int compressedMove = data & Move.COMPRESSED_MOVE_MASK;
		
		final Move move = new Move();
		move.uncompressMove(compressedMove, position);
		
		final boolean goodMove = (data & GOOD_MOVE_MASK) != 0;
		
		final BookMove bookMove = new BookMove();
		bookMove.setMove(move);
		bookMove.setAnnotation((goodMove) ? Annotation.GOOD_MOVE : Annotation.POOR_MOVE);
		
		return bookMove;
	}

	@Override
	public Collection<EvaluatedBookRecord> getAllRecords() {
		// TODO Auto-generated method stub
		return null;
	}
}
