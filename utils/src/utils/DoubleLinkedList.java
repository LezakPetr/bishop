package utils;


public class DoubleLinkedList<T> {
	
	private DoubleLinkedItem<T> head;
	private DoubleLinkedItem<T> tail;
	
	public ISimpleIterator<T> addItemToEnd() {
		final DoubleLinkedItem<T> item = new DoubleLinkedItem<T>();
		
		item.setPrevItem(tail);
		
		if (tail != null)
			tail.setNextItem(item);
		else {
			head = item;
		}
		
		tail = item;
		
		return item;
	}
	
	public void removeItem (final ISimpleIterator<T> it) {
		final DoubleLinkedItem<T> item = (DoubleLinkedItem<T>) it;
		final DoubleLinkedItem<T> prevItem = item.getPrevItem();
		final DoubleLinkedItem<T> nextItem = item.getNextItem();
		
		if (prevItem != null)
			prevItem.setNextItem(nextItem);
		else
			head = nextItem;
		
		if (nextItem != null)
			nextItem.setPrevItem(prevItem);
		else
			tail = prevItem;
	}

	public ISimpleIterator<T> getFirstItem() {
		return head;
	}
	
	public ISimpleIterator<T> getLastItem() {
		return tail;
	}
}
