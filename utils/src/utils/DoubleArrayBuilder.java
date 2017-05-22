package utils;

import java.util.Arrays;
import java.util.stream.DoubleStream;

public class DoubleArrayBuilder {
	private final double[] array;
	private final boolean[] filled;
	
	public DoubleArrayBuilder(final int size) {
		array = new double[size];
		filled = new boolean[size];
	}
	
	public DoubleArrayBuilder(final int size, final double defaultValue) {
		array = new double[size];
		filled = new boolean[size];
		
		Arrays.fill(array, defaultValue);
	}
	
	public DoubleArrayBuilder put (final int index, final double value) {
		if (filled[index])
			throw new RuntimeException("Dupplicate index: " + value);
		
		array[index] = value;
		filled[index] = true;
		
		return this;
	}
	
	public double[] build() {
		return array;
	}
	
	public DoubleStream stream() {
		return Arrays.stream(array);
	}

}
