package regression;

import math.IMatrixRead;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

import java.util.stream.Collector;

public class CostStatistics {
    private double costSum;
    private double weightSum;
    private IVectorRead totalGradient;
    private IMatrixRead totalHessian;

    public CostStatistics(final int inputDimension) {
        costSum = 0.0;
        weightSum = 0.0;
        totalGradient = Vectors.getZeroVector(inputDimension);
        totalHessian = Matrices.getZeroMatrix(inputDimension, inputDimension);
    }

    public CostStatistics(final double costSum, final double weightSum, final IVectorRead totalGradient, final IMatrixRead totalHessian) {
        this.costSum = costSum;
        this.weightSum = weightSum;
        this.totalGradient = totalGradient;
        this.totalHessian = totalHessian;
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

    public IMatrixRead getTotalHessian() {
        return totalHessian;
    }

    public CostStatistics add (final CostStatistics that) {
        this.costSum += that.costSum;
        this.weightSum += that.weightSum;

        if (this.totalGradient != null && that.totalGradient != null)
            this.totalGradient = Vectors.plus(this.totalGradient, that.totalGradient);
        else
            this.totalGradient = null;

        if (this.totalHessian != null && that.totalHessian != null)
            this.totalHessian = Matrices.plus(this.totalHessian, that.totalHessian);
        else
            this.totalHessian = null;

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
