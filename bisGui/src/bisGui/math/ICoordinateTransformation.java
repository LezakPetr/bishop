package bisGui.math;

public interface ICoordinateTransformation {
	/**
	 * Transforms point by this transformation.
	 * @param point original point
	 * @return transformed point
	 */
	public IVector transformPointForward (final IVector point);
	
	/**
	 * Inverse transforms point by this transformation
	 * @param point transformed point
	 * @return original point
	 */
	public IVector transformPointBackward (final IVector point);
}
