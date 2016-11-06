package bishop.engine;

public class PawnStructure {

	private final long whitePawnMask;
	private final long blackPawnMask;
	
	
	public PawnStructure (final long whitePawnMask, final long blackPawnMask) {
		this.whitePawnMask = whitePawnMask;
		this.blackPawnMask = blackPawnMask;
	}
	
	public boolean equals (final Object obj) {
		if (obj == null || !(obj instanceof PawnStructure))
			return false;
		
		final PawnStructure that = (PawnStructure) obj;
		
		return this.whitePawnMask == that.whitePawnMask && this.blackPawnMask == that.blackPawnMask;
	}
	
	public int hashCode() {
		return 31 * Long.hashCode(whitePawnMask) + Long.hashCode(blackPawnMask);
	}
}
