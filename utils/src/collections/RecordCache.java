package collections;

import java.util.ArrayList;
import java.util.List;

public class RecordCache<K, V> {
    private static class BlockRecord<K, V> {
        private K key;
        private V value;
    }

    private final int indexMask;
    private final List<BlockRecord<K, V>> records;

    public RecordCache(final int cacheBits) {
        final int cacheSize = 1 << cacheBits;
        this.records = new ArrayList<>(cacheSize);

        for (int i = 0; i < cacheSize; i++)
            this.records.add(new BlockRecord<>());

        this.indexMask = cacheSize - 1;
    }

    private int getRecordIndex (final K key) {
        return key.hashCode() & indexMask;
    }

    public V get (final K key) {
        final int index = getRecordIndex(key);
        final BlockRecord<K, V> record = records.get(index);

        synchronized (record) {
            if (record.key == null || !record.key.equals(key))
                return null;

            return record.value;
        }
    }

    public void put (final K key, final V value) {
        final int index = getRecordIndex(key);
        final BlockRecord record = records.get(index);

        synchronized (record) {
            record.key = key;
            record.value = value;
        }
    }

}
