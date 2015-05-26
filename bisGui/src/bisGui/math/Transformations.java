package bisGui.math;

public class Transformations {

	/**
	 * Creates translation transformation.
	 * @param vector translation vector
	 * @return translation transformation
	 */
	public static ILinearCoordinateTransformation getTranslation (final IVector vector) {
		final int dimension = vector.getDimension();
		final IMatrix identityMatrix = Matrices.getIdentityMatrix(dimension);
		
		return new LinearCoordinateTransformationImpl(vector, identityMatrix, identityMatrix);
	}
	
	/**
	 * Creates scaling transformation.
	 * Transformation is defined by scaling vector - it defines coefficient for each coordinate. 
	 * @param vector scaling vector
	 * @return scaling transformation
	 */
	public static ILinearCoordinateTransformation getScaling (final IVector vector) {
		final int dimension = vector.getDimension();
		final IVector origin = Vectors.getZeroVector(dimension);
		final IMatrix matrix = Matrices.getDiagonalMatrix(vector);
		
		return new LinearCoordinateTransformationImpl(origin, matrix, matrix);
	}
	
	/**
	 * Creates new linear transformation by composing given transformations.
	 * If transformations is {a, b, c} then method returns transformation a(b(c)).
	 * @param transformations array of transformations
	 * @return compound transformation
	 */
	public static ILinearCoordinateTransformation composeLinearTransformations (final ILinearCoordinateTransformation[] transformations) {
		final int lastIndex = transformations.length - 1;
		
		IVector origin = transformations[lastIndex].getOrigin();
		IMatrix forwardMatrix = transformations[lastIndex].getForwardMatrix();
		
		for (int i = lastIndex-1; i >= 0; i--) {
			origin = transformations[i].transformPointForward(origin);
			forwardMatrix = Matrices.multiply(transformations[i].getForwardMatrix(), forwardMatrix);
		}
		
		return new LinearCoordinateTransformationImpl(origin, forwardMatrix);
	}

}
