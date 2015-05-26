package magicCalculator;

import bishop.base.BitBoard;
import bishop.base.Square;

public class MagicSettings {

	private int square;
	private long mask;
	private int depth;

	public long getMask() {
		return mask;
	}

	public void setMask(long mask) {
		this.mask = mask;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getSquare() {
		return square;
	}

	public void setSquare(final int square) {
		this.square = square;
	}
	
	/**
	 * Returns first bit in coefficient that cannot change the table.
	 * @return first spare bit
	 */
	public int getFirstSpareBit() {
		return Square.LAST - BitBoard.getFirstSquare(mask);
	}
	
	public long getSpareBitMask() {
		final int firstSpareBit = getFirstSpareBit();
		long mask = 0;
		
		for (int i = firstSpareBit; i < Square.LAST; i++)
			mask |= BitBoard.getSquareMask(i);
		
		return mask;
	}
}
