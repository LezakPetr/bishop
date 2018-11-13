package bishop.engine;

import utils.Mixer;

import java.util.function.ToLongFunction;

public class PawnStructureCache {

	private static final int CACHE_BITS = 20;
	private static final int CACHE_SIZE = 1 << CACHE_BITS;
	private static final int CACHE_MASK = (CACHE_SIZE - 1) << 1;

	private final ToLongFunction<PawnStructure> calculateCombinedEvaluation;
	private final long[] table;
	private long hitCount;
	private long missCount;
	
	private static long getHash (final PawnStructure structure) {
		final long whitePawnMask = structure.getWhitePawnMask();
		final long blackPawnMask = structure.getBlackPawnMask();

		final long index = Mixer.mixLong(whitePawnMask + 31 * blackPawnMask);

		return index;
	}
	
	public PawnStructureCache(final ToLongFunction<PawnStructure> calculateCombinedEvaluation) {
		this.calculateCombinedEvaluation = calculateCombinedEvaluation;
		this.table = new long[2 * CACHE_SIZE];
	}

	public long getCombinedEvaluation (final PawnStructure structure) {
		final long hash = getHash(structure);
		final int baseIndex = (int) hash & CACHE_MASK;
		final long hashFromTable = table[baseIndex];
		final long combinedEvaluation;
		
		if (hashFromTable == hash) {
			combinedEvaluation = table[baseIndex + 1];
			hitCount++;
		}
		else {
			combinedEvaluation = calculateCombinedEvaluation.applyAsLong(structure);

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
