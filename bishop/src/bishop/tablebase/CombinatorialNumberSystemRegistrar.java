package bishop.tablebase;

import utils.KeyValueCache;
import utils.KeyValueMapping;

/**
 * Central registrar of combinatorial number system.
 * @author Ing. Petr Ležák
 */
public class CombinatorialNumberSystemRegistrar {

	private final KeyValueMapping<CombinatorialNumberSystemDefinition, ICombinatorialNumberSystem> mapping;
	
	/**
	 * Private constructor.
	 */
	private CombinatorialNumberSystemRegistrar() {
		mapping = new KeyValueCache<>(CombinatorialNumberSystemRegistrar::getCombinatorialNumberSystemForDefinition);
	}

	private static ICombinatorialNumberSystem getCombinatorialNumberSystemForDefinition(final CombinatorialNumberSystemDefinition key) {
		switch (key.getK()) {
			case 0:
				return new ZeroCombinatorialNumberSystem(key.getN());

			case 1:
				return new OneCombinatorialNumberSystem(key.getN());

			case 2:
				return new TwoCombinatorialNumberSystem(key.getN());

			default:
				return new GeneralCombinatorialNumberSystem(key.getN(), key.getK());
		}
	}

	/**
	 * Returns number system for given definition.
	 * @param definition number system definition
	 * @return combinatorial number system
	 */
	public ICombinatorialNumberSystem getDefinition(final CombinatorialNumberSystemDefinition definition) {
		return mapping.getValue(definition);
	}
	
	
	private static final CombinatorialNumberSystemRegistrar instance = new CombinatorialNumberSystemRegistrar();
	
	/**
	 * Returns instance of this registrar.
	 * @return registrar instance
	 */
	public static CombinatorialNumberSystemRegistrar getInstance() {
		return instance;
	}
}
