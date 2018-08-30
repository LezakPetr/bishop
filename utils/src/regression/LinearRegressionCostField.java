package regression;

import math.Utils;


public class LinearRegressionCostField extends SampleCostFieldImpl {

    public LinearRegressionCostField(final int inputDimension, final int outputIndex, final ISampleCostField valueField) {
        super (
            inputDimension,
            valueField,
            (c, e) -> Utils.sqr(c - e),
            (c, e) -> 2 * (c - e),
            (c, e) -> 2,
            outputIndex
        );
    }
}
