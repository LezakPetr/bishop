package collections;

import java.util.Arrays;

public class IntArray {
	private static final int[] EMPTY_ARRAY = new int[0];

	private int[] items;
	private int size;

	public IntArray () {
		this.items = EMPTY_ARRAY;
	}

	public IntArray (final int capacity) {
		this.items = new int[capacity];
	}

	public int getSize() {
		return size;
	}

	public int getItem (final int index) {
		assert index >= 0 && index < size;

		return items[index];
	}

	public void append (final int item) {
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
