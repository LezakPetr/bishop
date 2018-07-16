package regression;

import math.IVector;
import math.IVectorRead;
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
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final ISample sample) {
        double value = 0;
        IVector gradient = Vectors.dense(featureList.size());

        for (int i = 0; i < featureList.size(); i++) {
            final double featureValue = featureList.get(i).calculateValue(sample.getInput());
            final double parameter = x.getElement(i);

            value += featureValue * parameter;
            gradient.setElement(i, featureValue);
        }

        return new ScalarWithGradient(value, gradient);
    }


}
