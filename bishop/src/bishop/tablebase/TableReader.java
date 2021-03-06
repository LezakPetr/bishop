package bishop.tablebase;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.CRC32;

import range.IProbabilityModel;
import range.ProbabilityModelFactory;
import range.RangeBase;
import range.RangeDecoder;
import utils.*;
import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.Piece;
import bishop.base.Position;
import bishop.base.PositionValidator;

public class TableReader extends TableIo {
	
	private final File file;
	private TableDefinition tableDefinition;
	private long blockPositionSize;
	private int blockIndexExponent;
	private int blockIndexCount;
	private int bytesPerBlockPosition;
	private HugeLongArray blockPositions;
	private long headerLength;

	private final RangeDecoder decoder;
	private final PositionValidator validator;
	private final Position position;
	
	private static final int MIN_VERSION = 0;
	private static final int MAX_VERSION = 0;
	
	public TableReader(final File file) {
		this.file = file;
				
		decoder = new RangeDecoder();
		validator = new PositionValidator();
		position = new Position();

		validator.setPosition(position);
		headerLength = -1;
		
		try {
			prefetchHeader();
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot read header", ex);
		}
	}
	
	private void prefetchHeader() throws IOException {
		final FileInputStream fileStream = new FileInputStream(file);

		try (CountingInputStream countingStream = new CountingInputStream(fileStream)) {
			readHeaderFromStream(countingStream);
		}
	}

	public void readTable () throws IOException {

		try (FileInputStream fileStream = new FileInputStream(file)) {
			skipHeader(fileStream);

			table = new CompressedMemoryTable(tableDefinition, new SymbolToResultMapWithIllegal(symbolToResultMap));

			readBlockLenghts(fileStream);
			readSymbolsFromStream(fileStream);
		}
	}
	
	private void skipHeader (final InputStream stream) throws IOException {
		IoUtils.skip(stream, headerLength);
	}
	
	public long getBlockIndex(final long tableIndex) {
		return tableIndex >> blockIndexExponent;
	}

	public void readBlockWithResult (final long tableIndex) throws IOException {
		final FileInputStream fileStream = new FileInputStream(file);
		
		// Allocate buffer for 2 block positions because they are read byte by byte. We don't have to
		// allocate buffer for the whole block because it is read by single bulk read.
		final BufferedInputStream bufferedStream = new BufferedInputStream(fileStream, 2 * bytesPerBlockPosition);

		try (CountingInputStream countingStream = new CountingInputStream(bufferedStream)) {
			final long blockIndex = getBlockIndex(tableIndex);
			final long blockOffset = blockIndex << blockIndexExponent;

			IoUtils.skip(countingStream, headerLength + blockIndex * bytesPerBlockPosition);

			final long prevPos = IoUtils.readUnsignedNumberBinary(countingStream, bytesPerBlockPosition);
			final long nextPos = IoUtils.readUnsignedNumberBinary(countingStream, bytesPerBlockPosition);
			final int blockLength = (int) (nextPos - prevPos);

			IoUtils.skip(countingStream, (blockPositionSize - blockIndex - 2) * bytesPerBlockPosition + prevPos);

			table = new CompressedMemoryTable(tableDefinition, blockOffset, blockIndexCount, new SymbolToResultMapWithIllegal(symbolToResultMap));

			readOneBlock(countingStream, table.getIterator(), blockIndexCount, blockLength);
		}
	}
	
	private void readSymbolsFromStream(final InputStream stream) throws IOException {
		final ITableIterator it = table.getIterator();
		for (long i = 1; i < blockPositions.getSize(); i++) {
			final int blockLength = (int) (blockPositions.getAt(i) - blockPositions.getAt(i-1));
			
			readOneBlock(stream, it, blockIndexCount, blockLength);
		}
	}

