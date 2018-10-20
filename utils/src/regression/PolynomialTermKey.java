package regression;

import math.IVectorRead;
import math.Vectors;
import utils.ArrayUtils;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * PolynomialTermKey represents product of exponents of vector components.
 * For instance, if exponents = {5, 2, 0, 4} the key represents polynomial
 * x0^5 * x1^2 * x3^4. The exponents must be non-negative.
 */
public class PolynomialTermKey implements Comparable<PolynomialTermKey> {
    private final IVectorRead exponents;

    private PolynomialTermKey (final IVectorRead exponents) {
        this.exponents = exponents.immutableCopy();
    }

    /**
     * Returns dimension of the input vectors.
     * @return dimension
     */
    public int getInputDimension() {
        return exponents.getDimension();
    }

    public double getExponent(final int index) {
        return exponents.getElement(index);
    }

    public IVectorRead getExponents() {
        return exponents;
    }

    /**
     * Returns key this * that
     * @param that multiplied key
     * @return this * that
     */
    public PolynomialTermKey multiply(final PolynomialTermKey that) {
        final IVectorRead resultExponents = this.exponents.plus(that.exponents);

        return new PolynomialTermKey(resultExponents);
    }

    /**
     * Returns key with decremented power with given index. Only exponents with exponent
     * @param index index of decremented power
     * @return key with decremented power
     */
    public PolynomialTermKey decrementPower (final int index) {
        final IVectorRead newExponents = exponents.minus(Vectors.getUnitVector (index, exponents.getDimension()));

        return new PolynomialTermKey(newExponents);
    }

    @Override
    public boolean equals (final Object obj) {
        if (obj == null || this.getClass() != obj.getClass())
            return false;

        final PolynomialTermKey that = (PolynomialTermKey) obj;

        return this.exponents.equals(that.exponents);
    }

    @Override
    public int hashCode() {
        return exponents.hashCode();
    }

    @Override
    public int compareTo(final PolynomialTermKey that) {
        return this.exponents.compareTo(that.exponents);
    }

    private String powerToString (final int index) {
        final double power = exponents.getElement(index);

        if (power == 0)
            return "";

        final String variable = "x" + index;

        if (power == 1)
            return variable;
        else
            return variable + "^" + power;
    }

    @Override
    public String toString() {
        return IntStream.range(0, exponents.getDimension())
                .mapToObj(this::powerToString)
                .collect(Collectors.joining(" * "));
    }

    /**
     * Returns key with given exponents.
     * @param exponents array with exponents. The array is copied.
     * @return key
     */
    public static PolynomialTermKey of (final IVectorRead exponents) {
        return new PolynomialTermKey(exponents);
    }

    /**
     * Returns key representing constant 1.
     * @param dimension input dimension
     * @return key representing constant 1.
     */
    public static PolynomialTermKey one (final int dimension) {
        return new PolynomialTermKey(Vectors.getZeroVector(dimension));
    }
}
