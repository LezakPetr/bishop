package regression;

import math.IVectorRead;
import math.Vectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiSampleCostField implements IScalarField {
    private final List<ISample> sampleList = new ArrayList<>();
    private final ISampleCostField sampleCostField;
    private final Regularization regularization;
    private final AtomicInteger batchBegin = new AtomicInteger();
    private int batchSize = Integer.MAX_VALUE;



    public MultiSampleCostField(final ISampleCostField sampleCostField) {
        this.sampleCostField = sampleCostField;
        this.regularization = new Regularization(getInputDimension());
    }

    public void addSamples (final Collection<?extends ISample> samples) {
        sampleList.addAll(samples);
    }

    /**
     * Returns dimension of the input vectors.
     * @return input dimension
     */
    public int getInputDimension() {
        return sampleCostField.getInputDimension();
    }

    private CostStatistics calculateStatisticsForSample (final IVectorRead x, final ISample sample) {
        final ScalarWithGradient sampleCostWithGradient = sampleCostField.calculateValueAndGradient(x, sample);

        final double sampleCost = sampleCostWithGradient.getScalar();
        final double weight = sample.getWeight();

        return new CostStatistics(
                sampleCost * weight,
                weight,
                sampleCostWithGradient.getGradient()
        );
    }

    /**
     * Returns value and gradient at given point.
     */
    @Override
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final Void parameters) {
        final int inputDimension = sampleCostField.getInputDimension();
        final List<ISample> batch = getSampleBatch();

        final CostStatistics statistics = batch.parallelStream()
                .map(s -> calculateStatisticsForSample(x, s))
                .collect(CostStatistics.collector(inputDimension));

        final ScalarWithGradient regularizationTerm = regularization.calculateValueAndGradient(x, (long) sampleList.size());

        final double weightSum = statistics.getWeightSum();
        final double cost = statistics.getCostSum() / weightSum + regularizationTerm.getScalar();
        final IVectorRead gradient = Vectors.plus(Vectors.multiply(1.0 / weightSum, statistics.getTotalGradient()), regularizationTerm.getGradient());

        return new ScalarWithGradient(cost, gradient);
    }

    /**
     * Returns batch to be processed. This method will sequentially cycle through the whole sample list.
     * @return batch - nonempty sublist with samples
     */
    private List<ISample> getSampleBatch() {
        final int totalSampleCount = sampleList.size();
        final int realBatchSize = Math.min(batchSize, totalSampleCount);

        int originalBatchBegin;
        int currentBatchBegin;
        int currentBatchEnd;

        do {
            originalBatchBegin = batchBegin.get();
            currentBatchBegin = (originalBatchBegin < totalSampleCount) ? originalBatchBegin : 0;
            currentBatchEnd = currentBatchBegin + Math.min (totalSampleCount - currentBatchBegin, realBatchSize);
        } while (!batchBegin.compareAndSet(originalBatchBegin, currentBatchEnd));

        return sampleList.subList(currentBatchBegin, currentBatchEnd);
    }

    public double getLambda() {
        return regularization.getLambda();
    }

    public void setLambda(final double lambda) {
        regularization.setLambda(lambda);
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public int getSampleCount() {
        return sampleList.size();
    }

}
