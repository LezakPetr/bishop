package utils;

public class Mixer {
	
	private static final long LONG_TO_INT_COEFF = 0xb2c9ae1182d84b7bL;
	
	/**
	 * Mixing long bits with following properties:
	 * - it is non-linear
	 * - it mixes the bits of the original value to all bits of the result so good hash code is generated
	 * - the mapping is bijective 

	 * @param input input
	 * @return mixed input
	 */
	public static long mixLong (final long input) {
		// Xor the upper half of the input to the lower half.
		// This is bijective mapping because we can undo it by same operation.
		final long x = input ^ (input >>> 32);
		
		// Multiply the number by random odd constant.
		// This spreads the bits to the up. The lower half of 'x' depended on all bits
		// of input and it is spreaded to all bits of the upper half of 'y'. So upper half of 'y'
		// contains mixed bits - every bit of 'y' depends on every bit of the input.
		// This step is also bijective. Suppose that there are two numbers x1 and x2 so
		// LONG_TO_INT_COEFF * x1 = LONG_TO_INT_COEFF * x2 (mod 2^64)
		// LONG_TO_INT_COEFF * x1 - LONG_TO_INT_COEFF * x2 = k * 2^64
		// LONG_TO_INT_COEFF * (x1 - x2) = k * 2^64
		// Now consider the factors of both sides of the equation. The right side contains at lease
		// 64 factors 2 so the left side must contain it too to satisfy the equation. The expression
		// x1 - x2 can contain at most 63 factors 2 because 0 <= x1 < 2^64 and 0 <= x2 < 2^64.
		// So if the LONG_TO_INT_COEFF does not contain factor 2 - if it is odd - then the equation
		// cannot be satisfied. This means that multiplication by odd LONG_TO_INT_COEFF is bijective mapping.
		final long y = LONG_TO_INT_COEFF * x;
		
		// Xor two halves of the 'y' - nonlinear step with respect to the multiplication.
		final long z =  y ^ (y >>> 32);
		
		return z;
	}
	
	/**
	 * Mapping long to int value with following properties:
	 * - it is non-linear
	 * - it mixes the bits of the original value to all bits of the result so good hash code is generated
	 * - for random input with uniform probability distribution we gets random output with uniform probability distribution 
	 * @param value input
	 * @return outout
	 */
	public static int mixLongToInt (final long input) {
		return (int) mixLong(input);
	}
}
