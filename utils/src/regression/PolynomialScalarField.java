package regression;

import collections.ImmutableList;
import math.IVector;
import math.IVectorRead;
import math.Vectors;
import utils.SynchronizedLazy;

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
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final Void parameters) {
        final double value = this.apply(x);
        final IVector gradient = Vectors.dense(inputDimension);
        final ImmutableList<PolynomialScalarField> derivations = this.derivations.get();

        for (int i = 0; i < inputDimension; i++)
            gradient.setElement(i, derivations.get(i).apply(x));

        return new ScalarWithGradient(
            value,
            gradient.freeze()
        );
    }


}
