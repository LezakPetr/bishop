package bishop.tablebase;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;


public class FullTablePage implements ITablePage {
	private final long offset;
	private final ShortBuffer results;
	
	public FullTablePage (final long offset, final int size) {
		this.offset = offset;
		this.results = ByteBuffer.allocateDirect(Short.BYTES * size).asShortBuffer();
	}
	
	public int getResult (final long index) {
		return results.get((int) (index - offset));
	}
	
	public void setResult (final long index, final int result) {
		results.put((int) (index - offset), (short) result);
	}
	
	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return results.capacity();
	}

	public void read(final ITableIteratorRead it) {
		for (int i = 0; i < results.capacity(); i++) {
			results.put(i, (short) it.getResult());
			it.next();
		}
	}

	public void clear() {
		for (int i = 0; i < results.capacity(); i++) {
			results.put(i, (short) TableResult.ILLEGAL);
		}
	}
}
