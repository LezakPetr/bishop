package collections;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class ImmutableList<E> extends ImmutableListBase<E> {
	
	public static class Builder<E> {
		
		private static final Object[] EMPTY_DATA = new Object[0];
		
		private Object[] data;
		private int size;
		private boolean closed;
		
		public Builder() {
			this.data = EMPTY_DATA;
		}
		
		public Builder<E> withCapacity (final int capacity) {
			checkNotClosed();
			
			if (data.length < capacity)
				data = Arrays.copyOf(data, capacity);
			
			return this;
		}
		
		public Builder<E> add (final E value) {
			checkNotClosed();
			ensureCapacity (size + 1);
			
			data[size] = value;
			size++;
			
			return this;
		}
		
		public Builder<E> addTimes (final E value, final int count) {
			if (count < 0)
				throw new IllegalArgumentException("Negative count " + count);
			
			checkNotClosed();
			ensureCapacity (size + count);
			
			Arrays.fill(data, size, size + count, value);
			size += count;
			
			return this;
		}


		private void ensureCapacity(final int capacity) {
			if (data.length < capacity) {
				final int newCapacity = Math.max((int) Math.min(2L * data.length, Integer.MAX_VALUE), capacity);
				data = Arrays.copyOf(data, newCapacity);
			}
		}
		
		private void checkNotClosed() {
			if (closed)
				throw new RuntimeException("Builder already closed");
		}
		
		public List<E> build() {
			checkNotClosed();
			
			if (data.length != size)
				data = Arrays.copyOf(data, size);
			
			closed = true;
			
			return new ImmutableList<>(data);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static ImmutableList EMPTY_LIST = new ImmutableList(new Object[0]);
	
	private final Object[] data;
	
	private ImmutableList(final Object[] data) {
		this.data = data;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E get(final int index) {
		try {
			return (E) data[index];
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			// Optimization - we optimistically assume that the index is in range.
			// Because the size of array is same as the size of the list we don't have to check boundaries
			// explicitly and we just catch the exception from the array.
			throw new IndexOutOfBoundsException("Index " + index + " out of bound, size = " + size());
		}
	}

	@Override
	public int size() {
		return data.length;
	}


	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		final int newSize = toIndex - fromIndex;
		
		if (fromIndex < 0 || toIndex > size() || newSize < 0)
			throw new IndexOutOfBoundsException("Illegal range <" + fromIndex + ", " + toIndex + ") with size = " + size());
		
		return new ImmutableSubList<>(data, fromIndex, newSize);
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> emptyList() {
		return EMPTY_LIST;
	}
	
	@SafeVarargs
	public static <E> List<E> of (final E... elements) {
		final int size = elements.length;
		
		if (size == 0)
			return emptyList();
		else {
			final Object[] data = new Object[size];
			System.arraycopy(elements, 0, data, 0, size);
			
			return new ImmutableList<>(data);
		}
	}
	
	public static <E> Builder<E> builder() {
		return new Builder<>();
	}
	
}
