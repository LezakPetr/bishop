package bishop.engine;

import collections.RecordCache;


public class PawnEndingTableRegister {

    private static final int CACHE_BITS = 10;

    private final RecordCache<PawnEndingKey, PawnEndingTable> cache = new RecordCache<>(CACHE_BITS);

    public PawnEndingTable getTable (final PawnEndingKey key) {
        final PawnEndingTable tableFromCache = cache.get(key);

        if (tableFromCache != null)
            return tableFromCache;

        final PawnEndingTable calculatedTable = PawnEndingEvaluator.calculateTable(this, key);

        cache.put(key, calculatedTable);

        return calculatedTable;
    }


}
