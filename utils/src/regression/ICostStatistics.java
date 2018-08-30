package regression;

import math.IMatrixRead;
import math.IVectorRead;

public interface ICostStatistics {
	public double getCostSum();

	public double getWeightSum();

	public IVectorRead getTotalGradient();

	public IMatrixRead getTotalHessian();
}
