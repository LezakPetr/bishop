package bishop.engine;

import bishop.base.Move;
import bishop.base.Position;

import java.util.concurrent.atomic.AtomicLongArray;

public class BestMoveHashTableImpl implements IBestMoveHashTable {
	private int exponent;
	private AtomicLongArray table;
	private long indexMask;

	private static final int HORIZON_DIFF = 0;

	private static final int HORIZON_SHIFT              = 0;
	private static final int COMPRESSED_BEST_MOVE_SHIFT = 8;

	private static final long HORIZON_MASK              = 0x00000000000000FFL;
	private static final long COMPRESSED_BEST_MOVE_MASK = 0x00000000007FFF00L;
	private static final long HASH_MASK                 = 0xFFFFFFFFFF800000L;

	public static final int MIN_EXPONENT = 0;
	public static final int MAX_EXPONENT = 31;
	public static final int ITEM_SIZE =  Long.BYTES;   // Size of hash item [B]

	public BestMoveHashTableImpl(final int exponent) {
		resize(exponent);
	}

	public void resize (final int exponent) {
		if (exponent < MIN_EXPONENT || exponent > MAX_EXPONENT)
			throw new RuntimeException("Exponent out of range: " + exponent);

		if (this.exponent != exponent) {
			this.exponent = exponent;

			final int recordCount = 1 << exponent;

			table = new AtomicLongArray(recordCount);
			indexMask = recordCount - 1;
		}
	}

	/**
	 * Returns hash record for given position.
	 * @param position position
	 * @return true if record is found, false if not
	 */
	public int getRecord (final Position position) {
		return getRecord (position.getHash());
	}

	/**
	 * Returns hash record for given position.
	 * @param hash hash code of position
	 * @return true if record is found, false if not
	 */
	public int getRecord (final long hash) {
		final int index = (int) (hash & indexMask);
		final long tableItem = table.get(index);

		if (((tableItem ^ hash) & HASH_MASK) == 0)
			return (int) ((tableItem & COMPRESSED_BEST_MOVE_MASK) >>> COMPRESSED_BEST_MOVE_SHIFT);
		else
			return Move.NONE_COMPRESSED_MOVE;
	}

	/**
	 * Updates hash record for given position.
	 * @param position position
	 * @param horizon horizon
	 * @param compressedBestMove compressed best move
	 */
	public void updateRecord (final Position position, final int horizon, final int compressedBestMove) {
		updateRecord(position.getHash(), horizon, compressedBestMove);
	}

	/**
	 * Updates hash record for given position.
	 * @param hash hash code of position
	 * @param horizon horizon
	 * @param compressedBestMove compressed best move
	 */
	public void updateRecord (final long hash, final int horizon, final int compressedBestMove) {
		final int index = (int) (hash & indexMask);

		long data = 0;
		data |= ((long) horizon << HORIZON_SHIFT) & HORIZON_MASK;
		data |= ((long) compressedBestMove << COMPRESSED_BEST_MOVE_SHIFT) & COMPRESSED_BEST_MOVE_MASK;
		data |= hash & HASH_MASK;

		while (true) {
			final long oldTableItem = table.get(index);
			final int oldHorizon = (int) ((oldTableItem & HORIZON_MASK) >> HORIZON_SHIFT);

			if (horizon + HORIZON_DIFF < oldHorizon)
				break;

			if (table.compareAndSet(index, oldTableItem, data))
				break;
		}
	}

	/**
	 * Clears the table.
	 */
	public void clear() {
		// Non-volatile writes
		for (int i = table.length() - 1; i > 0; i--)
			table.lazySet(i, 0);

		// Volatile write to flush cache
		table.set(0, 0);
	}
}
