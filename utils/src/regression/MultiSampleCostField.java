package regression;

import collections.ImmutableEnumSet;
import math.IMatrixRead;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiSampleCostField implements IScalarField {
    private final List<ISample> samples = new ArrayList<>();
    private final ISampleCostField sampleCostField;
    private final Regularization regularization;


    public MultiSampleCostField(final ISampleCostField sampleCostField) {
        this.sampleCostField = sampleCostField;
        this.regularization = new Regularization(getInputDimension());
    }

    public void addSamples (final Collection<?extends ISample> samples) {
        this.samples.addAll(samples);
    }

    /**
     * Returns dimension of the input vectors.
     * @return input dimension
     */
    public int getInputDimension() {
        return sampleCostField.getInputDimension();
    }

    private CostStatistics calculateStatisticsForSample (final IVectorRead x, final ISample sample, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        final ScalarPointCharacteristics costCharacteristics = sampleCostField.calculate(x, sample, characteristics);

        final double sampleCost = costCharacteristics.getValue();
        final double weight = sample.getWeight();

        return new CostStatistics(
                sampleCost * weight,
                weight,
                costCharacteristics.getGradient(),
                costCharacteristics.getHessian()
        );
    }

    /**
     * Returns value and gradient at given point.
     */
    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final Void parameters, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        final int inputDimension = sampleCostField.getInputDimension();

        final ICostStatistics statistics = samples.parallelStream()
                .map(s -> calculateStatisticsForSample(x, s, characteristics))
                .collect(CostStatisticsAccumulator.collector(inputDimension));

        final ScalarPointCharacteristics regularizationTerm = regularization.calculate(x, (long) samples.size(), characteristics);

        final double weightSum = statistics.getWeightSum();
        final double cost = statistics.getCostSum() / weightSum + regularizationTerm.getValue();

        final IVectorRead gradient;

        if (characteristics.contains(ScalarFieldCharacteristic.GRADIENT))
            gradient = Vectors.plus(Vectors.multiply(1.0 / weightSum, statistics.getTotalGradient()), regularizationTerm.getGradient());
        else
            gradient = null;

        final IMatrixRead hessian;

        if (characteristics.contains(ScalarFieldCharacteristic.HESSIAN))
            hessian = Matrices.plus(Matrices.multiply(1.0 / weightSum, statistics.getTotalHessian()), regularizationTerm.getHessian());
        else
            hessian = null;

        return new ScalarPointCharacteristics(cost, gradient, hessian);
    }

    public double getLambda() {
        return regularization.getLambda();
    }

    public void setLambda(final double lambda) {
        regularization.setLambda(lambda);
    }

    public int getSampleCount() {
        return samples.size();
    }

}
