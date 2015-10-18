package bishop.tablebase;

import java.util.Arrays;


public class TablePage {
	private final long offset;
	private final short[] results;
	
	public TablePage (final long offset, final int size) {
		this.offset = offset;
		this.results = new short[size];
		Arrays.fill(results, (short) TableResult.ILLEGAL);
	}
	
	public int getResult (final long index) {
		return results[(int) (index - offset)];
	}
	
	public void setResult (final long index, final int result) {
		results[(int) (index - offset)] = (short) result;
	}
	
	public long getOffset() {
		return offset;
	}

	public void read(final ITableIteratorRead it) {
		for (int i = 0; i < results.length; i++) {
			results[i] = (short) it.getResult();
			it.next();
		}
	}

}
