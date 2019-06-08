package bishop.engine;

import bishop.base.Move;
import bishop.base.Position;

public class RepeatedPositionRegister {
	
	private long[] hashStack;
	private int[] intervalBeginStack;   // Stack of begin indices of positions to check repeats
	private int size;

	
	/**
	 * Reserves capacity of the register. Clears data.
	 * @param capacity capacity of the register
	 */
	public void clearAndReserve(final int capacity) {
		if (hashStack == null || capacity > hashStack.length) {
			hashStack = new long[capacity];
			intervalBeginStack = new int[capacity];
		}
		
		size = 0;
	}
	
	public void pushAll(final RepeatedPositionRegister origRegister) {
		if (origRegister.size > 0) {
			System.arraycopy(origRegister.hashStack, 0, this.hashStack, this.size, origRegister.size);
			System.arraycopy(origRegister.intervalBeginStack, 0, this.intervalBeginStack, this.size, origRegister.size);
			
			this.size += origRegister.size;
		}
	}

	public void pushPosition(final Position position, final Move previousMove) {
		hashStack[size] = position.getHash();
		
		if (size > 0 && previousMove.isReversible()) {
			intervalBeginStack[size] = intervalBeginStack[size - 1];
		}
		else {
			intervalBeginStack[size] = size;
		}
		
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
			
			if (size - begin >= 4) {   // Position repetition can occur after 4 moves
				for (int i = begin; i < size; i++) {
					if (hashStack[i] == hash)
						count++;
				}
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