	private void readOneBlock(final InputStream stream, final ITableIterator it, final int blockIndexCount, final int blockLength) throws IOException {
		final byte[] blockData = IoUtils.readByteArray(stream, blockLength);
		final int dataSize = blockLength - CRC_SIZE;
		final MemoryInputStream memoryStream = new MemoryInputStream(blockData, 0, dataSize);
		
		final MemoryInputStream crcStream = new MemoryInputStream(blockData, dataSize, CRC_SIZE);
		final long expectedCrc = IoUtils.readUnsignedNumberBinary(crcStream, CRC_SIZE);
		final CRC32 crcChecksum = new CRC32();
		final ChecksumStream checksumStream = new ChecksumStream(crcChecksum);
				
		decoder.initialize(memoryStream);
		
		modelSelector.resetSymbols();
		
		for (int i = 0; i < blockIndexCount && it.isValid(); i++, it.next()) {
			it.fillPosition(position);
			
			final boolean isValid = table.getDefinition().hasSameCountOfPieces(position) && validator.checkPositionForTablebase();
			int result = TableResult.ILLEGAL;
			
			if (isValid) {
				final int positionLabel = modelSelector.getModelIndex(position);
				final IProbabilityModel probabilityModel = probabilityModelMap.get(positionLabel);
				final int symbol = decoder.decodeSymbol(probabilityModel);
				result = symbolToResultMap.symbolToResult(symbol);
				
				modelSelector.addSymbol(position, symbol);
				updateCrcWithResult(checksumStream, position, result);
			}
			
			it.setResult(result);
		}
		
		decoder.close();
		
		final long calculatedCrc = crcChecksum.getValue();
		
		if (calculatedCrc != expectedCrc)
			throw new RuntimeException("Wrong CRC: expected " + expectedCrc + ", calculated " + calculatedCrc);
	}

	private void readHeaderFromStream(final CountingInputStream stream) throws IOException {
		if (!IoUtils.hasExpectedBytes(stream, HEADER_MAGIC))
			throw new RuntimeException("Wrong magic");
		
		final byte version = IoUtils.readByteBinary(stream);
		
		if (version < MIN_VERSION || version > MAX_VERSION)
			throw new RuntimeException("Unknown version of the table");
		
		/*final byte flags = */IoUtils.readByteBinary(stream);
		
		readTableDefinition(version, stream);
		readProbabilityModel(stream, version);
		readBlockLengthConstants(stream);
		
		headerLength = stream.getPosition();
	}

	private void readBlockLengthConstants(final InputStream stream) throws IOException {
		blockPositionSize = IoUtils.readUnsignedNumberBinary(stream, LAYER_LENGTH_BYTES);
		blockIndexExponent = IoUtils.readByteBinary(stream);
		bytesPerBlockPosition = IoUtils.readByteBinary(stream);
		
		blockIndexCount = 1 << blockIndexExponent;
	}
	
	private void readBlockLenghts(final InputStream stream) throws IOException {
		blockPositions = new HugeLongArray(blockPositionSize);
				
		for (long blockIndex = 0; blockIndex < blockPositionSize; blockIndex++) {
			final long position = IoUtils.readUnsignedNumberBinary(stream, bytesPerBlockPosition);
			
			blockPositions.setAt(blockIndex, position); 
		}
	}

	private void readTableDefinition(final int version, final InputStream stream) throws IOException {
		final int onTurn = IoUtils.readByteBinary(stream);
		
		if (!Color.isValid(onTurn))
			throw new IOException("Wrong onTurn");
		
		final byte combinationCount = IoUtils.readByteBinary(stream);
		final MaterialHash materialHash = new MaterialHash();
				
		for (int i = 0; i < combinationCount; i++) {
			final byte definitionData = IoUtils.readByteBinary(stream);
			final CombinationDefinition definition = CombinationDefinition.fromData(definitionData);
			final Piece piece = definition.getPiece();
			
			materialHash.addPiece(piece.getColor(), piece.getPieceType(), definition.getCount());
		}
		
		materialHash.setOnTurn(onTurn);
		
		tableDefinition = TableDefinitionRegistrar.getInstance().getDefinition(version, materialHash);
	}
	
