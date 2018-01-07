package range;

import java.io.IOException;
import java.io.InputStream;

/**
 * Range decoder, see commentary in class RangeBase for explanation
 * of the algorithm.
 * @author Ing. Petr Ležák
 */
public final class RangeDecoder extends RangeBase {
	
	private InputStream stream; 
	
	/**
	 * Initializes the decoder - sets given stream as input stream
	 * and select full range.
	 * @param stream input stream
	 * @throws IOException
	 */
	public void initialize (final InputStream stream) throws IOException {
		this.stream = stream;
		
		low = 0;
		high = 1;
		number = 0;
		
		for (int i = 0; i < MAX_RANGE_BYTES; i++)
			addByte();
	}

	protected void addByte() throws IOException {
		popHighByte();
		
		final int b = stream.read();
		
		if (b >= 0)
			number += b;
		
		assert (number >= low && number < high) : "Number is out of range";
	}
	
	/**
	 * Decodes symbol encoded with given probability model.
	 * @param probabilityModel probability model used to encode symbol
	 * @return decoded symbol
	 * @throws IOException
	 */
	public int decodeSymbol (final IProbabilityModel probabilityModel) throws IOException {
		final long range = high - low;
		final long origLow = low;
		
		// Original code that operated with longs was:
		//   final int cdf = (int) (((number - low) << MAX_SYMBOL_BITS) / range);
		// but double division is much faster:
		final double cdfNumerator = (number - low) << MAX_SYMBOL_BITS;   // Precise (number - low) < 2^40 < 2^52 and then multiplied by 2^16
		final double cdfDenominator = range;   // Precise because range < 2^40 < 2^52
		
		// The cdf is guaranteed to be lower bound. To obtain greater cdf than correct one the division must be at least
		// 1 ulp close to the next integer which means that it must be closer that 2^(16 - 52) = 2^(-36). But the difference
		// between division results is at least 2^MAX_SYMBOL_BITS / MAX_RANGE_WIDTH = 2^16 / 2^40 = 2^(-24).
		// We still have 36 - 24 = 12 spare bits in double precision. This also guarantees that 0 <= cdf < MAX_SYMBOL_CDF.
		final int cdf = (int) (cdfNumerator / cdfDenominator);

		// Get symbol and try next ones until we have bigger CDF than number.
		int symbol = probabilityModel.getSymbolForCdf(cdf);
		
		long updatedLow;
		long updatedHigh = getSymbolLowerBound(probabilityModel, origLow, range, symbol);
		
		assert (updatedHigh <= number);
		
		do {
			symbol++;
			
			updatedLow = updatedHigh;
			updatedHigh = getSymbolLowerBound(probabilityModel, origLow, range, symbol);
		} while (updatedHigh <= number);
		
		low = updatedLow;
		high = updatedHigh;
		
		normalize();
		
		final int decodedSymbol = symbol - 1;
		
		return decodedSymbol;
	}
	
	/**
	 * Closes the decoder.
	 * Does NOT close underlying stream.
	 * @throws IOException
	 */
	public void close() throws IOException {
		stream = null;
	}

}
