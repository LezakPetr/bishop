package collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;


abstract public class ImmutableListBase<E> implements List<E> {
	private class ListIteratorImpl implements ListIterator<E> {
		private int index;
		
		public ListIteratorImpl (final int index) {
			this.index = index;
		}
		
		@Override
		public void add(final E e) {
			throw getModificationException();
		}

		@Override
		public boolean hasNext() {
			return nextIndex() < size();
		}

		@Override
		public boolean hasPrevious() {
			return previousIndex() >= 0;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			
			final E value = get(nextIndex());
			index++;
			
			return value;
		}

		@Override
		public int nextIndex() {
			return index;
		}

		@Override
		public E previous() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			
			final E value = get(previousIndex());
			index--;
			
			return value;

		}

		@Override
		public int previousIndex() {
			return index - 1;
		}

		@Override
		public void remove() {
			throw getModificationException();
		}

		@Override
		public void set(final E e) {
			throw getModificationException();
		}
	}

	private UnsupportedOperationException getModificationException() {
		return new UnsupportedOperationException ("Modification of ImmutableList is not possible");
	}

	@Override
	public boolean add(final E arg0) {
		throw getModificationException();
	}

	@Override
	public void add(final int index, final E element) {
		throw getModificationException();
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		throw getModificationException();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		throw getModificationException();
	}

	@Override
	public void clear() {
		throw getModificationException();
	}

	@Override
	public boolean contains(final Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (Object o: c) {
			if (!contains(o))
				return false;
		}
		
		return true;
	}

	@Override
	public int indexOf(final Object o) {
		final int size = this.size();
		
		for (int i = 0; i < size; i++) {
			if (Objects.equals(o, get(i)))
				return i;
		}
		
		return -1;
	}
	
	@Override
	public int lastIndexOf(final Object o) {
		if (o == null)
			return -1;
		
		for (int i = size() - 1; i >= 0; i--) {
			if (Objects.equals(o, get(i)))
				return i;
		}
		
		return -1;
	}


	@Override
	public boolean isEmpty() {
		return size() <= 0;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		return new ListIteratorImpl(index);
	}

	@Override
	public boolean remove(final Object o) {
		throw getModificationException();
	}

	@Override
	public E remove(final int index) {
		throw getModificationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw getModificationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw getModificationException();
	}

	@Override
	public E set(int index, E element) {
		throw getModificationException();
	}

	@Override
	public Object[] toArray() {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		throw new RuntimeException("Method not implemented");
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("[");
		
		for (int i = 0; i < size(); i++)
			result.append(get(i).toString());
		
		result.append("]");
		
		return result.toString();
	}
}
