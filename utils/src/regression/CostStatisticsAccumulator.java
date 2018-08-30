package regression;

import math.*;

import java.util.function.Function;
import java.util.stream.Collector;

public class CostStatisticsAccumulator implements ICostStatistics {
	private double costSum;
	private double weightSum;
	private final IVector totalGradient;
	private final IMatrix totalHessian;

	public CostStatisticsAccumulator(final int inputDimension) {
		costSum = 0.0;
		weightSum = 0.0;
		totalGradient = Vectors.sparse(inputDimension);
		totalHessian = Matrices.createMutableMatrix(Density.SPARSE, inputDimension, inputDimension);
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

	public CostStatisticsAccumulator add (final ICostStatistics that) {
		this.costSum += that.getCostSum();
		this.weightSum += that.getWeightSum();

		final IVectorRead thatTotalGradient = that.getTotalGradient();

		if (thatTotalGradient != null)
			Vectors.addInPlace(this.totalGradient, thatTotalGradient);

		final IMatrixRead thatTotalHessian = that.getTotalHessian();

		if (thatTotalHessian != null)
			Matrices.addInPlace(this.totalHessian, thatTotalHessian);

		return this;
	}

	public static Collector<ICostStatistics, CostStatisticsAccumulator, ICostStatistics> collector(final int inputDimension) {
		return Collector.of(
				() -> new CostStatisticsAccumulator(inputDimension),
				CostStatisticsAccumulator::add,
				CostStatisticsAccumulator::add,
				a -> a
		);
	}

}
