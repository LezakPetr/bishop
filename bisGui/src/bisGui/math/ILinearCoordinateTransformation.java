package bisGui.math;

public interface ILinearCoordinateTransformation extends ICoordinateTransformation {
	/**
	 * Returns origin of the inner coordinate system.
	 * @return origin of the inner coordinate system defined in outer coordinate system
	 */
	public IVector getOrigin();
	
	/**
	 * Returns forward transformation matrix.
	 * @return forward transformation matrix
	 */
	public IMatrix getForwardMatrix();
	
	/**
	 * Returns backward transformation matrix.
	 * @return backward transformation matrix
	 */
	public IMatrix getBackwardMatrix();
}
