package utils;

/**
 * Mapping between key and some immutable value that uses a lot of memory or time to compute. 
 * @author Ing. Petr Ležák
 *
 * @param <K> key
 * @param <V> value
 */
public interface KeyValueMapping<K, V> {
	/**
	 * Returns value for given key.
	 * @param key key
	 * @return value
	 */
	public V getValue (final K key);
}
