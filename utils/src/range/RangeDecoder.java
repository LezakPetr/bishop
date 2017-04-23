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
	volatile long x; 
	
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
		final int cdf = (int) (((number - low) << MAX_SYMBOL_BITS) / range);

		// Get symbol and try next ones until we have bigger CDF than number.
		int symbol = probabilityModel.getSymbolForCdf(cdf);
		
		long updatedLow;
		long updatedHigh = getSymbolLowerBound(probabilityModel, symbol);
		
		do {
			symbol++;
			
			updatedLow = updatedHigh;
			updatedHigh = getSymbolLowerBound(probabilityModel, symbol);
		} while (updatedHigh <= number);
		
		low = updatedLow;
		high = updatedHigh;
		
		normalize();
		
		final int decodedSymbol = symbol - 1;
		sendSymbolToConsumers(decodedSymbol);
		
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
