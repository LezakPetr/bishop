package bishop.evaluationStatistics;

import bishop.base.MaterialHash;

/**
 * Evaluation of given material from white's perspective. 
 * @author Ing. Petr Ležák
 */
public class PawnComplement {
	private final MaterialHash materialHash;   // Evaluated material
	private final double complement;   // Evaluation (in multiply of pawn) of material
	private final long totalCount;   // Number of positions used to calculate the evaluation
	
	public PawnComplement(final MaterialHash materialHash, final double complement, final long totalCount) {
		this.materialHash = materialHash;
		this.complement = complement;
		this.totalCount = totalCount;
	}

	public MaterialHash getMaterialHash() {
		return materialHash;
	}

	public double getComplement() {
		return complement;
	}

	public long getTotalCount() {
		return totalCount;
	}
}
