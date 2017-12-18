package collections;

import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Immutable map which keys are mappable to ordinal numbers.
 * @author Ing. Petr Ležák
 */
public class ImmutableOrdinalMap<K, V> {
	
	private final ToIntFunction<? super K> keyMapper;
	private final Object[] values;
	
	private ImmutableOrdinalMap(final ToIntFunction<? super K> keyMapper, final Object[] values) {
		this.keyMapper = keyMapper;
		this.values = values;
	}

	public static class Builder<K, V> {
		private final ToIntFunction<? super K> keyMapper;
		private final Object[] values;
		
		public Builder(final ToIntFunction<? super K> keyMapper, final int count) {
			this.keyMapper = keyMapper;
			this.values = new Object[count];
		}
		
		public Builder<K, V> put (final K key, final V value) {
			Objects.requireNonNull(key);
			Objects.requireNonNull(value);
			
			values[keyMapper.applyAsInt(key)] = value;
			
			return this;
		}
		
		public ImmutableOrdinalMap<K, V> build() {
			return new ImmutableOrdinalMap<>(keyMapper, values);
		}
	}
	
	public static <K extends Enum<K>, V> Builder<K, V> forEnum(final Class<K> clazz) {
		return new Builder<>(Enum::ordinal, clazz.getEnumConstants().length);
	}
	
	public static <K, V> Builder<K, V> forMapper(final ToIntFunction<? super K> keyMapper, final int count) {
		return new Builder<>(keyMapper, count);
	}

	@SuppressWarnings("unchecked")
	public V get(final K key) {
		final int index = keyMapper.applyAsInt(key);
		
		return (V) values[index];
	}

}
