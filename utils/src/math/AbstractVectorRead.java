package math;

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

}
