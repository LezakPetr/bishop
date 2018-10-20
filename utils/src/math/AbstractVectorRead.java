package math;

import java.util.Objects;

public abstract class AbstractVectorRead implements IVectorRead {
    @Override
    public boolean equals (final Object obj) {
        if (!(obj instanceof IVectorRead))
            return false;

        final IVectorRead that = (IVectorRead) obj;

        if (this.getDimension() != that.getDimension())
            return false;

        return getZeroVectorTester(that)
                .isZero();
    }

    private ZeroVectorTester getZeroVectorTester(IVectorRead that) {
        return BinaryVectorAlgorithmOneNonzero.getInstance()
                .processElements(this, that, (x, y) -> x - y, new ZeroVectorTester());
    }

    @Override
    public int hashCode() {
        int hash = 0;

        for (IVectorIterator it = getNonZeroElementIterator(); it.isValid(); it.next()) {
            final double element = it.getElement();

            if (element != 0)
                hash ^= it.getIndex() + Double.hashCode(element);
        }

        return hash;
    }

    @Override
    public int compareTo (final IVectorRead that) {
        if (this.getDimension() != that.getDimension())
            return Integer.compare(this.getDimension(), that.getDimension());

        final double firstNonZeroValue = getZeroVectorTester(that).getFirstNonZeroValue();

        return (int) Math.signum(firstNonZeroValue);
    }

	private static void checkSameDimensions(final IVectorRead a, final IVectorRead b) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);

		if (a.getDimension() != b.getDimension())
			throw new RuntimeException("Dimensions of input vectors does not match, they are " + a.getDimension() + ", " + b.getDimension());
	}

	@Override
	public IVectorRead negate() {
    	return UnaryVectorAlgorithm.getInstance()
				.processElements(this, x -> -x, new VectorSetter())
				.getVector();
	}

	@Override
	public IVectorRead plus (final IVectorRead that) {
		checkSameDimensions(this, that);

		if (this.isZero())
			return that.immutableCopy();

		if (that.isZero())
			return this.immutableCopy();

		return BinaryVectorAlgorithmOneNonzero.getInstance()
				.processElements(this, that, Double::sum, new VectorSetter())
				.getVector();
	}

	@Override
	public IVectorRead minus (final IVectorRead that) {
		checkSameDimensions(this, that);

		if (that.isZero())
			return this.immutableCopy();

		if (this.isZero())
			return that.negate();

		return BinaryVectorAlgorithmOneNonzero.getInstance()
				.processElements(this, that, (x, y) -> x - y, new VectorSetter())
				.getVector();
	}

	@Override
	public IVectorRead multiply (final double c) {
    	if (c == 0 || this.isZero())
    		return Vectors.getZeroVector(getDimension());

    	return UnaryVectorAlgorithm.getInstance().processElements(this, x -> c * x, new VectorSetter()).getVector();
	}

	@Override
	public IVectorRead elementMultiply (final IVectorRead that) {
		checkSameDimensions(this, that);

		if (this.isZero() || that.isZero())
			return Vectors.getZeroVector(getDimension());

		return BinaryVectorAlgorithmBothNonzero.getInstance()
				.processElements(this, that, (x, y) -> x * y, new VectorSetter())
				.getVector();
	}

	@Override
	public IVectorRead elementDivide (final IVectorRead that) {
    	final int dimension = this.getDimension();

		if (this.getDimension() != dimension)
			throw new RuntimeException("Different dimensions");

		final IVector result = Vectors.vectorWithDensity(this.density(), dimension);

		for (IVectorIterator it = this.getNonZeroElementIterator(); it.isValid(); it.next()) {
			final int index = it.getIndex();
			result.setElement(index, it.getElement() / that.getElement(index));
		}

		return result.freeze();
	}

	@Override
	public IVectorRead normalize() {
    	final double length = getLength();

		return multiply(1 / length);
	}

	@Override
	public double getLength() {
		final double squareSum = UnaryVectorAlgorithm.getInstance().processElements(this, x -> x*x, new VectorElementSum()).getSum();

		return Math.sqrt(squareSum);
	}

	@Override
	public IVectorRead multiplyAndAdd(final IVectorRead that, final double coeff) {
    	if (coeff == 0 || that.isZero())
    		return this.immutableCopy();

    	if (this.isZero())
    		return that.multiply(coeff);

    	return BinaryVectorAlgorithmOneNonzero.getInstance()
				.processElements(this, that, (x, y) -> x + coeff * y, new VectorSetter())
				.getVector();
	}

	@Override
	public double dotProduct(final IVectorRead that) {
		return BinaryVectorAlgorithmBothNonzero.getInstance()
				.processElements(this, that, (x, y) -> x * y, new VectorElementSum())
				.getSum();
	}

	@Override
	public IVectorRead multiply (final IMatrixRead m) {
    	final int rowCount = m.getRowCount();

		if (rowCount != this.getDimension())
			throw new RuntimeException("Bad dimensions");

		final int columnCount = m.getColumnCount();

		if (this.isZero() || m.isZero())
			return Vectors.getZeroVector(columnCount);

		final Density density = Matrices.minDensity(this.density(), m.density());
		final IVector result = Vectors.vectorWithDensity(density, columnCount);

		for (int column = 0; column < columnCount; column++) {
			final double dotProduct = this.dotProduct (m.getColumnVector (column));

			if (dotProduct != 0)
				result.setElement(column, dotProduct);
		}

		return result.freeze();
	}


}
