package regression;

import math.IVectorRead;
import math.Vectors;

public class ScalarWithGradient {
    private final double scalar;
    private final IVectorRead gradient;

    public ScalarWithGradient (final double scalar, final IVectorRead gradient) {
        this.scalar = scalar;
        this.gradient = Vectors.immutableCopy(gradient);
    }

    public double getScalar() {
        return scalar;
    }

    public IVectorRead getGradient() {
        return gradient;
    }
}
