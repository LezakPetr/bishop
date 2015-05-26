package bishopTests;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import org.junit.Test;

import bishop.base.BitBoardCombinator;
import bishop.base.CrossDirection;
import bishop.base.LineIndexer;
import bishop.base.Square;

public class LineIndexerTest {

	@Test
	public void testIndices() {
		final Set<Integer> indices = new HashSet<Integer>();
		
		for (int direction = CrossDirection.FIRST; direction < CrossDirection.LAST; direction++) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				final long mask = LineIndexer.calculateDirectionMask(direction, square);
				
				for (BitBoardCombinator combinator = new BitBoardCombinator(mask); combinator.hasNextCombination(); ) {
					final long combination = combinator.getNextCombination();
					final int index = LineIndexer.getLineIndex(direction, square, combination);
					
					if (!indices.add(index))
						Assert.fail("Dupplicate combination: direction " + direction + ", square " + square + ", combination " + combination);
				}
			}
		}
	}
}
