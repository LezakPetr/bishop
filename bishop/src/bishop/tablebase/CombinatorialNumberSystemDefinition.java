package bishop.tablebase;

import utils.ICopyable;

/**
 * Definition of combinatorial number system
 * @author Ing. Petr Ležák
 */
public final class CombinatorialNumberSystemDefinition implements ICopyable<CombinatorialNumberSystemDefinition> {
	
	private final int n;
	private final int k;

	/**
	 * Creates definition C(n, k).
	 * @param n number of available items
	 * @param k number of selected items
	 */
	public CombinatorialNumberSystemDefinition(final int n, final int k) {
		this.n = n;
		this.k = k;
	}

	public int getN() {
		return n;
	}

	public int getK() {
		return k;
	}

	@Override
	public CombinatorialNumberSystemDefinition copy() {
		return this;   // Unmodifiable class
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof CombinatorialNumberSystemDefinition))
			return false;
		
		final CombinatorialNumberSystemDefinition cmp = (CombinatorialNumberSystemDefinition) obj;
		
		return this.k == cmp.k && this.n == cmp.n;
	}
	
	@Override
	public int hashCode() {
		return n ^ Integer.rotateLeft(k, 16);
	}
}
