package regression;

import collections.ImmutableEnumSet;
import collections.ImmutableList;
import math.*;
import utils.SynchronizedLazy;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Scalar field formed by polynomial of more variables.
 * The field is immutable.
 */
public class PolynomialScalarField implements IScalarField {

    private final int inputDimension;
    private final SortedMap<PolynomialTermKey, PolynomialTerm> termMap;
    private final Supplier<ImmutableList<PolynomialScalarField>> derivations;

    private PolynomialScalarField (final int inputDimension, final SortedMap<PolynomialTermKey, PolynomialTerm> termMap) {
        this.inputDimension = inputDimension;
        this.termMap = termMap;

        for (PolynomialTerm term: termMap.values()) {
            if (term.getInputDimension() != inputDimension)
                throw new RuntimeException("Wrong dimension of term");
        }

        derivations = SynchronizedLazy.of(this::calculateAllDerivations);
    }

    private ImmutableList<PolynomialScalarField> calculateAllDerivations() {
        return IntStream.range(0, inputDimension)
                .mapToObj(this::derive)
                .collect(ImmutableList.collector());
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    public double apply (final IVectorRead x) {
        return termMap.values().stream()
                .mapToDouble(term -> term.apply(x))
                .sum();
    }

    private PolynomialScalarField derive (final int index) {
        final SortedMap<PolynomialTermKey, PolynomialTerm> newTermMap = new TreeMap<>();

        for (PolynomialTerm term: termMap.values()) {
            final PolynomialTerm derivedTerm = term.derive(index);

            if (!derivedTerm.isZero())
                newTermMap.put(derivedTerm.getKey(), derivedTerm);
        }

        return new PolynomialScalarField(inputDimension, newTermMap);
    }

    /**
     * Creates field from given collection of terms.
     * @param inputDimension input dimension
     * @param terms array of terms
     * @return field
     */
    public static PolynomialScalarField of (final int inputDimension, final PolynomialTerm ...terms) {
        return of (inputDimension, Arrays.asList(terms));
    }

    /**
     * Creates field from given collection of terms.
     * @param inputDimension input dimension
     * @param terms collection of terms
     * @return field
     */
    public static PolynomialScalarField of (final int inputDimension, final Collection<PolynomialTerm> terms) {
        final SortedMap<PolynomialTermKey, PolynomialTerm> termMap = new TreeMap<>();

        for (PolynomialTerm term: terms)
            termMap.merge(term.getKey(), term, PolynomialTerm::add);

        return new PolynomialScalarField(inputDimension, termMap);
    }

    /**
     * Parses given string.
     * @param inputDimension input dimension
     * @param str string to parse
     * @return field
     */
    public static PolynomialScalarField parse (final int inputDimension, final String str) {
        final String[] tokens = str.split("\\+");
        final SortedMap<PolynomialTermKey, PolynomialTerm> termMap = new TreeMap<>();

        for (String token: tokens) {
            final PolynomialTerm term = PolynomialTerm.parse(inputDimension, token);
            termMap.merge(term.getKey(), term, PolynomialTerm::add);
        }

        return new PolynomialScalarField(inputDimension, termMap);
    }

    public static PolynomialScalarField getUnitField (final int inputIndex, final int inputDimension) {
        return PolynomialScalarField.of(
                inputDimension,
                ImmutableList.of(
                        PolynomialTerm.linear(inputIndex, inputDimension)
                )
        );
    }

    /**
     * Returns value and gradient at given point.
     */
    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final Void parameters, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        // Value
        final double value = (characteristics.contains(ScalarFieldCharacteristic.VALUE)) ? this.apply(x) : Double.NaN;

        // Gradient
        final IVector gradient;

        if (characteristics.contains(ScalarFieldCharacteristic.GRADIENT)) {
            final ImmutableList<PolynomialScalarField> derivations = this.derivations.get();
            gradient = Vectors.dense(inputDimension);

            for (int i = 0; i < inputDimension; i++)
                gradient.setElement(i, derivations.get(i).apply(x));
        }
        else
            gradient = null;

        // Hessian
        final IMatrix hessian;

        if (characteristics.contains(ScalarFieldCharacteristic.HESSIAN)) {
            final ImmutableList<PolynomialScalarField> derivations = this.derivations.get();
            hessian = Matrices.createMutableMatrix(Density.DENSE, inputDimension, inputDimension);

            for (int row = 0; row < inputDimension; row++) {
                final ImmutableList<PolynomialScalarField> rowDerivations = derivations.get(row).derivations.get();

                for (int column = 0; column < inputDimension; column++)
                    hessian.setElement(row, column, rowDerivations.get(column).apply(x));
            }
        }
        else
            hessian = null;

        return new ScalarPointCharacteristics(
                () -> value,
                () -> gradient.freeze(),
                () -> hessian.freeze(),
                characteristics
        );
    }

    public PolynomialScalarField add (final PolynomialScalarField that) {
        final SortedMap<PolynomialTermKey, PolynomialTerm> termMap = new TreeMap<>();

        for (PolynomialTerm term: this.termMap.values())
            termMap.merge(term.getKey(), term, PolynomialTerm::add);

        for (PolynomialTerm term: that.termMap.values())
            termMap.merge(term.getKey(), term, PolynomialTerm::add);

        return new PolynomialScalarField(inputDimension, termMap);
    }

    public PolynomialScalarField multiply (final PolynomialScalarField that) {
        final SortedMap<PolynomialTermKey, PolynomialTerm> termMap = new TreeMap<>();

        for (PolynomialTerm thisTerm: this.termMap.values()) {
            for (PolynomialTerm thatTerm: that.termMap.values()) {
                final PolynomialTerm product = thisTerm.multiply(thatTerm);
                termMap.merge(product.getKey(), product, PolynomialTerm::add);
            }
        }

        return new PolynomialScalarField(inputDimension, termMap);
    }

}
