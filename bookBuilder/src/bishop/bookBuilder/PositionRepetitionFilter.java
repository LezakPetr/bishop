package bishop.bookBuilder;

import java.util.Arrays;

/*
 * Repetition filter is a table that holds number of positions with hashes fitting into
 * given index. Repetition counts are stored in bytes, but are considered as unsigned.
 * The counts saturates at FILTER_SATURATION.
 * Repetition filter is used to reduce number of positions stored in memory, because
 * count of position stored in filter is at least same as real count of given
 * position (it can be greater because of collisions) or it saturates. 
 */
public class PositionRepetitionFilter {
	private static final int FILTER_SIZE = 1 << 24;
	private static final int FILTER_MASK = FILTER_SIZE - 1;
	
	private static final int FILTER_SATURATION = 0xFF;

	private final byte[] repetitionFilter = new byte[FILTER_SIZE];
	
	public void clear() {
		Arrays.fill(repetitionFilter, (byte) 0);
	}

	// Returns index of position in repetition filter
	private int getRepetitionFilterIndex(final long hash) {
		return (int) (hash & FILTER_MASK);
	}

	// Adds one position into the filter
	public void addPositionToRepetitionFilter (final long hash) {
		final int index = getRepetitionFilterIndex(hash);
		
		if ((repetitionFilter[index] & FILTER_SATURATION) < FILTER_SATURATION)
			repetitionFilter[index]++;
	}

	public boolean canPositionBeRepeatedAtLeast (final long hash, final int minCount) {
		final int filterIndex = getRepetitionFilterIndex(hash);
		final int filterCount = repetitionFilter[filterIndex] & FILTER_SATURATION;
		
		return filterCount >= minCount || filterCount == FILTER_SATURATION;
	}
}
