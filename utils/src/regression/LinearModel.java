package regression;

import math.IMatrixRead;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

public class LinearModel implements IModel {
    private final IVectorRead origin;
    private final IMatrixRead coeff;

    public LinearModel(final IVectorRead origin, final IMatrixRead coeff) {
        this.origin = origin;
        this.coeff = coeff;
    }

    public LinearModel(final IMatrixRead coeff) {
        this.coeff = coeff;
        this.origin = Vectors.getZeroVector(coeff.getRowCount());
    }

    public IVectorRead apply (final IVectorRead x) {
        return Vectors.plus(origin, Matrices.multiply(coeff, x));
    }
}
