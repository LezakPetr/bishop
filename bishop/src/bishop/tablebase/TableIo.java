package bishop.tablebase;

import java.io.IOException;
import java.util.Map;

import range.EnumerationProbabilityModel;
import range.IProbabilityModel;
import range.RangeBase;
import utils.ChecksumStream;
import utils.IoUtils;
import bishop.base.Position;

public class TableIo {
	
	protected static final byte[] HEADER_MAGIC = { (byte) 'T', (byte) 'B', (byte) 'B', (byte) 'S' };
	
	protected static final int SYMBOL_COUNT_SIZE = 2;
	protected static final int PROBABILITY_MODEL_COUNT_SIZE = 4;
	protected static final int POSITION_LABEL_SIZE = 8;
	protected static final int RESULT_SIZE = 2;
	protected static final int HASH_SIZE = 8;
	protected static final int CRC_SIZE = 4;
	protected static final int FULL_PROBABILITY_SIZE = 2;
	
	protected static final int MAX_PROBABILITY_ONE_COUNT = 63;
	protected static final int MAX_SMALL_PROBABILITY = RangeBase.MIN_SYMBOL_PROBABILITY + (1 << 7);
	protected static final int MAX_LARGE_PROBABILITY = MAX_SMALL_PROBABILITY + (1 << 14);
	
	protected static final int SMALL_PROBABILITY_ID_VALUE = 0x00;
	protected static final int SMALL_PROBABILITY_ID_MASK = 0x80;
	protected static final int SMALL_PROBABILITY_VALUE_MASK = 0x7F;

	protected static final int LARGE_PROBABILITY_ID_VALUE = 0x80;
	protected static final int LARGE_PROBABILITY_ID_MASK = 0xC0;
	protected static final int LARGE_PROBABILITY_VALUE_MASK = 0x3F;
	
	protected static final int FULL_PROBABILITY_ID = 0xC0;
	
	protected static final int ONE_COUNT_ID_VALUE = 0xC0;
	protected static final int ONE_COUNT_VALUE_MASK = 0x3F;
	
	protected static final int LAYER_LENGTH_BYTES = Long.SIZE / Byte.SIZE;
	
	protected static final int HISTORY_LENGTH_MASK = 0xFE;
	protected static final int HISTORY_LENGTH_SHIFT = 1;
	protected static final int PREVIOUS_WIN_MASK = 0x01;
	
	protected ITable table;
	protected ISymbolToResultMap symbolToResultMap;
	protected Map<Integer, int[]> symbolProbabilities;
	protected IProbabilityModelSelector modelSelector;
	
	protected Map<Integer, IProbabilityModel> probabilityModelMap;
	
	
	public ITable getTable() {
		return table;
	}

	public void setTable(final ITable table) {
		this.table = table;
	}
	
	protected static void updateCrcWithResult(final ChecksumStream checksumStream, final Position position, final int result) throws IOException {
		IoUtils.writeNumberBinary(checksumStream, position.getHash(), HASH_SIZE);
		IoUtils.writeNumberBinary(checksumStream, result, RESULT_SIZE);
	}
	
}

