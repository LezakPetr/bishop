package collections;

import java.util.List;

final class ImmutableSubList<E> extends ImmutableListBase<E> {
	
	private final Object[] data;
	private final int offset;
	private final int listSize;


	ImmutableSubList (final Object[] data, final int offset, final int listSize) {
		this.data = data;
		this.offset = offset;
		this.listSize = listSize;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(final int index) {
		if (index < 0 || index >= listSize)
			throw new IndexOutOfBoundsException("Index " + index + " out of bound, size = " + listSize);
		
		return (E) data[index];
	}

	@Override
	public int size() {
		return listSize;
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		final int newSize = toIndex - fromIndex;
		
		if (fromIndex < 0 || toIndex > listSize || newSize < 0)
			throw new IndexOutOfBoundsException("Illegal range <" + fromIndex + ", " + toIndex + ") with size = " + listSize);
		
		return new ImmutableSubList<>(data, offset + fromIndex, newSize);
	}


}
