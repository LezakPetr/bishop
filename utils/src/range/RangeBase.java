package range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for range encoder and decoder.
 * Range encoder encodes sequence of symbols as a number in some range.
 * Encoder starts with full range <0; MAX_RANGE_WIDTH) and shrinks this range
 * with each symbol.
 * When symbol is encoded current range is divided into subranges with width
 * proportional to probability of symbol occurrence. Current range is then
 * shrunk into subrange that corresponds to encoded symbol.
 * Number that is written into stream must lie inside current range. If highest
 * byte of the number is defined by the current range it is written into
 * the stream and range boundaries are shifted left by 8 bits.
 * This emulates infinite size width of initial range with finite numbers.
 * But it is also possible that range is narrow but more numbers with different
 * highest bytes are inside it. In case that range is narrower than
 * MIN_RANGE_WIDTH the range must be normalized. Subrange of the actual range is
 * selected so it contains only numbers with same highest byte. This byte can
 * be then written into the stream.
 * Decoder reads bytes of the number and decides which subranges was selected.
 * The sequence of subrange selection defines sequence of symbols.
 * Division of current range into subranges are defined by probability model.
 * Each symbol may be encoded with different probability model, but model
 * used for decode symbol must be same as model used for encode it. This means
 * that probability model may for instance depend on previous symbols. 
 * @author Ing. Petr Ležák
 */
public abstract class RangeBase {
	
	// Universal constant
	protected static final int BITS_IN_BYTE = 8;
	
	// Minimal width of range. Must be at least MAX_SYMBOL_VALUE.
	protected static final int MIN_RANGE_BYTES = 2;
	protected static final int MIN_RANGE_BITS = BITS_IN_BYTE * MIN_RANGE_BYTES;
	protected static final long MIN_RANGE_WIDTH = 1L << MIN_RANGE_BITS;
	
	// Maximal width of range. MAX_RANGE_WIDTH * MAX_SYMBOL_VALUE must fit into
	// long type due to multiplication in getSymbolLowerBound.
	protected static final int MAX_RANGE_BYTES = 5;
	protected static final int MAX_RANGE_BITS = BITS_IN_BYTE * MAX_RANGE_BYTES;
	protected static final long MAX_RANGE_WIDTH = 1L << MAX_RANGE_BITS;
	protected static final long MAX_RANGE_MASK = MAX_RANGE_WIDTH - 1;
	
	// Most significant byte that carries information.
	protected static final long HIGH_BYTE = 0xFFL << ((MAX_RANGE_BYTES - 1) * BITS_IN_BYTE);
	
	public static final int MAX_SYMBOL_BITS = MIN_RANGE_BITS;
	public static final int MAX_SYMBOL_CDF = 1 << MAX_SYMBOL_BITS;
	public static final int PROBABILITY_BYTES = MIN_RANGE_BYTES;
	
	public static final int MIN_SYMBOL_PROBABILITY = 1;
	
	protected long number;   // Actual number read from the stream
	protected long low;   // Lower bound of actual range (inclusive)
	protected long high;   // Upper bound of actual range (exclusive)
	
	// List of symbol consumers, usually for debugging 
	private final List<ISymbolConsumer> symbolConsumerList = new ArrayList<>(0);
	
	
	/**
	 * Writes byte into stream in encoder.
	 * Reads byte from stream in decoder.
	 * In both cases method shifts range boundaries left by BITS_IN_BYTE bits.
	 * Method expects that high byte can be shifted out.
	 * @throws IOException thrown in case of IO error
	 */
	protected abstract void addByte() throws IOException;
	
	/**
	 * Updates range by selection of subrange corresponding to given symbol.
	 * @param probabilityModel probability model
	 * @param symbol symbol
	 * @throws IOException
	 */
	protected void updateRange(final IProbabilityModel probabilityModel, final int symbol) throws IOException {
		final long range = high - low;
		final long origLow = low;
		
		low = getSymbolLowerBound(probabilityModel, origLow, range, symbol);
		high = getSymbolLowerBound(probabilityModel, origLow, range, symbol + 1);
		
		normalize();
	}
	
