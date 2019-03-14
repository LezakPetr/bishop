package bishop.engine;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import bishop.base.Move;
import bishop.base.PositionWriter;
import range.IProbabilityModel;
import range.ProbabilityStatistics;
import range.RangeBase;
import range.RangeEncoder;
import utils.IntUtils;
import utils.IoUtils;

public class BookWriter extends BookIo {
	
	private static final byte VERSION = 0;
	
	private List<List<BookRecord>> sortedRecordList;
	private int offsetSize;
	private long[] hashOffsets;
	private OutputStream targetStream;
	private java.io.File tempPath;
	

	public void writeBook (final IBook<? extends BookRecord> book, final String path) throws IOException {
		calculateSortedRecordList (book);
		calculateProabilityModels();
		
		try {
			writeRecordsToTempFile();
			
			targetStream = new FileOutputStream(path);
			
			try {
				writeHeader();
				writeHashOffsets();
				copyRecordsFromTempFile();
			}
			finally {
				targetStream.close();
				targetStream = null;
			}
		}
		finally {
			if (tempPath != null) {
				tempPath.delete();
				tempPath = null;
			}
		}
	}
	
	private void calculateProabilityModels() {
		final ProbabilityStatistics recordListContinueStatistics = new ProbabilityStatistics(BOOLEAN_SYMBOL_COUNT);
		final ProbabilityStatistics moveIncludedStatistics = new ProbabilityStatistics(BOOLEAN_SYMBOL_COUNT);
		final ProbabilityStatistics relativaMoveRepetitionStatistics = new ProbabilityStatistics(RELATIVE_MOVE_REPETITION_SYMBOL_COUNT);
		final ProbabilityStatistics balanceStatistics = new ProbabilityStatistics(BALANCE_SYMBOL_COUNT);
		final ProbabilityStatistics positionRepetitionCountStatistics = new ProbabilityStatistics(POSITION_REPETITION_COUNT_SYMBOL_COUNT);
				
		for (List<BookRecord> bookRecordList: sortedRecordList) {
			final int recordListSize = bookRecordList.size();
			
			if (recordListSize > 0) {
				recordListContinueStatistics.addSymbol(SYMBOL_FALSE, 1);
				recordListContinueStatistics.addSymbol(SYMBOL_TRUE, recordListSize - 1);
			}
			
			for (BookRecord bookRecord: bookRecordList) {
				final int includedCount = bookRecord.getMoveCount();
				
				moveGenerator.setPosition(bookRecord.getPosition());
				moveGenerator.generateMoveList(moveList);
				final int totalCount = moveList.getSize();
				
				moveIncludedStatistics.addSymbol(SYMBOL_FALSE, totalCount - includedCount);
				moveIncludedStatistics.addSymbol(SYMBOL_TRUE, includedCount);
				
				final int balance = bookRecord.getBalance();
				final int balanceSymbol = balance + BALANCE_OFFSET;
				balanceStatistics.addSymbol(balanceSymbol);
				
				positionRepetitionCountStatistics.addSymbol(bookRecord.getRepetitionCount());
				
				for (BookMove move: bookRecord.getMoveMap().values()) {
					final int relativeMoveRepetition = move.getRelativeMoveRepetition();
					relativaMoveRepetitionStatistics.addSymbol(relativeMoveRepetition);
				}
			}
		}
		
		recordListContinueProbabilityModel = recordListContinueStatistics.buildProbabilityModel();
		moveIncludedProbabilityModel = moveIncludedStatistics.buildProbabilityModel();
		relativaMoveRepetitionProbabilityModel = relativaMoveRepetitionStatistics.buildProbabilityModel();
		balanceProbabilityModel = balanceStatistics.buildProbabilityModel();
		positionRepetitionCountProbabilityModel = positionRepetitionCountStatistics.buildProbabilityModel();
	}

	private void writeProbabilityModel (final IProbabilityModel model) throws IOException {
		for (int symbol = 0; symbol < model.getSymbolCount() - 1; symbol++) {
			final int probability = model.getSymbolProbability(symbol);
			IoUtils.writeNumberBinary(targetStream, probability, RangeBase.PROBABILITY_BYTES);
			
			System.out.println("p " + symbol + " " + probability);
		}
	}
	
	private void writeRecordsToTempFile() throws IOException {
		tempPath = java.io.File.createTempFile("book", "tmp");
		
		try (FileOutputStream tempStream = new FileOutputStream(tempPath)) {
			final int recordListSize = sortedRecordList.size();
			final int hashOffsetCount = recordListSize + 1;
			hashOffsets = new long[hashOffsetCount];
			
			hashOffsets[0] = 0;
			
			for (int i = 0; i < recordListSize; i++) {
				final ByteArrayOutputStream memoryStream = writeRecordList(i);
				
				final byte[] array = memoryStream.toByteArray();
				tempStream.write(array);
				hashOffsets[i+1] = hashOffsets[i] + array.length;
			}
			
			// Calculate bytesPerSize and update hashOffsets
			final long maximalOffset = HEADER_SIZE + hashOffsetCount * Long.BYTES + hashOffsets[recordListSize];
			offsetSize = IntUtils.divideRoundUp(IntUtils.ceilLog(maximalOffset), Byte.SIZE);
			
			final long beginOffset = HEADER_SIZE + hashOffsetCount * offsetSize;
			
			for (int i = 0; i < hashOffsetCount; i++)
				hashOffsets[i] += beginOffset;
		}
	}

