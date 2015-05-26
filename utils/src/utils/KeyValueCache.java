package utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;


/**
 * Cache of key value mapping.
 * @author Ing. Petr Ležák
 *
 * @param <K> key
 * @param <V> value
 */
public class KeyValueCache<K extends ICopyable<K>, V> implements KeyValueMapping<K, V> {

	private final KeyValueMapping<K, V> baseMapping;
	private final HashMap<K, WeakReference<V>> definitionMap;
	
	/**
	 * Creates cache.
	 * @param baseMapping mapping that is used if key is not in the map
	 */
	public KeyValueCache(final KeyValueMapping<K, V> baseMapping) {
		this.baseMapping = baseMapping;
		this.definitionMap = new HashMap<K, WeakReference<V>>();
	}
	
	@Override
	public synchronized V getValue(final K key) {
		V value = null;
		
		if (definitionMap.containsKey(key)) {
			final WeakReference<V> valueRef = definitionMap.get(key);
			value = valueRef.get();			
		}
		
		if (value == null) {
			value = baseMapping.getValue(key);
			final WeakReference<V> definitionRef = new WeakReference<V>(value);
			
			definitionMap.put(key.copy(), definitionRef);
		}
		
		return value;
	}
	
}
