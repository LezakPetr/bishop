package bishop.engine;

import bishop.base.LegalMoveGenerator;
import bishop.base.MoveList;
import range.IProbabilityModel;
import range.RangeBase;
import utils.IoUtils;

public class BookIo {
	protected static final int MOVE_SIZE = 2;
	protected static final int GOOD_MOVE_MASK = 0x8000;
	
	protected static final byte[] HEADER_MAGIC = { (byte) 'B', (byte) 'O', (byte) 'O', (byte) 'K' };
	
	protected static final int SYMBOL_FALSE = 0;
	protected static final int SYMBOL_TRUE = 1;
	
	protected static final int BOOLEAN_SYMBOL_COUNT = 2;
	protected static final int RELATIVE_MOVE_REPETITION_SYMBOL_COUNT = 101;
	protected static final int BALANCE_SYMBOL_COUNT = 201;
	protected static final int BALANCE_OFFSET = 100;
	protected static final int POSITION_REPETITION_COUNT_SYMBOL_COUNT = BookRecord.MAX_REPETITION_COUNT + 1;
	
	protected static final int HEADER_SIZE = 7 + IoUtils.INT_BYTES + (
				BOOLEAN_SYMBOL_COUNT - 1 + 
				BOOLEAN_SYMBOL_COUNT - 1 +
				RELATIVE_MOVE_REPETITION_SYMBOL_COUNT - 1 +
				BALANCE_SYMBOL_COUNT - 1 +
				POSITION_REPETITION_COUNT_SYMBOL_COUNT - 1
			) * RangeBase.PROBABILITY_BYTES;

	protected IProbabilityModel moveIncludedProbabilityModel;
	protected IProbabilityModel recordListContinueProbabilityModel;
	protected IProbabilityModel relativaMoveRepetitionProbabilityModel;
	protected IProbabilityModel balanceProbabilityModel;
	protected IProbabilityModel positionRepetitionCountProbabilityModel;
	
	protected int hashBits;
	protected long hashMask;
	
	protected final LegalMoveGenerator moveGenerator = new LegalMoveGenerator();
	protected final MoveList moveList = new MoveList();

	
	protected void setHashBits (final int bits) {
		this.hashBits = bits;
		this.hashMask = (1L << hashBits) - 1;
	}
	
}
