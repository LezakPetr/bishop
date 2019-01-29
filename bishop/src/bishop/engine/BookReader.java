package bishop.engine;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import bishop.base.Move;
import bishop.base.Position;
import bishop.base.PositionReader;
import range.IProbabilityModel;
import range.ProbabilityModelFactory;
import range.RangeBase;
import range.RangeDecoder;
import utils.CountingInputStream;
import utils.IoUtils;

public class BookReader extends BookIo implements IBook<BookRecord> {
	
	private final URL url;
	private CountingInputStream stream;
	private RangeDecoder recordListDecoder;

	private int offsetSize;
	private int maxBookRecordsInList;
	
	public BookReader(final String path) {
		try {
			this.url = new File(path).toURI().toURL();
			
			init();
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot initialize reader", ex);
		}
	}
	
	public BookReader (final URL url) {
		try {
			this.url = url;
			
			init();
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot initialize reader", ex);
		}
	}
	
	private void init() throws IOException {
		try {
			stream = new CountingInputStream(new BufferedInputStream(url.openStream()));
		
			readHeader();
		}
		finally {
			if (stream != null) {
				stream.close();
				stream = null;
				
				recordListDecoder = null;
			}
		}
	}
	
	@Override
	public BookRecord getRecord(final Position position) {
		try {
			try {
				stream = new CountingInputStream(new BufferedInputStream(url.openStream()));
				
				final byte[] recordListData = findRecordList(position);
				
				if (recordListData == null)
					return null;
				
				recordListDecoder = new RangeDecoder();
				recordListDecoder.initialize(new ByteArrayInputStream(recordListData));
				
				return findRecordInList(position);
			}
			finally {
				if (stream != null) {
					stream.close();
					stream = null;
					
					recordListDecoder = null;
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
		
		setHashBits(IoUtils.readByteBinary(stream));
		
		offsetSize = IoUtils.readByteBinary(stream);
		maxBookRecordsInList = (int) IoUtils.readUnsignedNumberBinary(stream, IoUtils.INT_BYTES);

		moveIncludedProbabilityModel = readProbabilityModel(BOOLEAN_SYMBOL_COUNT);
		recordListContinueProbabilityModel = readProbabilityModel(BOOLEAN_SYMBOL_COUNT);
		relativaMoveRepetitionProbabilityModel = readProbabilityModel(RELATIVE_MOVE_REPETITION_SYMBOL_COUNT);
		balanceProbabilityModel = readProbabilityModel(BALANCE_SYMBOL_COUNT);
		positionRepetitionCountProbabilityModel = readProbabilityModel(POSITION_REPETITION_COUNT_SYMBOL_COUNT);
	}
	
	private IProbabilityModel readProbabilityModel (final int count) throws IOException {
		final int[] probabilities = new int[count];
		int sum = 0;
		
		for (int i = 0; i < count - 1; i++) {
			final int probability = (int) IoUtils.readUnsignedNumberBinary(stream, IoUtils.SHORT_BYTES);
			probabilities[i] = probability;
			sum += probability;
		}
		
		probabilities[count - 1] = RangeBase.MAX_SYMBOL_CDF - sum;
		
		return ProbabilityModelFactory.fromProbabilities(probabilities);
	}

	private byte[] findRecordList(final Position position) throws IOException {
		final long hash = position.getHash();
		final long hashOffset = (hash & hashMask) * offsetSize;
	
		IoUtils.skip(stream, HEADER_SIZE + hashOffset);
		
		final long recordListBegin = IoUtils.readUnsignedNumberBinary(stream, offsetSize);
		final long recordListEnd = IoUtils.readUnsignedNumberBinary(stream, offsetSize);
		
		if (recordListEnd > recordListBegin) {
			IoUtils.skip(stream, recordListBegin - stream.getPosition());
			
			final int size = (int) (recordListEnd - recordListBegin);

			return IoUtils.readByteArray(stream, size);
		}
		else
			return null;
	}
	

	private BookRecord findRecordInList(final Position position) throws IOException {
		for (int i = 0; i < maxBookRecordsInList; i++) {
			final BookRecord record = readRecord();
			
			if (record.getPosition().equals(position))
				return record;
			
			if (recordListDecoder.decodeSymbol(recordListContinueProbabilityModel) == SYMBOL_FALSE)
				break;
		}
		
		return null;
	}

	private BookRecord readRecord() throws IOException {
		final BookRecord record = new BookRecord();
		
		final PositionReader positionReader = new PositionReader();
		positionReader.readPositionFromDecoder(recordListDecoder);
		
		final Position position = positionReader.getPosition();
		record.getPosition().assign(position);
		
		final int balance = readPositionBalance();
		record.setBalance(balance);
		
		final int repetitionCount = recordListDecoder.decodeSymbol(positionRepetitionCountProbabilityModel);
		record.setRepetitionCount(repetitionCount);
		
		moveGenerator.setPosition(position);
		moveGenerator.generateMoveList(moveList);
		moveList.sort();
		
		for (int i = 0; i < moveList.getSize(); i++) {
			final BookMove move = readMove(position, moveList.get(i));
			
			if (move != null)
				record.addMove(move);
		}
		
		return record;
	}

	private BookMove readMove(final Position position, final Move move) throws IOException {
		if (recordListDecoder.decodeSymbol(moveIncludedProbabilityModel) == SYMBOL_FALSE)
			return null;
		
		final int relativaMoveRepetition = recordListDecoder.decodeSymbol(relativaMoveRepetitionProbabilityModel);
		final int targetPositionBalance = readPositionBalance();
		
		final BookMove bookMove = new BookMove();
		bookMove.setMove(move);
		bookMove.setRelativeMoveRepetition (relativaMoveRepetition);
		bookMove.setTargetPositionBalance (targetPositionBalance);
		
		return bookMove;
	}

	private int readPositionBalance() throws IOException {
		return recordListDecoder.decodeSymbol(balanceProbabilityModel) - BALANCE_OFFSET;
	}

	@Override
	public Collection<BookRecord> getAllRecords() {
		throw new RuntimeException("Method getAllRecords not implemented");
	}
}
