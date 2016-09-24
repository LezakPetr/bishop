package utils;

import java.util.Arrays;

public class IntArrayBuilder {
	
	private final int[] array;
	private final boolean[] filled;
	
	public IntArrayBuilder(final int size) {
		this(size, 0);
	}
	
	public IntArrayBuilder(final int size, final int defaultValue) {
		array = new int[size];
		filled = new boolean[size];
		
		if (defaultValue != 0)
			Arrays.fill(array, defaultValue);
	}
	
	public IntArrayBuilder put (final int index, final int value) {
		if (filled[index])
			throw new RuntimeException("Dupplicate index: " + value);
		
		array[index] = value;
		filled[index] = true;
		
		return this;
	}
	
	public int[] build() {
		return array;
	}
}
