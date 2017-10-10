package utils;


public class Mixer {
	
	public static final long MIX_COEFF1 = 0xb2c9ae1182d84b7bL;
	public static final long MIX_COEFF2 = 0x5284803fe41c5383L;
	
	public static final long MIX_CONSTANT_XOR = 0xa210a71e1eb21f34L;
	
	/**
	 * Mixing long bits with following properties:
	 * - it is non-linear
	 * - it mixes the bits of the original value to all bits of the result so good hash code is generated
	 * - the mapping is bijective 
	 * The function is self-inverse with inverse coeffs.
	 * @param input input
	 * @param coeff1 first odd coeff
	 * @param coeff2 second odd coeff
	 * @param constantXor XOR constant
	 * @return mixed input
	 */
	public static long mixLongWithCoeffs (final long input, final long coeff1, final long coeff2, final long constantXor) {
		// Xor the upper half of the input to the lower half.
		// This is bijective mapping because we can undo it by same operation.
		final long a = input ^ (input >>> 32);
		
		// Multiply the number by random odd constant.
		// This spreads the bits to the up. The lower half of 'a' depended on all bits
		// of input and it is spreaded to all bits of the upper half of 'b'. So upper half of 'b'
		// contains mixed bits - every bit of 'b' depends on every bit of the input.
		// This step is also bijective. Suppose that there are two numbers a1 and a2 so
		// coeff * a1 = coeff * a2 (mod 2^64)
		// coeff * a1 - coeff * a2 = k * 2^64
		// coeff * (a1 - a2) = k * 2^64
		// Now consider the factors of both sides of the equation. The right side contains at lease
		// 64 factors 2 so the left side must contain it too to satisfy the equation. The expression
		// a1 - a2 can contain at most 63 factors 2 because 0 <= a1 < 2^64 and 0 <= a2 < 2^64.
		// So if the coeff does not contain factor 2 - if it is odd - then the equation
		// cannot be satisfied. This means that multiplication by odd coeff is bijective mapping.
		final long b = coeff1 * a;
		
		// Xor two halves of the 'y' - nonlinear step with respect to the multiplication.
		// The CONSTANT_XOR ensures that the 0 is not fixed point of the function.
		final long c = b ^ (b >>> 32) ^ constantXor;
		
		// Again multiplication by odd coeff
		final long d = coeff2 * c;
		final long e = d ^ (d >>> 32);

		return e;
	}
	
	public static long mixLong(final long input) {
		return mixLongWithCoeffs(input, MIX_COEFF1, MIX_COEFF2, MIX_CONSTANT_XOR);
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
