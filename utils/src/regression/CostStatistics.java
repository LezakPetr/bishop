package regression;

import math.IMatrixRead;
import math.IVectorRead;


public class CostStatistics implements ICostStatistics {
    private final double costSum;
    private final double weightSum;
    private final IVectorRead totalGradient;
    private final IMatrixRead totalHessian;

    public CostStatistics(final double costSum, final double weightSum, final IVectorRead totalGradient, final IMatrixRead totalHessian) {
        this.costSum = costSum;
        this.weightSum = weightSum;
        this.totalGradient = totalGradient;
        this.totalHessian = totalHessian;
    }

    @Override
    public double getCostSum() {
        return costSum;
    }

	@Override
    public double getWeightSum() {
        return weightSum;
    }

	@Override
    public IVectorRead getTotalGradient() {
        return totalGradient;
    }

	@Override
    public IMatrixRead getTotalHessian() {
        return totalHessian;
    }

}
