package bishop.tablebase;


import utils.ICopyable;
import utils.KeyValueCache;
import utils.KeyValueMapping;
import bishop.base.MaterialHash;


public class TableDefinitionRegistrar {
	
	private static final class Key implements ICopyable<Key> {
		public final int version;
		public final MaterialHash materialHash;
		
		public Key (final int version, final MaterialHash materialHash) {
			this.version = version;
			this.materialHash = materialHash;
		}

		@Override
		public Key copy() {
			return new Key (version, materialHash.copy());
		}
		
		@Override
		public int hashCode() {
			return version ^ materialHash.hashCode();
		}
		
		public boolean equals (final Object obj) {
			if (!(obj instanceof Key))
				return false;
			
			final Key cmp = (Key) obj;
			
			return this.version == cmp.version && this.materialHash.equals(cmp.materialHash);
		}
	}
	
	private final KeyValueMapping<Key, TableDefinition> creator = new KeyValueMapping<Key, TableDefinition>() {
		@Override
		public TableDefinition getValue(final Key key) {
			return new TableDefinition(key.version, key.materialHash);
		}
	};
	
	private final KeyValueMapping<Key, TableDefinition> mapping;
	
	public TableDefinitionRegistrar() {
		mapping = new KeyValueCache<Key, TableDefinition>(creator);
	}
	
	public TableDefinition getDefinition(final int version, final MaterialHash hash) {
		return mapping.getValue(new Key (version, hash));
	}
	
	
	private static TableDefinitionRegistrar instance = new TableDefinitionRegistrar();
	
	public static TableDefinitionRegistrar getInstance() {
		return instance;
	}
}
