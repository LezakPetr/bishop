package bishop.engine;

import java.util.Arrays;

import bishop.base.Position;

public final class HashTableImpl implements IHashTable {
	
	private int exponent;
	private long[] table;
	private long indexMask;
	private final Object[] monitorArray;
	private int monitorShift;
	
	private static final int MONITOR_EXPONENT = 5;
	private static final int MONITOR_COUNT = 1 << MONITOR_EXPONENT;
	
	private static final int EVALUATION_SHIFT           = 0;
	private static final int HORIZON_SHIFT              = 31;
	private static final int TYPE_SHIFT                 = 47;
	private static final int COMPRESSED_BEST_MOVE_SHIFT = 49;

	private static final long EVALUATION_MASK           = 0x000000007FFFFFFFL;
	private static final long HORIZON_MASK              = 0x00007FFF80000000L;
	private static final long TYPE_MASK                 = 0x0001800000000000L;
	private static final long COMPRESSED_BEST_MOVE_MASK = 0xFFFE000000000000L;
	
	private static final int EVALUATION_OFFSET = Evaluation.MIN;
	
	public HashTableImpl(final int exponent) {
		monitorArray = new Object[MONITOR_COUNT];
		
		for (int i = 0; i < MONITOR_COUNT; i++)
			monitorArray[i] = new Object();
		
		resize(exponent);
	}
	
	public void resize (final int exponent) {
		final int effectiveExponent = Math.max(exponent, MONITOR_EXPONENT);
		
		if (this.exponent != effectiveExponent) {
			this.exponent = effectiveExponent;
			
			final int recordCount = 1 << effectiveExponent;
			
			table = new long[2 * recordCount];
			indexMask = ((long) recordCount - 1) << 1;
			monitorShift = effectiveExponent - MONITOR_EXPONENT + 1;
		}
	}
	
	private Object getMonitor (final int index) {
		return monitorArray[index >>> monitorShift];
	}
	
	public boolean getRecord (final Position position, final HashRecord record) {
		return getRecord(position.getHash(), record);
	}
	
	public boolean getRecord (final long hash, final HashRecord record) {
		final int index = (int) (hash & indexMask);
		final Object monitor = getMonitor (index);
		
		synchronized (monitor) {
			if (table[index] != hash) {
				record.setType(HashRecordType.INVALID);
				
				return false;
			}
				
			final long data = table[index + 1];
			
			final int evaluation = (int) ((data & EVALUATION_MASK) >>> EVALUATION_SHIFT) + EVALUATION_OFFSET;
						
			record.setEvaluation(evaluation);
			record.setHorizon((int) ((data & HORIZON_MASK) >>> HORIZON_SHIFT));
			record.setType((int) ((data & TYPE_MASK) >>> TYPE_SHIFT));
			record.setCompressedBestMove((int) ((data & COMPRESSED_BEST_MOVE_MASK) >>> COMPRESSED_BEST_MOVE_SHIFT));
			
			return true;
		}
	}
	
	public void updateRecord (final Position position, final HashRecord record) {
		updateRecord(position.getHash(), record);
	}
	
	public void updateRecord (final long hash, final HashRecord record) {
		final int index = (int) (hash & indexMask);
		final Object monitor = getMonitor (index);
		
		synchronized (monitor) {
			final int oldHorizon = (int) ((table[index + 1] & HORIZON_MASK) >> HORIZON_SHIFT);
			
			if (record.getHorizon() >= oldHorizon) {
				table[index] = hash;
				
				long data = 0;
				
				final int evaluation = Math.min(Math.max(record.getEvaluation(), Evaluation.MIN), Evaluation.MAX) - EVALUATION_OFFSET;
				
				data |= ((long) evaluation << EVALUATION_SHIFT) & EVALUATION_MASK;
				data |= ((long) record.getHorizon() << HORIZON_SHIFT) & HORIZON_MASK;
				data |= ((long) record.getType() << TYPE_SHIFT) & TYPE_MASK;
				data |= ((long) record.getCompressedBestMove() << COMPRESSED_BEST_MOVE_SHIFT) & COMPRESSED_BEST_MOVE_MASK;
				
				table[index + 1] = data;
			}
		}
	}
	
	/**
	 * Clears the table.
	 */
	public void clear() {
		for (int i = 0; i < MONITOR_COUNT; i++) {
			synchronized (monitorArray[i]) {
				Arrays.fill(table, i << monitorShift, (i + 1) << monitorShift, 0);
			}

		}
	}
}
