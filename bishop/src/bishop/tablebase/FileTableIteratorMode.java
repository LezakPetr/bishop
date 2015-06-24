package bishop.tablebase;

public enum FileTableIteratorMode {
	
	FULL (0, 64),
	COMPRESSED (64, 128),
	DRAW (128, 192),
	ILLEGAL (192, 256);
	
	private final int minDescriptor;
	private final int maxDescriptor;
	
	private FileTableIteratorMode (final int minDescriptor, final int maxDescriptor) {
		this.minDescriptor = minDescriptor;
		this.maxDescriptor = maxDescriptor;
	}

	public int getMinDescriptor() {
		return minDescriptor;
	}

	public int getMaxDescriptor() {
		return maxDescriptor;
	}

	public static FileTableIteratorMode forDescriptor(final int descriptor) {
		for (FileTableIteratorMode mode: values()) {
			if (descriptor >= mode.minDescriptor && descriptor < mode.maxDescriptor)
				return mode;
		}
		
		throw new RuntimeException("Unknown mode for descriptor " + descriptor);
	}

	public int getCount(final int descriptor) {
		return descriptor - minDescriptor + 1;
	}
	
	/**
	 * Returns maximal count (exclusive).
	 * @return maximal count (exclusive)
	 */
	public int getMaxCount() {
		return maxDescriptor - minDescriptor + 1;
	}
	
	public int getDescriptor (final int count) {
		return count + minDescriptor - 1;
	}
	
}
