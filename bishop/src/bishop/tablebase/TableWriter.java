package bishop.tablebase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.CRC32;

import range.EnumerationProbabilityModel;
import range.RangeBase;
import range.RangeEncoder;
import utils.ChecksumStream;
import utils.HugeLongArray;
import utils.IoUtils;
import bishop.base.MaterialHash;
import bishop.base.Position;

public class TableWriter extends TableIo {
	
	public static final byte VERSION = 2;
	
	private static final int BLOCK_INDEX_EXPONENT = 12;
	public static final int BLOCK_INDEX_LENGTH = 1 << BLOCK_INDEX_EXPONENT;
	
	private File tempFile;
	private HugeLongArray blockPositions;
	
	
	private void createProbabilityModelsFromTable() {
		final TableStatistics statistics = new TableStatistics();
		
		statistics.calculate(table, BLOCK_INDEX_LENGTH);
		
		symbolToResultMap = statistics.getSymbolToResultMap();
		resultToSymbolMap = statistics.getResultToSymbolMap();
		symbolProbabilities = statistics.getSymbolProbabilities();
		
		probabilityModelMap = new HashMap<Long, EnumerationProbabilityModel>();
		
		for (Entry<Long, int[]> entry: symbolProbabilities.entrySet()) {
			EnumerationProbabilityModel model = new EnumerationProbabilityModel(entry.getValue());
			
			probabilityModelMap.put(entry.getKey(), model);
		}
		
		modelSelector = statistics.getProbabilityModelSelector(); 
	}
	
	public void writeTable (final ITable table, final OutputStream stream) throws IOException {
		this.table = table;
		
		createProbabilityModelsFromTable();
		
		writeSymbolsToTempFile();
		writeHeaderToStream(stream);
		copyTempFileToStream(stream);
	}
	
	public void writeTable (final ITable table, final File file) throws IOException {
		final OutputStream outputStream = new FileOutputStream(file);
		
		try {
			writeTable(table, outputStream);
		}
		finally {
			outputStream.close();
		}
	}
	
	private void writeSymbolsToTempFile() throws IOException {
		tempFile = File.createTempFile("bishop", ".tbbs");
		tempFile.deleteOnExit();
		
		final OutputStream tempStream = new FileOutputStream(tempFile);
		
		try {
			writeSymbolsToStream (tempStream);
		}
		finally {
			tempStream.close();
		}
	}
	
	private void copyTempFileToStream(final OutputStream stream) throws IOException {
		final FileInputStream tempStream = new FileInputStream(tempFile);
		
		try {
			IoUtils.copyStream(tempStream, stream);
		}
		finally {
			tempStream.close();
		}
	}
	
	private void writeHeaderToStream(final OutputStream stream) throws IOException {
		stream.write(HEADER_MAGIC);
		stream.write(VERSION);
		
		stream.write(table.getDefinition().getOnTurn());
		
		writeTableDefinition(stream);
		writeProbabilityModel(stream);
		writeBlockDataLengthsToStream(stream);
	}
	
	private int getBytesPerBlockPosition() {
		long maxBlockPosition = blockPositions.getAt(blockPositions.getSize() - 1);
		int neededBytes = 0;
		
		while (maxBlockPosition > 0) {
			neededBytes++;
			maxBlockPosition = maxBlockPosition >> 8;
		}
		
		return neededBytes;
	}

	private void writeBlockDataLengthsToStream(final OutputStream stream) throws IOException {
		final long blockPositionSize = blockPositions.getSize();
		final int bytesPerBlockPosition = getBytesPerBlockPosition();
		
		IoUtils.writeNumberBinary(stream, blockPositionSize, LAYER_LENGTH_BYTES);
		stream.write(BLOCK_INDEX_EXPONENT);
		stream.write(bytesPerBlockPosition);
		
		for (long i = 0; i < blockPositionSize; i++) {
			final long position = blockPositions.getAt(i);
			
			IoUtils.writeNumberBinary(stream, position, bytesPerBlockPosition);
		}
	}

	private void writeTableDefinition(final OutputStream stream) throws IOException {
		final TableDefinition tableDefinition = table.getDefinition();
		
		final int combinationCount = tableDefinition.getCombinationDefinitionCount();
		stream.write(combinationCount);
		
		for (int i = 0; i < combinationCount; i++) {
			final CombinationDefinition definition = tableDefinition.getCombinationDefinitionAt(i);
			final byte definitionData = definition.toData();
			
			stream.write(definitionData);
		}
	}
	
