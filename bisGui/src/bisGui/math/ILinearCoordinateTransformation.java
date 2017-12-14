package bisGui.math;

import math.IMatrixRead;
import math.IVectorRead;

public interface ILinearCoordinateTransformation extends ICoordinateTransformation {
	/**
	 * Returns origin of the inner coordinate system.
	 * @return origin of the inner coordinate system defined in outer coordinate system
	 */
	public IVectorRead getOrigin();
	
	/**
	 * Returns forward transformation matrix.
	 * @return forward transformation matrix
	 */
	public IMatrixRead getForwardMatrix();
	
	/**
	 * Returns backward transformation matrix.
	 * @return backward transformation matrix
	 */
	public IMatrixRead getBackwardMatrix();
}
