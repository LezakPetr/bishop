package collections;

import java.util.Arrays;

public class DoubleArray {
	private double[] items;
	private int size;

	public DoubleArray (final int capacity) {
		this.items = new double[capacity];
	}

	public int getSize() {
		return size;
	}

	public double getItem (final int index) {
		assert index >= 0 && index < size;

		return items[index];
	}

	public void setItem (final int index, final double value) {
		assert index >= 0 && index < size;

		items[index] = value;
	}

	public void append (final double item) {
		growTo (size + 1);

		items[size] = item;
		size++;
	}

	private void growTo (final int capacity) {
		final long newCapacity = Math.min (Math.max(capacity, 2L * size), Integer.MAX_VALUE);

		ensureCapacity((int) newCapacity);
	}

	public void ensureCapacity (final int capacity) {
		if (capacity > items.length)
			items = Arrays.copyOf(items, capacity);
	}
}
