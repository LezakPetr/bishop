package bishop.tablebase;

import bishop.base.MaterialHash;
import utils.Mixer;

public class BlockKey {
	private final MaterialHash materialHash;
	private final long blockIndex;
	
	public BlockKey(final MaterialHash materialHash, final long blockIndex) {
		this.materialHash = materialHash;
		this.blockIndex = blockIndex;
	}

	public MaterialHash getMaterialHash() {
		return materialHash;
	}

	public long getBlockIndex() {
		return blockIndex;
	}
	
	@Override
	public int hashCode() {
		return Mixer.mixLongToInt(materialHash.getHash() + 31 * blockIndex);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof BlockKey))
			return false;
		
		final BlockKey cmpKey = (BlockKey) obj;
		
		return this.materialHash.equals(cmpKey.materialHash) && this.blockIndex == cmpKey.blockIndex;
	}

}
