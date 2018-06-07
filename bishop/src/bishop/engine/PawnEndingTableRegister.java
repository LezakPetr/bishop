package bishop.engine;

import collections.RecordCache;


public class PawnEndingTableRegister {

    private static final int CACHE_BITS = 10;

    private final TablebasePositionEvaluator tablebaseEvaluator;
    private final RecordCache<PawnEndingKey, PawnEndingTable> cache = new RecordCache<>(CACHE_BITS);

    public PawnEndingTableRegister(final TablebasePositionEvaluator tablebaseEvaluator) {
        this.tablebaseEvaluator = tablebaseEvaluator;
    }

    public PawnEndingTable getTable (final PawnEndingKey key) {
        final PawnEndingTable tableFromCache = cache.get(key);

        if (tableFromCache != null)
            return tableFromCache;

        final PawnEndingTable calculatedTable = PawnEndingEvaluator.calculateTable (tablebaseEvaluator, this, key);

        cache.put(key, calculatedTable);

        return calculatedTable;
    }


}
