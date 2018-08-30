package regression;

import math.IVector;
import math.IVectorRead;
import math.Vectors;

import java.util.function.DoubleBinaryOperator;
import java.util.stream.IntStream;

/**
 * PolynomialTerm represents product of powers of vector components multiplied by coefficient.
 * The term is immutable.
 */
public class PolynomialTerm {
    private final double coeff;
    private final PolynomialTermKey key;

    /**
     * Creates term with given coeff and key.
     * @param coeff coefficient
     * @param key key (the powers)
     */
    public PolynomialTerm(final double coeff, final PolynomialTermKey key) {
        this.coeff = coeff;
        this.key = key;
    }

    /**
     * Returns dimension of the input vectors.
     * @return input dimension
     */
    public int getInputDimension() {
        return key.getInputDimension();
    }

    /**
     * Applies given field to given vector.
     */
    public double apply (final IVectorRead x) {
        if (x.getDimension() != key.getInputDimension())
            throw new RuntimeException("Wrong length of vector");

        return coeff * IntStream.range(0, key.getInputDimension())
                .mapToDouble(i -> Math.pow(x.getElement(i), key.getExponent(i)))
                .reduce(1.0, (a, b) -> a * b);
    }

    /**
     * Derives this scalar field against given index.
     * @param index index of vector element
     * @return partial derivation
     */
    public PolynomialTerm derive (final int index) {
        final int dimension = key.getInputDimension();
        final double oldPower = key.getExponent(index);

        if (oldPower > 0)
            return new PolynomialTerm(oldPower * coeff, key.decrementPower(index));
        else
            return new PolynomialTerm(0.0, PolynomialTermKey.one(dimension));
    }

    /**
     * Returns term with value this + that. Both terms must have same key.
     * @param that added term
     * @return this + that
     */
    public PolynomialTerm add (final PolynomialTerm that) {
        ensureEqualKeys(that);

        return new PolynomialTerm(this.coeff + that.coeff, this.key);
    }

    /**
     * Returns term with value this - that. Both terms must have same key.
     * @param that subtracted term
     * @return this - that
     */
    public PolynomialTerm subtract (final PolynomialTerm that) {
        ensureEqualKeys(that);

        return new PolynomialTerm(this.coeff - that.coeff, this.key);
    }

    /**
     * Returns term with value this * that.
     * @param that multiplied term
     * @return this * that
     */
    public PolynomialTerm multiply (final PolynomialTerm that) {
        return new PolynomialTerm(this.coeff * that.coeff, this.key.multiply (that.key));
    }

    private void ensureEqualKeys(PolynomialTerm that) {
        if (!this.key.equals(that.key))
            throw new RuntimeException("Polynomial terms have different powers");
    }

    public PolynomialTermKey getKey() {
        return key;
    }

    public boolean isZero() {
        return coeff == 0;
    }

    @Override
    public String toString() {
        return coeff + " * " + key.toString();
    }

    /**
     * Parses term.
     * @param dimension dimension of input vectors
     * @param str string to parse
     * @return term
     */
    public static PolynomialTerm parse (final int dimension, final String str) {
        double coeff = 1.0;
        final IVector exponents = Vectors.sparse(dimension);
        final String[] tokens = str.split("\\*");

        for (String token: tokens) {
            final String trimmedToken = token.trim();
            final char firstChar = trimmedToken.charAt(0);

            if (Character.isDigit(firstChar) || firstChar == '-') {
                coeff *= Double.parseDouble(trimmedToken);
            }
            else {
                final String[] subTokens = trimmedToken.split("\\^");
                final String variable = subTokens[0];
                final int powerIndex = Integer.parseInt(variable.substring(1));
                final double exponent;

                if (subTokens.length == 1) {
                    exponent = 1;
                }
                else {
                    if (subTokens.length == 2) {
                        exponent = Double.parseDouble(subTokens[1]);
                    }
                    else
                        throw new RuntimeException("Parse error - too many ^");
                }

                exponents.setElement(powerIndex, exponents.getElement(powerIndex) + exponent);
            }
        }

        return new PolynomialTerm(coeff, PolynomialTermKey.of(exponents.freeze()));
    }

    public static PolynomialTerm linear (final int inputIndex, final int dimension) {
        return new PolynomialTerm(
                1.0,
                PolynomialTermKey.of(
                        Vectors.getUnitVector(inputIndex, dimension)
                )
        );
    }

}
