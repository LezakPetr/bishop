package utils;

public class HugeLongArray {
	
	private final long[] array;
	
	public HugeLongArray(final long size) {
		this.array = new long[(int) size];
	}
	
	public long getAt (final long index) {
		return array[(int) index];
	}
	
	public void setAt (final long index, final long value) {
		array[(int) index] = value;
	}

	public long getSize() {
		return array.length;
	}
}
