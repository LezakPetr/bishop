package regression;

import math.IVectorRead;
import math.Vectors;

import java.util.stream.Collector;

public class CostStatistics {
    private double costSum;
    private double weightSum;
    private IVectorRead totalGradient;

    public CostStatistics(final int inputDimension) {
        costSum = 0.0;
        weightSum = 0.0;
        totalGradient = Vectors.getZeroVector(inputDimension);
    }

    public CostStatistics(final double costSum, final double weightSum, final IVectorRead totalGradient) {
        this.costSum = costSum;
        this.weightSum = weightSum;
        this.totalGradient = totalGradient;
    }

    public double getCostSum() {
        return costSum;
    }

    public double getWeightSum() {
        return weightSum;
    }

    public IVectorRead getTotalGradient() {
        return totalGradient;
    }

    public CostStatistics add (final CostStatistics that) {
        this.costSum += that.costSum;
        this.weightSum += that.weightSum;
        this.totalGradient = Vectors.plus(this.totalGradient, that.totalGradient);

        return this;
    }

    public static Collector<CostStatistics, CostStatistics, CostStatistics> collector(final int inputDimension) {
        return Collector.of(
                () -> new CostStatistics(inputDimension),
                CostStatistics::add,
                CostStatistics::add
        );
    }

}
