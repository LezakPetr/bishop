package bishop.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

public class MoveList implements Iterable<Move> {

	private int[] data;
	private int size;

	public MoveList() {
		data = null;
		size = 0;
	}

	public MoveList(final int capacity) {
		data = null;
		size = 0;

		reserve(capacity);
	}

	public void reserve(final int capacity) {
		if (capacity > 0) {
			if (data == null || capacity > data.length) {
				final int[] newData = new int[capacity];

				if (size > 0)
					System.arraycopy(data, 0, newData, 0, size);

				data = newData;
			}
		}
	}

	/**
	 * Returns number of moves in the list.
	 * 
	 * @return size of the list
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Removes all moves from the list.
	 */
	public void clear() {
		size = 0;
	}

	/**
	 * Adds move to the end of the list.
	 * 
	 * @param move
	 *            added move
	 */
	public void add(final Move move) {
		reserve(size + 1);

		data[size] = move.getData();
		size++;
	}

	public void addRange(final MoveList list, final int begin, final int count) {
		if (count > 0) {
			reserve(this.size + count);

			System.arraycopy(list.data, begin, this.data, this.size, count);
			size += count;
		}
	}

	/**
	 * Adds all moves from given list to the end of this list.
	 * 
	 * @param list
	 *            added list
	 */
	public void addAll(final MoveList list) {
		addRange(list, 0, list.size);
	}

	public void assignToMove(final int index, final Move move) {
		assert index >= 0 && index < size;

		move.setData(data[index]);
	}

	public Move get(final int index) {
		final Move move = new Move();
		assignToMove(index, move);

		return move;
	}

	public int getCompressedMove(final int index) {
		assert index >= 0 && index < size;

		return data[index] & Move.COMPRESSED_MOVE_MASK;
	}
	
	public void set (final int index, final Move move) {
		if (index < 0 || index >= size)
			throw new ArrayIndexOutOfBoundsException(index);
		
		data[index] = move.getData();
	}

	public MoveList copy() {
		final MoveList list = new MoveList();
		list.assign(this);

		return list;
	}

	public void assign(final MoveList orig) {
		this.clear();
		this.addAll(orig);
	}
	
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		final Move move = new Move();

		for (int i = 0; i < size; i++) {
			if (i > 0)
				builder.append(' ');

			move.setData(data[i]);
			builder.append(move.toString());
		}

		return builder.toString();
	}

	public String toString(final Position position, final INotationWriter notation) {
		final Position currentPosition = position.copy();
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter writer = new PrintWriter(stringWriter);

		final Move move = new Move();

		for (int i = 0; i < size; i++) {
			if (i > 0)
				writer.append(' ');

			move.setData(data[i]);
			notation.writeMove(writer, currentPosition, move);
			currentPosition.makeMove(move);
		}

		writer.flush();

		return stringWriter.toString();
	}

	public Iterator<Move> iterator() {
		return new MoveListIterator(this);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof MoveList))
			return false;

		final MoveList cmp = (MoveList) obj;

		if (this.size != cmp.size)
			return false;

		for (int i = 0; i < size; i++) {
			if (this.data[i] != cmp.data[i])
				return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int code = 0;

		for (int i = 0; i < size; i++)
			code = Integer.rotateLeft(code, 7) ^ data[i];

		return code;
	}

	/**
	 * Finds first occurrence of given move or return -1 if not found.
	 * 
	 * @param move
	 *            move to find
	 * @return index of move
	 */
	public int indexOf(final Move move) {
		final int expecetdData = move.getData();

		for (int i = 0; i < size; i++) {
			if (data[i] == expecetdData)
				return i;
		}

		return -1;
	}
	
	public void sort() {
		final Move minMove = new Move();
		final Move checkedMove = new Move();
		int minIndex;
		
		for (int i = 0; i < size - 1; i++) {
			minMove.setData(data[i]);
			minIndex = i;
			
			for (int j = i + 1; j < size; j++) {
				checkedMove.setData(data[j]);
				
				if (checkedMove.compareTo (minMove) < 0) {
					minMove.assign(checkedMove);
					minIndex = j;
				}
			}
			
			data[minIndex] = data[i];
			data[i] = minMove.getData();
		}
	}
}
