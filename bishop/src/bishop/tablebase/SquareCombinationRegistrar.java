package bishop.tablebase;

import utils.KeyValueCache;
import utils.KeyValueMapping;

public class SquareCombinationRegistrar {
	
	private final KeyValueMapping<SquareCombinationKey, SquareCombination> creator = new KeyValueMapping<SquareCombinationKey, SquareCombination>() {
		@Override
		public SquareCombination getValue(final SquareCombinationKey key) {
			return new SquareCombination(key);
		}
	};
	
	private final KeyValueMapping<SquareCombinationKey, SquareCombination> mapping;
	
	public SquareCombinationRegistrar() {
		mapping = new KeyValueCache<SquareCombinationKey, SquareCombination>(creator);
	}
	
	public SquareCombination getDefinition(final SquareCombinationKey key) {
		return mapping.getValue(key);
	}
	
	
	private static SquareCombinationRegistrar instance = new SquareCombinationRegistrar();
	
	public static SquareCombinationRegistrar getInstance() {
		return instance;
	}
}
