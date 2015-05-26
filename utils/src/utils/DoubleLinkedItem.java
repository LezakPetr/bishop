package utils;

public class DoubleLinkedItem<T> implements ISimpleIterator<T> {
	
	private DoubleLinkedItem<T> prevItem;
	private DoubleLinkedItem<T> nextItem;
	private T data;

	public T getData() {
		return data;
	}

	public void setData(final T data) {
		this.data = data;
	}

	public DoubleLinkedItem<T> getPrevItem() {
		return prevItem;
	}

	public void setPrevItem(final DoubleLinkedItem<T> prevItem) {
		this.prevItem = prevItem;
	}

	public DoubleLinkedItem<T> getNextItem() {
		return nextItem;
	}

	public void setNextItem(final DoubleLinkedItem<T> nextItem) {
		this.nextItem = nextItem;
	}
	
}