	private ByteArrayOutputStream writeRecordList(final int index) throws IOException {
		final List<BookRecord> recordList = sortedRecordList.get(index);
		final ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		final int recordCount = recordList.size();
		
		if (recordCount > 0) {
			final RangeEncoder encoder = new RangeEncoder();
			encoder.initialize(memoryStream);
			
			for (int i = 0; i < recordCount; i++) {
				writeRecord(encoder, recordList.get(i));
				
				final int symbolCont = (i < recordCount - 1) ? SYMBOL_TRUE : SYMBOL_FALSE;
				encoder.encodeSymbol(recordListContinueProbabilityModel, symbolCont);
			}
			
			encoder.close();
		}
		
		return memoryStream;
	}

	private void writeRecord(final RangeEncoder encoder, final BookRecord record) throws IOException {
		final PositionWriter positionWriter = new PositionWriter();
		positionWriter.getPosition().assign(record.getPosition());
		positionWriter.writePositionToEncoder(encoder);
		
		final int balance = record.getBalance();
		writePositionBalance(encoder, balance);
		
		final int repetitionCount = record.getRepetitionCount();
		encoder.encodeSymbol(positionRepetitionCountProbabilityModel, repetitionCount);
		
		moveGenerator.setPosition(record.getPosition());
		moveGenerator.generateMoveList(moveList);
		moveList.sort();
		
		final Map<Move, BookMove> bookMoveMap = record.getMoveMap();
		
		final int moveCount = moveList.getSize();
		
		for (int i = 0; i < moveCount; i++) {
			final Move move = moveList.get(i);
			final BookMove bookMove = bookMoveMap.get(move);
			
			writeMove (encoder, bookMove);
		}
	}

	private void writeMove(final RangeEncoder encoder, final BookMove bookMove) throws IOException {
		final int moveIncludedSymbol = (bookMove != null) ? SYMBOL_TRUE : SYMBOL_FALSE;
		encoder.encodeSymbol(moveIncludedProbabilityModel, moveIncludedSymbol);
		
		if (bookMove != null) {
			final int relativaMoveRepetition = bookMove.getRelativeMoveRepetition();
			encoder.encodeSymbol(relativaMoveRepetitionProbabilityModel, relativaMoveRepetition);
			
			final int targetPositionBalance = bookMove.getTargetPositionBalance();
			writePositionBalance (encoder, targetPositionBalance);
		}
	}

	private void writePositionBalance(final RangeEncoder encoder, final int balance) throws IOException {
		encoder.encodeSymbol(balanceProbabilityModel, balance + BALANCE_OFFSET);
	}

	private void writeHeader() throws IOException {
		targetStream.write(HEADER_MAGIC);
		targetStream.write(VERSION);
		targetStream.write(hashBits);
		targetStream.write(offsetSize);
		
		final int maxBookRecordsInList = sortedRecordList.stream()
				.mapToInt(List::size)
				.max()
				.orElse(0);
		
		IoUtils.writeNumberBinary(targetStream, maxBookRecordsInList, IoUtils.INT_BYTES);
		
		writeProbabilityModel(moveIncludedProbabilityModel);
		writeProbabilityModel(recordListContinueProbabilityModel);
		writeProbabilityModel(relativaMoveRepetitionProbabilityModel);
		writeProbabilityModel(balanceProbabilityModel);
		writeProbabilityModel(positionRepetitionCountProbabilityModel);
	}
	
	private void writeHashOffsets() throws IOException {
		for (long offset: hashOffsets)
			IoUtils.writeNumberBinary(targetStream, offset, offsetSize);
	}

	private void copyRecordsFromTempFile() throws IOException {
		try (FileInputStream tempStream = new FileInputStream(tempPath)) {
			IoUtils.copyStream(tempStream, targetStream);
		}
	}
	
	private void calculateSortedRecordList(final IBook<? extends BookRecord> book) {
		final Collection<? extends BookRecord> recordList = book.getAllRecords();
		setHashBits(IntUtils.ceilLog(recordList.size()));
		
		sortedRecordList = new ArrayList<>();
				
		for (int i = 0; i <= hashMask; i++) {
			sortedRecordList.add(new ArrayList<>());
		}
		
		for (BookRecord record: recordList) {
			final int hash = (int) (record.getPosition().getHash() & hashMask);
			
			sortedRecordList.get(hash).add(record);
		}
	}
}
