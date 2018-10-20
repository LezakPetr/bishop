package regression;

import collections.ImmutableEnumSet;
import math.IMatrixRead;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class ScalarPointCharacteristics {
    private final double value;
    private final IVectorRead gradient;
    private final IMatrixRead hessian;

    public ScalarPointCharacteristics(final double value, final IVectorRead gradient, final IMatrixRead hessian) {
        this.value = value;
        this.gradient = (gradient != null) ? gradient.immutableCopy() : null;
        this.hessian = (hessian != null) ? hessian.immutableCopy() : null;
    }

    public ScalarPointCharacteristics(final DoubleSupplier valueSupplier, final Supplier<IVectorRead> gradientSupplier, final Supplier<IMatrixRead> hessianSupplier, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        if (characteristics.contains(ScalarFieldCharacteristic.VALUE))
            this.value = valueSupplier.getAsDouble();
        else
            this.value = Double.NaN;

        if (characteristics.contains(ScalarFieldCharacteristic.GRADIENT))
            this.gradient = gradientSupplier.get();
        else
            this.gradient = null;

        if (characteristics.contains(ScalarFieldCharacteristic.HESSIAN))
            this.hessian = hessianSupplier.get();
        else
            this.hessian = null;
    }

    public double getValue() {
        return value;
    }

    public IVectorRead getGradient() {
        return gradient;
    }

    public IMatrixRead getHessian() {
        return hessian;
    }
}