	private void writeProbabilityModel(final OutputStream stream) throws IOException {
		final int symbolCount = symbolToResultMap.length;
		final int modelCount = probabilityModelMap.size();
		
		System.out.println ("Writing " + modelCount + " models with " + symbolCount + " symbols");
		System.out.println ("Chunk count: " + table.getDefinition().getChunkCount());
		
		IoUtils.writeNumberBinary(stream, symbolCount, SYMBOL_COUNT_SIZE);
		
		final ClassificationProbabilityModelSelector classificationSelector = (ClassificationProbabilityModelSelector) modelSelector;
		final int classificationHistoryLength = classificationSelector.getClassificationHistoryLength();
		final boolean previousWin = classificationSelector.isPreviousWin();
		
		final int dat = (classificationHistoryLength << HISTORY_LENGTH_SHIFT) | (previousWin ? PREVIOUS_WIN_MASK : 0);
		stream.write(dat);

		for (int symbol = 0; symbol < symbolCount; symbol++) {
			final int result = symbolToResultMap[symbol];
			IoUtils.writeNumberBinary(stream, result, RESULT_SIZE);
		}
		
		for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
			final int[] probabilities = symbolProbabilities.get((long) modelIndex);
			int oneCount = 0;
			
			for (int symbol = 0; symbol < symbolCount; symbol++) {
				final int probability = probabilities[symbol];
				
				if (probability == 1) {
					oneCount++;
					
					if (oneCount >= MAX_PROBABILITY_ONE_COUNT) {
						writeOneCount (stream, oneCount);
						oneCount = 0;
					}
				}
				else {
					if (oneCount > 0) {
						writeOneCount (stream, oneCount);
						oneCount = 0;
					}
					
					writeProbability (stream, probability);
				}
			}
			
			if (oneCount > 0) {
				writeOneCount (stream, oneCount);
			}
		}
	}

	private void writeProbability(final OutputStream stream, final int probability) throws IOException {
		if (probability < MAX_SMALL_PROBABILITY) {
			final int movedPobability = probability - RangeBase.MIN_SYMBOL_PROBABILITY;
			
			stream.write(SMALL_PROBABILITY_ID_VALUE | movedPobability);
		}
		else {
			if (probability < MAX_LARGE_PROBABILITY) {
				final int movedPobability = probability - MAX_SMALL_PROBABILITY;
				
				stream.write(LARGE_PROBABILITY_ID_VALUE | (movedPobability >> 8));
				stream.write(movedPobability & 0xFF);
			}
			else {
				final int movedPobability = probability - RangeBase.MIN_SYMBOL_PROBABILITY;
				
				stream.write(FULL_PROBABILITY_ID);
				IoUtils.writeNumberBinary(stream, movedPobability, FULL_PROBABILITY_SIZE);
			}
		}
	}

	private void writeOneCount(final OutputStream stream, final int count) throws IOException {
		stream.write (ONE_COUNT_ID_VALUE | count);
	}

	private void writeSymbolsToStream(final OutputStream stream) throws IOException {
		final RangeEncoder encoder = new RangeEncoder();
		
		final long itemCount = table.getDefinition().getTableIndexCount();
		final long blockCount = (itemCount + BLOCK_INDEX_LENGTH - 1) >> BLOCK_INDEX_EXPONENT;
		
		blockPositions = new HugeLongArray(blockCount + 1);
		
		final ITableIterator it = table.getIterator();
		final Position position = new Position();
		
		long blockPos = 0;
		blockPositions.setAt(0, blockPos);
		
		for (long blockIndex = 0; blockIndex < blockCount; blockIndex++) {
			final CRC32 crc = new CRC32();
			final ChecksumStream checksumStream = new ChecksumStream(crc);
			final ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
			encoder.initialize(memoryStream);

			modelSelector.resetSymbols();
			
			for (int i = 0; i < BLOCK_INDEX_LENGTH && it.isValid(); i++, it.next()) {
				final int result = it.getResult();
				
				if (result != TableResult.ILLEGAL) {
					final int symbol = resultToSymbolMap.get(result);
					
					it.fillPosition(position);
					final long positionLabel = modelSelector.getModelIndex(position);
					modelSelector.addSymbol(position, symbol);
					
					final EnumerationProbabilityModel probabilityModel = probabilityModelMap.get(positionLabel);
					
					encoder.encodeSymbol(probabilityModel, symbol);
					
					it.fillPosition(position);
					updateCrcWithResult(checksumStream, position, result);
				}
			}
			
			encoder.close();
			
			IoUtils.writeNumberBinary(memoryStream, crc.getValue(), CRC_SIZE);
			
			blockPos += memoryStream.size();
			blockPositions.setAt(blockIndex + 1, blockPos);
			
			memoryStream.writeTo(stream);
		}
	}

}
