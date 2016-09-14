package bishop.evaluationStatistics;

import bishop.base.MaterialHash;

public class PawnComplement {
	private final MaterialHash materialHash;
	private final double complement;
	private final long totalCount;
	
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