	/**
	 * Calculates lower bound of subrange corresponding to given symbol.
	 * @param probabilityModel probability model
	 * @param symbol symbol
	 * @return lower bound of subrange corresponding to given symbol
	 */
	protected static long getSymbolLowerBound(final IProbabilityModel probabilityModel, final long low, final long range, final int symbol) {
		return ((probabilityModel.getCdfLowerBound(symbol) * range) >> MAX_SYMBOL_BITS) + low;
	}
	
	/**
	 * Reads or writes as many bytes as possible from/to stream and updates range.  
	 * @throws IOException thrown in case of IO exception
	 */
	private void shiftOutBytes() throws IOException {
		while ((low & HIGH_BYTE) == ((high - 1) & HIGH_BYTE)) {
			addByte();
		}
	}
	
	/**
	 * Shifts out as many bytes as possible. If range is still narrower
	 * than MIN_RANGE_WIDTH it select subrange of current so it can shift out
	 * bytes and make range wider or equal to MIN_RANGE_WIDTH.
	 * 
	 * Range contains two different high bytes because no byte can be shifted out
	 * so there must be at least two and range width is lower than MIN_RANGE_WIDTH
	 * so there must be up to two high bytes. It also means that all bytes
	 * on the right of MIN_RANGE_BYTES except high byte must be 0 in upper boundary
	 * and 255 in lower boundary (if not, range will be wider than MIN_RANGE_WIDTH).
	 * Method calculates border by masking out all bits except high byte from upper
	 * boundary of range. Range contains border. Method can use the border as
	 * lower or upper boundary of subrange.
	 * 
	 * Before narrowing:
	 *         | <--------------------- MAX_RANGE_BYTES
	 *                      | <-------- MIN_RANGE_BYTES
	 *   high = 124   0   0 | 115  48
	 *   low  = 123 255 255 |  25  97
	 *   
	 * After narrowing:
	 *   high = 124   0   0 |   0   0
	 *   low  = 123 255 255 |  25  97
	 * Or:
	 *   high = 124   0   0 | 115  48
	 *   low  = 124   0   0 |   0   0
	 *   
	 * After narrowing subrange method can shift out all bytes one the right of
	 * MIN_RANGE_BYTES. 
	 * @throws IOException Thrown in case of IO error
	 */
	protected void normalize() throws IOException {
		shiftOutBytes();
		
		if (high - low < MIN_RANGE_WIDTH) {
			final long border = high & (HIGH_BYTE | MAX_RANGE_WIDTH);
			
			if (border - low > high - border)
				high = border;
			else
				low = border;
			
			shiftOutBytes();
		}
	}
	
	/**
	 * Shifts out high byte of the range and number.
	 * @return shifted out byte
	 */
	protected byte popHighByte() {
		final long highByte = low & HIGH_BYTE;
		
		low = (low - highByte) << BITS_IN_BYTE;
		high = (high - highByte) << BITS_IN_BYTE;
		number = (number - highByte) << BITS_IN_BYTE;

		return (byte) (highByte >>> ((MAX_RANGE_BYTES - 1) * BITS_IN_BYTE));
	}
	
	/**
	 * Returns lower boundary.
	 * @return lower boundary
	 */
	public long getLow() {
		return low;
	}

	/**
	 * Returns upper boundary.
	 * @return upper boundary
	 */
	public long getHigh() {
		return high;
	}
	
	public void addSymbolConumer (final ISymbolConsumer consumer) {
		symbolConsumerList.add(consumer);
	}
	
	public void removeSymbolConsumer (final ISymbolConsumer consumer) {
		final int index = symbolConsumerList.lastIndexOf(consumer);
		
		if (index < 0)
			throw new RuntimeException("Consumer not found");
		
		symbolConsumerList.remove(index);
	}
	
	public void removeAllSymbolConsumers() {
		symbolConsumerList.clear();
	}
	
	protected void sendSymbolToConsumers (final int symbol) {
		symbolConsumerList.forEach(c -> c.consumeSymbol(symbol));
	}

}
