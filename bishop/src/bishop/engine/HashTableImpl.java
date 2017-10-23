package bishop.engine;

import java.util.concurrent.atomic.AtomicLongArray;

import bishop.base.Position;
import utils.Mixer;

/**
 * Implementation of the hash table.
 * The hash table stores records in AtomicLongArray. The "serialized" record is xored with the
 * difused hash. The index of the item in array is also calculated from the hash by masking the lower
 * bits of the hash by indexMask. This is why we need to diffuse the hash before xoring to spread
 * non-masked bits to whole item except bits where the horizon is stored.
 * If more records belongs to the same item in array the item with
 * greater horizon wins because it is more important.
 * When the record is read we verify that it contains searched position by:
 * - verifying hash stored in REST_MASK - it contains 19 bits just with the diffused hash, without data
 * - verifying evaluation - we allow 105542 / 1048576 combination = 3.31 bits
 * - verifying type - we allow 3 / 4 combination = 0.42 bits
 * In total we verifies 22.73 bits which is not enough. The caller should try to uncompress the best move.
 * This would verify if the move is pseudolegal in the position so it will also indirectly verify the
 * hash. If we count up to 40 moves in the position this is additional 9.68 bits of verification.
 * So in total we would have 32.41 bits which is enough, the probability of obtaining an undetected
 * collision is one in 5.7 billion for one read. This means that if we are running a search for 3 minutes
 * with speed 10 million of nodes per second we can expect 0.31 undetected collisions at most
 * (it is actually less because the search engine is not querying the hash table for every node).
 * @author Ing. Petr Ležák
 */
public final class HashTableImpl implements IHashTable {
	
	private int exponent;
	private AtomicLongArray table;
	private long indexMask;
	
	private static final int HORIZON_SHIFT              = 0;
	private static final int EVALUATION_SHIFT           = 8;
	private static final int TYPE_SHIFT                 = 28;
	private static final int COMPRESSED_BEST_MOVE_SHIFT = 30;

	private static final long HORIZON_MASK              = 0x00000000000000FFL;
	private static final long EVALUATION_MASK           = 0x000000000FFFFF00L;
	private static final long TYPE_MASK                 = 0x0000000030000000L;
	private static final long COMPRESSED_BEST_MOVE_MASK = 0x00001FFFC0000000L;
	private static final long REST_MASK                 = 0xFFFFE00000000000L;
	
	private static final long HASH_MASK = ~HORIZON_MASK;
	private static final int EVALUATION_OFFSET = Evaluation.MIN;
	
	public static final int MIN_EXPONENT = 0;
	public static final int MAX_EXPONENT = 31;
	public static final int ITEM_SIZE = Long.BYTES;   // Size of hash item [B]
		
	// Probability that the collision will be detected by the hash table itself (without uncompressing move)
	public static double PRIMARY_COLLISION_RATE = Math.pow(2, -(19 + 3.31 + 0.42));
	
	public HashTableImpl(final int exponent) {
		resize(exponent);
	}
	
	public void resize (final int exponent) {
		if (exponent < MIN_EXPONENT || exponent > MAX_EXPONENT)
			throw new RuntimeException("Exponent out of range: " + exponent);
		
		if (this.exponent != exponent) {
			this.exponent = exponent;
			
			final int recordCount = 1 << exponent;
			
			table = new AtomicLongArray(recordCount);
			indexMask = ((long) recordCount - 1) & ~1L;
		}
	}
	
	public boolean getRecord (final Position position, final HashRecord record) {
		return getRecord(position.getHash(), record);
	}
	
	private static long diffuseHash(final long hash) {
		return Mixer.mixLong(hash) & HASH_MASK;
	}
	
	private boolean readRecordFromIndex (final int index, final long diffusedHash, final HashRecord record) {
		final long tableItem = table.get(index);
		final long data = tableItem ^ diffusedHash;
		
		if ((data & REST_MASK) != 0)
			return false;
		
		final int evaluation = (int) ((data & EVALUATION_MASK) >>> EVALUATION_SHIFT) + EVALUATION_OFFSET;
		final int integralHorizon = (int) ((data & HORIZON_MASK) >>> HORIZON_SHIFT);
		final int type = (int) ((data & TYPE_MASK) >>> TYPE_SHIFT);
		final int compressedBestMove = (int) ((data & COMPRESSED_BEST_MOVE_MASK) >>> COMPRESSED_BEST_MOVE_SHIFT);
		
		record.setEvaluation(evaluation);
		record.setHorizon(integralHorizon << ISearchEngine.HORIZON_FRACTION_BITS);
		record.setType(type);
		record.setCompressedBestMove(compressedBestMove);
		
		if (!record.canBeStored())
			return false;
		
		return true;
	}
	
	public boolean getRecord (final long hash, final HashRecord record) {
		final int index = (int) (hash & indexMask);
		final long diffusedHash = diffuseHash (hash);
		
		if (readRecordFromIndex(index, diffusedHash, record))
			return true;

		if (readRecordFromIndex(index + 1, diffusedHash, record))
			return true;

		record.setType(HashRecordType.INVALID);
		
		return false;
	}
	
	public void updateRecord (final Position position, final HashRecord record) {
		updateRecord(position.getHash(), record);
	}
	
	public void updateRecord (final long hash, final HashRecord record) {
		if (!record.canBeStored())
			return;
				
		final int index = (int) (hash & indexMask);
		final int integralHorizon = record.getHorizon() >> ISearchEngine.HORIZON_FRACTION_BITS;

		final long biasedEvaluation = record.getEvaluation() - EVALUATION_OFFSET;
		
		long data = 0;
		data |= ((long) biasedEvaluation << EVALUATION_SHIFT) & EVALUATION_MASK;
		data |= ((long) integralHorizon << HORIZON_SHIFT) & HORIZON_MASK;
		data |= ((long) record.getType() << TYPE_SHIFT) & TYPE_MASK;
		data |= ((long) record.getCompressedBestMove() << COMPRESSED_BEST_MOVE_SHIFT) & COMPRESSED_BEST_MOVE_MASK;
		
		final long diffusedHash = diffuseHash(hash);
		final long newTableItem = data ^ diffusedHash;

		while (true) {
			final long oldTableItem = table.get(index);
			final int oldIntegralHorizon = (int) ((oldTableItem & HORIZON_MASK) >> HORIZON_SHIFT);
			
			if (integralHorizon < oldIntegralHorizon)
				break;
			
			if (table.compareAndSet(index, oldTableItem, newTableItem))
				break;
		}
		
		table.set(index + 1, newTableItem);
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
