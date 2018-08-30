package regression;

import collections.ImmutableEnumSet;
import math.IVector;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

import java.util.ArrayList;
import java.util.List;

public class LinearFeatureCombination implements ISampleCostField {

    private final List<IScalarField> featureList = new ArrayList<>();

    public void addFeature(final IScalarField feature) {
        featureList.add(feature);
    }

    @Override
    public int getInputDimension() {
        return featureList.size();
    }

    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final ISample sample, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        final int featureCount = featureList.size();
        double value = 0;
        final IVector gradient = Vectors.sparse(featureCount);

        for (int i = 0; i < featureList.size(); i++) {
            final double featureValue = featureList.get(i).calculateValue(sample.getInput());
            final double parameter = x.getElement(i);

            value += featureValue * parameter;
            gradient.setElement(i, featureValue);
        }

        final double totalValue = value;

        return new ScalarPointCharacteristics(
                () -> totalValue,
                () -> gradient,
                () -> Matrices.getZeroMatrix(featureCount, featureCount),
                characteristics
        );
    }


}
