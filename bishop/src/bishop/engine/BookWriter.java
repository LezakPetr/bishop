package bishop.engine;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bishop.base.Annotation;
import bishop.base.PositionWriter;

import utils.IntUtils;
import utils.IoUtils;

public class BookWriter extends BookIo {
	
	private static final byte VERSION = 0;
	
	private List<List<EvaluatedBookRecord>> sortedRecordList;
	private int hashBits;
	private int bytesPerSize;
	private long[] hashOffsets;
	private OutputStream targetStream;
	private java.io.File tempPath;
	

	public void writeBook (final IBook<EvaluatedBookRecord> book, final String path) throws IOException {
		getSortedRecordList (book);
		
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
	
	private void writeRecordsToTempFile() throws IOException {
		tempPath = java.io.File.createTempFile("book", "tmp");
		
		try (FileOutputStream tempStream = new FileOutputStream(tempPath)) {
			final int hashOffsetCount = sortedRecordList.size() + 1;
			hashOffsets = new long[hashOffsetCount];
			
			final long beginOffset = HEADER_SIZE + hashOffsetCount * bytesPerSize;;
			hashOffsets[0] = beginOffset;
			
			for (int i = 0; i < sortedRecordList.size(); i++) {
				final ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
				
				for (EvaluatedBookRecord record: sortedRecordList.get(i))
					writeRecord(memoryStream, record);
				
				final byte[] array = memoryStream.toByteArray();
				tempStream.write(array);
				hashOffsets[i+1] = hashOffsets[i] + array.length;
			}
		}
	}

	private void writeRecord(final OutputStream stream, final EvaluatedBookRecord record) throws IOException {
		final PositionWriter positionWriter = new PositionWriter();
		positionWriter.getPosition().assign(record.getPosition());
		positionWriter.writePositionToStream(stream);

		IoUtils.writeNumberBinary(stream, record.getEvaluation(), EVALUATION_BYTES);
		
		final int moveCount = record.getMoveCount();
		
		if (moveCount >= (1 << Byte.SIZE))
			throw new RuntimeException("Too many moves in record");
		
		stream.write(moveCount);
		
		for (int i = 0; i < moveCount; i++)
			writeMove (stream, record.getMoveAt(i));
	}

	private void writeMove(final OutputStream stream, final BookMove bookMove) throws IOException {
		final int compressedMove = bookMove.getMove().getCompressedMove();
		final int goodMoveBit = (bookMove.getAnnotation() == Annotation.POOR_MOVE) ? 0 : GOOD_MOVE_MASK;
		final int data = compressedMove | goodMoveBit;
		
		IoUtils.writeNumberBinary(stream, data, MOVE_SIZE);
	}

	private void writeHeader() throws IOException {
		targetStream.write(HEADER_MAGIC);
		targetStream.write(VERSION);
		targetStream.write(hashBits);
		targetStream.write(bytesPerSize);
	}
	
	private void writeHashOffsets() throws IOException {
		for (long offset: hashOffsets)
			IoUtils.writeNumberBinary(targetStream, offset, bytesPerSize);
	}

	private void copyRecordsFromTempFile() throws FileNotFoundException, IOException {
		try (FileInputStream tempStream = new FileInputStream(tempPath)) {
			IoUtils.copyStream(tempStream, targetStream);
		}
	}
	
	private void getSortedRecordList(final IBook<EvaluatedBookRecord> book) {
		final Collection<EvaluatedBookRecord> recordList = book.getAllRecords();
		hashBits = IntUtils.ceilLog(recordList.size());
		
		final int hashSize = 1 << hashBits;
		final int hashMask = hashSize - 1;
		
		sortedRecordList = new ArrayList<List<EvaluatedBookRecord>>();
				
		for (int i = 0; i < hashSize; i++) {
			sortedRecordList.add(new ArrayList<EvaluatedBookRecord>());
		}
		
		for (EvaluatedBookRecord record: recordList) {
			final int hash = (int) record.getPosition().getHash() & hashMask;
			
			sortedRecordList.get(hash).add(record);
		}
	}
}
