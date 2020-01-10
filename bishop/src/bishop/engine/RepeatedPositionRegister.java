package bishop.engine;

import bishop.base.Move;
import bishop.base.Position;

/**
 * Register that can detect repetition of positions.
 * The positions can be pushed and popped from the register, the register behaves like a stack.
 * Each pushed position can be stored (it will count to the repetition) or not.
 */
public class RepeatedPositionRegister {
	
	private long[] hashStack;   // Contains hashes of positions; valid index range <0; hashCounts[size-1])
	private int[] intervalBeginStack;   // Stack of begin indices of position hashes to check repeats
	private int[] hashCounts;   // Number of hashes up to given position indexx
	private int size;   // Number of positions


	public void clear() {
		size = 0;
	}
	
	/**
	 * Reserves capacity of the register. Clears data.
	 * @param capacity capacity of the register
	 */
	public void clearAndReserve(final int capacity) {
		if (hashStack == null || capacity > hashStack.length) {
			hashStack = new long[capacity];
			intervalBeginStack = new int[capacity];
			hashCounts = new int[capacity];
		}
		
		clear();
	}
	
	public void pushAll(final RepeatedPositionRegister origRegister) {
		if (origRegister.size > 0) {
			final int thisHashCount = getHashCount();
			final int origHashCount = origRegister.getHashCount();
			System.arraycopy(origRegister.hashStack, 0, this.hashStack, thisHashCount, origHashCount);

			final int lastIntervalBegin = (this.size > 0) ? this.intervalBeginStack[this.size - 1] : 0;

			for (int i = 0; i < origRegister.size; i++) {
				final int origBegin = origRegister.intervalBeginStack[i];
				final int newIndex = i + this.size;

				this.intervalBeginStack[newIndex] = (origBegin == 0) ? lastIntervalBegin : thisHashCount + origBegin;
				this.hashCounts[newIndex] = thisHashCount + origRegister.hashCounts[i];
			}
			
			this.size += origRegister.size;
		}
	}

	private int getHashCount() {
		return (size > 0) ? hashCounts[size - 1] : 0;
	}

	public void pushPosition(final Position position, final Move previousMove) {
		pushPosition(position, previousMove, true);
	}

	public void pushPosition(final Position position, final Move previousMove, final boolean store) {
		final int hashCount = getHashCount();

		if (size > 0 && previousMove.isReversible())
			intervalBeginStack[size] = intervalBeginStack[size - 1];
		else
			intervalBeginStack[size] = hashCount;

		if (store) {
			hashStack[hashCount] = position.getHash();
			hashCounts[size] = hashCount + 1;
		}
		else
			hashCounts[size] = hashCount;

		size++;
	}
	
	public void popPosition() {
		size--;
	}
	
	public int getSize() {
		return size;
	}

	/**
	 * Returns number position occurrence in the register.
	 * @param position position to check
	 * @return position count (may return 0 instead of 1)
	 */
	public int getPositionRepeatCount(final Position position) {
		int count = 0;
		
		if (size > 0) {
			final long hash = position.getHash();
			final int begin = intervalBeginStack[size - 1];
			final int hashCount = getHashCount();

			for (int i = begin; i < hashCount; i++) {
				if (hashStack[i] == hash)
					count++;
			}
		}
		
		return count;
	}

	public boolean isDrawByRepetition(final Position position, final int depth) {
		final int positionRepeatCount = getPositionRepeatCount(position);
		
		return positionRepeatCount >= 3 || (positionRepeatCount >= 2 && depth > 2);
	}

	public RepeatedPositionRegister copy() {
		final RepeatedPositionRegister result = new RepeatedPositionRegister();
		result.clearAndReserve(this.getSize());
		result.pushAll(this);
		
		return result;
	}

}
