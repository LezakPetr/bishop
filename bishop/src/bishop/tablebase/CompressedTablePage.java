package bishop.tablebase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CompressedTablePage implements ITablePage {
	private final long offset;
	private final byte[] symbols;
	private final short[] symbolToResultMap;
	private Map<Short, Byte> resultToSymbolMap;
	private int nextSymbol;
	
	private static final byte SYMBOL_ILLEGAL = 0;
	
	public CompressedTablePage (final long offset, final int size) {
		this.offset = offset;
		this.symbols = new byte[size];
		
		this.resultToSymbolMap = new HashMap<>();
		this.resultToSymbolMap.put((short) TableResult.ILLEGAL, SYMBOL_ILLEGAL);
		
		this.symbolToResultMap = new short[1 << Byte.SIZE];
		this.symbolToResultMap[SYMBOL_ILLEGAL] = (short) TableResult.ILLEGAL;
		this.nextSymbol = SYMBOL_ILLEGAL + 1;
	}
	
	public int getResult (final long index) {
		final int symbol = symbols[(int) (index - offset)] & 0xFF;
		
		return symbolToResultMap[symbol];
	}
	
	public void setResult (final long index, final int result) {
		Byte symbol = resultToSymbolMap.get((short) result);
		
		if (symbol == null) {
			symbol = (byte) nextSymbol;
			
			resultToSymbolMap.put((short) result, symbol);
			symbolToResultMap[nextSymbol] = (short) result;
			
			nextSymbol++;
		}
		
		symbols[(int) (index - offset)] = symbol;
	}
	
	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return symbols.length;
	}

	public void read(final ITableIteratorRead it) {
		for (int i = 0; i < symbols.length; i++) {
			setResult(offset + i, it.getResult());
			it.next();
		}
	}
	
	@Override
	public void clear() {
		Arrays.fill(symbols, SYMBOL_ILLEGAL);
	}
}