	private void readProbabilityModel(final InputStream stream, final byte version) throws IOException {
		final int symbolCount;
		final int modelCount;
		
		switch (version) {
			case 0:
				symbolCount = (int) IoUtils.readUnsignedNumberBinary(stream, SYMBOL_COUNT_SIZE);
				
				final byte dat = IoUtils.readByteBinary(stream);
				final int historyLength = (dat & HISTORY_LENGTH_MASK) >>> HISTORY_LENGTH_SHIFT;
				final boolean previousWin = (dat & PREVIOUS_WIN_MASK) != 0;
				
				readSymbolToResultMap(stream, symbolCount);
				modelSelector = new ClassificationProbabilityModelSelector(symbolToResultMap, historyLength, previousWin, tableDefinition.getMaterialHash());

				modelCount = modelSelector.getModelCount();
				break;
				
			default:
				throw new IOException("Unknown version");
		}		
		
		symbolProbabilities = new HashMap<>();

		probabilityModelMap = new HashMap<>();
		
		for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
			final int[] probabilities = new int[symbolCount];
			int symbol = 0;
			
			while (symbol < symbolCount) {
				final int head = IoUtils.readByteBinary(stream) & 0xFF;
				
				if ((head & SMALL_PROBABILITY_ID_MASK) == SMALL_PROBABILITY_ID_VALUE) {
					final int movedProbability = head & SMALL_PROBABILITY_VALUE_MASK;
					
					probabilities[symbol] = movedProbability + RangeBase.MIN_SYMBOL_PROBABILITY;
					symbol++;
				}
				else {
					if ((head & LARGE_PROBABILITY_ID_MASK) == LARGE_PROBABILITY_ID_VALUE) {
						final int secondByte = IoUtils.readByteBinary(stream) & 0xFF;
						final int movedProbability = ((head & LARGE_PROBABILITY_VALUE_MASK) << 8) + secondByte; 
						
						probabilities[symbol] = movedProbability + MAX_SMALL_PROBABILITY;
						symbol++;
					}
					else {
						 if (head == FULL_PROBABILITY_ID) {
							final int movedProbability = (int) IoUtils.readUnsignedNumberBinary(stream, FULL_PROBABILITY_SIZE);
							 
							probabilities[symbol] = movedProbability + RangeBase.MIN_SYMBOL_PROBABILITY;
							symbol++;
						 }
						 else {
							 final int count = head & ONE_COUNT_VALUE_MASK;
							 
							 for (int i = 0; i < count; i++) {
								 probabilities[symbol] = 1;
								 symbol++;
							 }
						 }
					}
				}
			}
			
			if (symbol != symbolCount) {
				throw new RuntimeException("Wrong symbol count");
			}
			
			symbolProbabilities.put(modelIndex, probabilities);
			
			final IProbabilityModel probabilityModel = ProbabilityModelFactory.fromProbabilities(probabilities);
			probabilityModelMap.put(modelIndex, probabilityModel);
		}
	}

	private void readSymbolToResultMap(final InputStream stream, final int symbolCount) throws IOException {
		final int[] symbolToResultTable = new int[symbolCount];

		for (int symbol = 0; symbol < symbolCount; symbol++) {
			int result = (int) IoUtils.readUnsignedNumberBinary(stream, RESULT_SIZE);
			
			if (result > Short.MAX_VALUE) {
				result -= 1 << (Byte.SIZE * RESULT_SIZE);
			}

			symbolToResultTable[symbol] = result;
		}
		
		symbolToResultMap = new SortedSymbolToResultMap(symbolToResultTable);
	}

	public TableDefinition getDefinition() {
		return tableDefinition;
	}

}
