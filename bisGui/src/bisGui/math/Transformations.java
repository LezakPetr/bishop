package bisGui.math;

import math.IMatrixRead;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

public class Transformations {

	/**
	 * Creates translation transformation.
	 * @param vector translation vector
	 * @return translation transformation
	 */
	public static ILinearCoordinateTransformation getTranslation (final IVectorRead vector) {
		final int dimension = vector.getDimension();
		final IMatrixRead identityMatrix = Matrices.getIdentityMatrix(dimension);
		
		return new LinearCoordinateTransformationImpl(vector, identityMatrix, identityMatrix);
	}
	
	/**
	 * Creates scaling transformation.
	 * Transformation is defined by scaling vector - it defines coefficient for each coordinate. 
	 * @param vector scaling vector
	 * @return scaling transformation
	 */
	public static ILinearCoordinateTransformation getScaling (final IVectorRead vector) {
		final int dimension = vector.getDimension();
		final IVectorRead origin = Vectors.getZeroVector(dimension);
		final IMatrixRead matrix = Matrices.getDiagonalMatrix(vector);
		
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
		
		IVectorRead origin = transformations[lastIndex].getOrigin();
		IMatrixRead forwardMatrix = transformations[lastIndex].getForwardMatrix();
		
		for (int i = lastIndex-1; i >= 0; i--) {
			origin = transformations[i].transformPointForward(origin);
			forwardMatrix = transformations[i].getForwardMatrix().multiply(forwardMatrix);
		}
		
		return new LinearCoordinateTransformationImpl(origin, forwardMatrix);
	}

}
