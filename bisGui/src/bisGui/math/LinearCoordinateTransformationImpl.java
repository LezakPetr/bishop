package bisGui.math;

import math.*;

public class LinearCoordinateTransformationImpl implements ILinearCoordinateTransformation {
	
	private final IVectorRead origin;
	private final IMatrixRead forwardMatrix;
	private final IMatrixRead backwardMatrix;
	
	
	/**
	 * Creates linear coordinate transformation with given origin and forward matrix.
	 * Backward matrix is calculated by inversion of forward matrix.
	 * @param origin origin of the inner coordinate system
	 * @param forwardMatrix forward transformation matrix
	 */
	public LinearCoordinateTransformationImpl (final IVectorRead origin, final IMatrixRead forwardMatrix) {
		this.origin = origin;
		this.forwardMatrix = forwardMatrix;
		this.backwardMatrix = GaussianElimination.matrixInversion(forwardMatrix).solve();
	}
	
	/**
	 * Creates linear coordinate transformation with given origin, forward and backward matrix.
	 * @param origin origin of the inner coordinate system
	 * @param forwardMatrix forward transformation matrix
	 * @param backwardMatrix backward transformation matrix
	 */
	public LinearCoordinateTransformationImpl (final IVectorRead origin, final IMatrixRead forwardMatrix, final IMatrixRead backwardMatrix) {
		this.origin = origin;
		this.forwardMatrix = forwardMatrix;
		this.backwardMatrix = backwardMatrix;
	}
	
	/**
	 * Transforms point by this transformation.
	 * @param point original point
	 * @return transformed point
	 */
	public IVectorRead transformPointForward (final IVectorRead point) {
		return Vectors.plus(origin, Matrices.multiply(point, forwardMatrix));
	}
	
	/**
	 * Inverse transforms point by this transformation
	 * @param point transformed point
	 * @return original point
	 */
	public IVectorRead transformPointBackward (final IVectorRead point) {
		return Matrices.multiply (Vectors.minus(point, origin), backwardMatrix);
	}

	/**
	 * Returns origin of the inner coordinate system.
	 * @return origin of the inner coordinate system defined in outer coordinate system
	 */
	public IVectorRead getOrigin() {
		return origin;
	}
	
	/**
	 * Returns forward transformation matrix.
	 * @return forward transformation matrix
	 */
	public IMatrixRead getForwardMatrix() {
		return forwardMatrix;
	}
	
	/**
	 * Returns backward transformation matrix.
	 * @return backward transformation matrix
	 */
	public IMatrixRead getBackwardMatrix() {
		return backwardMatrix;
	}
	
}
