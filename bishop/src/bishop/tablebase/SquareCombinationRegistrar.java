package bishop.tablebase;

import utils.KeyValueCache;
import utils.KeyValueMapping;

public class SquareCombinationRegistrar {
	
	private final KeyValueMapping<SquareCombinationKey, SquareCombination> mapping;
	
	public SquareCombinationRegistrar() {
		mapping = new KeyValueCache<>(SquareCombination::new);
	}
	
	public SquareCombination getDefinition(final SquareCombinationKey key) {
		return mapping.getValue(key);
	}
	
	
	private static final SquareCombinationRegistrar instance = new SquareCombinationRegistrar();
	
	public static SquareCombinationRegistrar getInstance() {
		return instance;
	}
}
