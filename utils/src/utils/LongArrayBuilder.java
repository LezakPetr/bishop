package utils;

import java.util.Arrays;
import java.util.function.IntToLongFunction;

public class LongArrayBuilder {
	private final long[] array;

	private LongArrayBuilder(final int size) {
		array = new long[size];
	}

	public LongArrayBuilder put (final int index, final int value) {
		array[index] = value;

		return this;
	}

	public LongArrayBuilder fill (final long value)
	{
		Arrays.fill(array, value);

		return this;
	}

	public LongArrayBuilder fill (final IntToLongFunction valueProducer)
	{
		for (int i= 0; i < array.length; i++)
			array[i] = valueProducer.applyAsLong(i);

		return this;
	}

	public long[] build() {
		return array;
	}

	public static LongArrayBuilder create (final int size)
	{
		return new LongArrayBuilder(size);
	}
}
