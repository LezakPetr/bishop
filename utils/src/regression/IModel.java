package regression;

import math.IVectorRead;

public interface IModel {
    public IVectorRead apply (final IVectorRead x);
}
