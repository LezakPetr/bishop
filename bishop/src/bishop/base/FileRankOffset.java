package bishop.base;

public final class FileRankOffset {
	
	private final int fileOffset;
	private final int rankOffset;
	
	
	/**
	 * Creates offset.
	 * @param fileOffset file offset
	 * @param rankOffset rank offset
	 */
	public FileRankOffset (final int fileOffset, final int rankOffset) {
		this.fileOffset = fileOffset;
		this.rankOffset = rankOffset;
	}

	/**
	 * Returns file offset.
	 * @return file offset
	 */
	public int getFileOffset() {
		return fileOffset;
	}

	/**
	 * Returns rank offset.
	 * @return rank offset
	 */
	public int getRankOffset() {
		return rankOffset;
	}

}
