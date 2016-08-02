package bisGui.math;

import math.IMatrix;
import math.IVector;
import math.Matrices;
import math.Vectors;

public class LinearCoordinateTransformationImpl implements ILinearCoordinateTransformation {
	
	private final IVector origin;
	private final IMatrix forwardMatrix;
	private final IMatrix backwardMatrix;
	
	
	/**
	 * Creates linear coordinate transformation with given origin and forward matrix.
	 * Backward matrix is calculated by inversion of forward matrix.
	 * @param origin origin of the inner coordinate system
	 * @param forwardMatrix forward transformation matrix
	 */
	public LinearCoordinateTransformationImpl (final IVector origin, final IMatrix forwardMatrix) {
		this.origin = origin;
		this.forwardMatrix = forwardMatrix;
		this.backwardMatrix = Matrices.inverse(forwardMatrix);
	}
	
	/**
	 * Creates linear coordinate transformation with given origin, forward and backward matrix.
	 * @param origin origin of the inner coordinate system
	 * @param forwardMatrix forward transformation matrix
	 * @param backwardMatrix backward transformation matrix
	 */
	public LinearCoordinateTransformationImpl (final IVector origin, final IMatrix forwardMatrix, final IMatrix backwardMatrix) {
		this.origin = origin;
		this.forwardMatrix = forwardMatrix;
		this.backwardMatrix = backwardMatrix;
	}
	
	/**
	 * Transforms point by this transformation.
	 * @param point original point
	 * @return transformed point
	 */
	public IVector transformPointForward (final IVector point) {
		return Vectors.plus(origin, Matrices.multiply(point, forwardMatrix));
	}
	
	/**
	 * Inverse transforms point by this transformation
	 * @param point transformed point
	 * @return original point
	 */
	public IVector transformPointBackward (final IVector point) {
		return Matrices.multiply (Vectors.minus(point, origin), backwardMatrix);
	}

	/**
	 * Returns origin of the inner coordinate system.
	 * @return origin of the inner coordinate system defined in outer coordinate system
	 */
	public IVector getOrigin() {
		return origin;
	}
	
	/**
	 * Returns forward transformation matrix.
	 * @return forward transformation matrix
	 */
	public IMatrix getForwardMatrix() {
		return forwardMatrix;
	}
	
	/**
	 * Returns backward transformation matrix.
	 * @return backward transformation matrix
	 */
	public IMatrix getBackwardMatrix() {
		return backwardMatrix;
	}
	
}
