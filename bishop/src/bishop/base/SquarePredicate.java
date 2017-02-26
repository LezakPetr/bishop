package bishop.base;

import java.util.function.IntPredicate;

public class SquarePredicate {
	
	private final long mask;

	public SquarePredicate(final IntPredicate predicate) {
		long accumulator = BitBoard.EMPTY;
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			if (predicate.test(square))
				accumulator |= BitBoard.getSquareMask(square);
		}
		
		mask = accumulator;
	}

	public long getMask() {
		return mask;
	}
}
