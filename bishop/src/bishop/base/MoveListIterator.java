package bishop.base;

import java.util.Iterator;

public class MoveListIterator implements Iterator<Move> {
	
	private final MoveList moveList;
	private int index;
	
	public MoveListIterator(final MoveList moveList) {
		this.moveList = moveList;
		this.index = 0;
	}

	public boolean hasNext() {
		return this.index < moveList.getSize();
	}

	public Move next() {
		final Move result = moveList.get(index);
		index++;
		
		return result;
	}

	public void remove() {
		throw new RuntimeException("Removing from move list is not permitted");
	}

}
