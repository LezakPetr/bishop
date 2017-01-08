package bishop.tablebase;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CompressedTablePage implements ITablePage {
	
	private static final int MAX_SLICE_COUNT = 16;
	
	private final long offset;
	private final long size;
	private final int sliceSize;
	private int sliceCount;
	private final LongBuffer[] slices;
	private short[] symbolToResultMap;
	private Map<Short, Short> resultToSymbolMap;
	private int nextSymbol;
	
	private static final short SYMBOL_ILLEGAL = 0;
	
	public CompressedTablePage (final long offset, final int size) {
		this.offset = offset;
		this.size = size;
		this.sliceSize = (int) Utils.divideRoundUp(size, Long.SIZE);
		this.slices = new LongBuffer[MAX_SLICE_COUNT];
		this.slices[0] = allocateBuffer();
		this.sliceCount = 1;
		
		this.resultToSymbolMap = new HashMap<>();
		this.resultToSymbolMap.put((short) TableResult.ILLEGAL, SYMBOL_ILLEGAL);
		
		this.symbolToResultMap = new short[2];
		this.symbolToResultMap[SYMBOL_ILLEGAL] = (short) TableResult.ILLEGAL;
		this.nextSymbol = SYMBOL_ILLEGAL + 1;
	}
	
	private LongBuffer allocateBuffer() {
		return ByteBuffer.allocateDirect(Long.BYTES * sliceSize).asLongBuffer();
	}

	public int getResult (final long index) {
		final long baseIndex = index - offset;
		final long bitIndex = baseIndex % Long.SIZE;
		final int cellIndex = (int) (baseIndex / Long.SIZE);
		int symbol = 0;
		
		for (int i = 0; i < sliceCount; i++) {
			symbol += ((slices[i].get(cellIndex) >>> bitIndex) & 0x01) << i;
		}
		
		return symbolToResultMap[symbol];
	}
	
	public void setResult (final long index, final int result) {
		Short symbol = resultToSymbolMap.get((short) result);
		
		if (symbol == null) {
			symbol = (short) nextSymbol;
						
			if (nextSymbol >= symbolToResultMap.length) {
				slices[sliceCount] = allocateBuffer();
				sliceCount++;
				
				symbolToResultMap = Arrays.copyOf(symbolToResultMap, 1 << sliceCount);
			}
			
			resultToSymbolMap.put((short) result, symbol);
			symbolToResultMap[nextSymbol] = (short) result;
			
			nextSymbol++;
		}
		
		final long baseIndex = index - offset;
		final long bitIndex = baseIndex % Long.SIZE;
		final int cellIndex = (int) (baseIndex / Long.SIZE);
		final long mask = 1L << bitIndex;
		
		for (int i = 0; i < sliceCount; i++) {
			long cell = slices[i].get(cellIndex);
			
			if (((symbol >> i) & 0x01) != 0)
				cell |= mask;
			else
				cell &= ~mask;
			
			slices[i].put(cellIndex, cell);
		}
	}
	
	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return size;
	}

	public void read(final ITableIteratorRead it) {
		for (int i = 0; i < size; i++) {
			setResult(offset + i, it.getResult());
			it.next();
		}
	}
	
	@Override
	public void clear() {
		for (int i = 0; i < sliceSize; i++)
			slices[0].put(i, 0);
		
		for (int i = 1; i < MAX_SLICE_COUNT; i++)
			slices[i] = null;
	}
}
