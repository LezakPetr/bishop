package bishop.engine;

public class PawnStructureCache {
	
	
	private static final int CACHE_BITS = 16;
	private static final int CACHE_SIZE = 1 << CACHE_BITS;
	private static final int CACHE_MASK = CACHE_SIZE - 1;
	
	private final PawnStructureData[] dataCache;
	private long hitCount;
	private long missCount;
	
	private static int getIndex (final PawnStructure structure) {
		final long whitePawnMask = structure.getWhitePawnMask();
		final long blackPawnMask = structure.getBlackPawnMask();
		
		int index = 0;
		
		index += 3 * (int) whitePawnMask;
		index += 7 * (int) (whitePawnMask >> 16);
		index += 15 * (int) (whitePawnMask >> 32);
		index += 31 * (int) (whitePawnMask >> 48);

		index += 63 * (int) blackPawnMask;
		index += 127 * (int) (blackPawnMask >> 16);
		index += 255 * (int) (blackPawnMask >> 32);
		index += 511 * (int) (blackPawnMask >> 48);

		return index & CACHE_MASK;
	}
	
	public PawnStructureCache() {
		dataCache = new PawnStructureData[CACHE_SIZE];
		
		for (int i = 0; i < CACHE_SIZE; i++)
			dataCache[i] = new PawnStructureData();
	}

	public void getData (final PawnStructure structure, final PawnStructureData data) {
		final int index = getIndex(structure);
		final PawnStructureData dataFromCache = dataCache[index];
		
		if (!structure.equals(dataFromCache.getStructure()))
		{
			dataFromCache.calculate(structure);
			missCount++;
		}
		else
			hitCount++;
		
		data.assign(dataFromCache);
	}
	
	public void printStatistics() {
		final double totalCount = hitCount + missCount;
		final double hitPercent = 100.0 * (hitCount / totalCount);
		
		System.out.println ("Pawn structure cache hit: " + hitPercent + "%");
	}

}
