package range;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Range encoder, see commentary in class RangeBase for explanation
 * of the algorithm.
 * @author Ing. Petr Ležák
 */
public class RangeEncoder extends RangeBase {
	
	private OutputStream stream;

	/**
	 * Initializes the encoder - sets given stream as output stream
	 * and select full range.
	 * @param stream output stream
	 * @throws IOException
	 */
	public void initialize (final OutputStream stream) throws IOException {
		this.stream = stream;
		
		low = 0;
		high = MAX_RANGE_WIDTH;
	}
	
	/**
	 * Encodes given symbol.
	 * @param probabilityModel probability model used to encode symbol
	 * @param symbol symbol to encode
	 * @throws IOException
	 */
	public void encodeSymbol (final IProbabilityModel probabilityModel, final int symbol) throws IOException {
		updateRange(probabilityModel, symbol);
	}
	
	/**
	 * Flushes remaining bytes to output stream.
	 * Does NOT close the stream.
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (stream != null) {
			emitFinalBytes();
			
			stream = null;
		}
	}
	
	private void emitFinalBytes() throws IOException {
		final long requestedNumber = high - 1;
		long storedNumber = 0;
		
		for (int i = MAX_RANGE_BYTES-1; i >= 0; i--) {
			if (storedNumber >= low)
				break;
			
			final int shift = BITS_IN_BYTE * i;
			final long digit = (requestedNumber >>> shift) & 0xFF;
			storedNumber |= digit << shift;
			
			stream.write((byte) digit);
		}
	}

	/**
	 * Reads or writes as many bytes as possible from/to stream and updates range.
	 * @throws IOException thrown in case of IO exception
	 */
	protected void shiftOutBytes() throws IOException {
		while ((low & HIGH_BYTE) == ((high - 1) & HIGH_BYTE)) {
			final long highByte = low & HIGH_BYTE;

			low = (low - highByte) << BITS_IN_BYTE;
			high = (high - highByte) << BITS_IN_BYTE;
			number = (number - highByte) << BITS_IN_BYTE;

			final int b = (byte) (highByte >>> ((MAX_RANGE_BYTES - 1) * BITS_IN_BYTE));

			stream.write(b);
		}
	}

}
