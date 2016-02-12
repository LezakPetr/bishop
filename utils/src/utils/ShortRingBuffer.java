package utils;

public class ShortRingBuffer {

	private final short[] data;
	private final int mask;
	private int begin;
	private int size;
	
	public ShortRingBuffer (final int exponent) {
		final int capacity = 1 << exponent;
		mask = capacity - 1;
		data = new short[capacity];
		begin = 0;
		size = 0;
	}
	
	private int getDataIndex (final int index) {
		return (begin + index) & mask;
	}
	
	public short getAt (final int index) {
		if (index < 0 || index >= size)
			throw new RuntimeException("Index out of range " + index);
		
		final int dataIndex = getDataIndex(index);
		
		return data[dataIndex];
	}
	
	public void push (final short value) {
		if (size >= data.length)
			throw new RuntimeException("Ring buffer is full");

		final int dataIndex = getDataIndex(size);
		
		data[dataIndex] = value;
		size++;
	}
	
	public void pop() {
		pop(1);
	}
	
	public void pop(final int count) {
		if (size < count)
			throw new RuntimeException("Not enough items in buffer");
		
		begin = getDataIndex(count);
		size -= count;
	}


	public int getSize() {
		return size;
	}

	public int getCapacity() {
		return data.length;
	}

}
