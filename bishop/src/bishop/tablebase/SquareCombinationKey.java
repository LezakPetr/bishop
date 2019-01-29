package bishop.tablebase;

import bishop.base.BitBoard;
import utils.ICopyable;

/**
 * Immutable key which defines SquareCombination.
 * @author Ing. Petr Ležák
 */
public final class SquareCombinationKey implements ICopyable<SquareCombinationKey> {
	
	private final CombinationDefinition definition;
	private final long allowedSquares;
	
	public SquareCombinationKey (final CombinationDefinition definition, final long allowedSquares) {
		this.definition = definition;
		this.allowedSquares = allowedSquares;
	}

	public CombinationDefinition getDefinition() {
		return definition;
	}

	public long getAllowedSquares() {
		return allowedSquares;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof SquareCombinationKey))
			return false;
		
		final SquareCombinationKey cmp = (SquareCombinationKey) obj;
		
		if (!this.definition.equals(cmp.definition))
			return false;
		
		return this.allowedSquares == cmp.allowedSquares;
	}
	
	@Override
	public int hashCode() {
		return (int) allowedSquares ^ (int) (allowedSquares >> 32) ^ definition.hashCode();
	}

	@Override
	public SquareCombinationKey copy() {
		return this;   // Immutable class
	}
	
	@Override
	public String toString() {
		return definition.toString() + ", Allowed squares: " + BitBoard.toString(allowedSquares); 
	}
}
