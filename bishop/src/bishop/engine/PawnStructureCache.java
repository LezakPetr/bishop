package bishop.engine;

import utils.Mixer;

import java.util.function.LongBinaryOperator;

public class PawnStructureCache {

	private static final int CACHE_BITS = 20;
	private static final int CACHE_SIZE = 1 << CACHE_BITS;
	private static final int CACHE_MASK = (CACHE_SIZE - 1) << 1;

	private final LongBinaryOperator calculateCombinedEvaluation;
	private final long[] table;
	private long hitCount;
	private long missCount;
	
	private static long getHash (final long whitePawnMask, final long blackPawnMask) {
		return Mixer.mixLong(whitePawnMask + 31 * blackPawnMask);
	}
	
	public PawnStructureCache(final LongBinaryOperator calculateCombinedEvaluation) {
		this.calculateCombinedEvaluation = calculateCombinedEvaluation;
		this.table = new long[2 * CACHE_SIZE];
	}

	public long getCombinedEvaluation (final long whitePawnMask, final long blackPawnMask) {
		final long hash = getHash(whitePawnMask, blackPawnMask);
		final int baseIndex = (int) hash & CACHE_MASK;
		final long hashFromTable = table[baseIndex];
		final long combinedEvaluation;
		
		if (hashFromTable == hash) {
			combinedEvaluation = table[baseIndex + 1];
			hitCount++;
		}
		else {
			combinedEvaluation = calculateCombinedEvaluation.applyAsLong(whitePawnMask, blackPawnMask);

			table[baseIndex] = hash;
			table[baseIndex + 1] = combinedEvaluation;
			missCount++;
		}

		return combinedEvaluation;
	}

	public void printStatistics() {
		final double totalCount = hitCount + missCount;
		final double hitPercent = 100.0 * (hitCount / totalCount);
		
		System.out.println ("Pawn structure cache hit: " + hitPercent + "%");
	}

}
