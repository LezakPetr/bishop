package bisGui.math;

import math.IVectorRead;

public interface ICoordinateTransformation {
	/**
	 * Transforms point by this transformation.
	 * @param point original point
	 * @return transformed point
	 */
	public IVectorRead transformPointForward (final IVectorRead point);
	
	/**
	 * Inverse transforms point by this transformation
	 * @param point transformed point
	 * @return original point
	 */
	public IVectorRead transformPointBackward (final IVectorRead point);
}
