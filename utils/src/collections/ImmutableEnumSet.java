package collections;

import utils.EnumToken;

import java.util.Iterator;


public class ImmutableEnumSet<E extends Enum<E>> implements Iterable<E> {

    public static final int MAX_SIZE = Long.SIZE;

    private final EnumToken<E> token;
    private final long elements;

    private class EnumSetIterator implements Iterator<E> {
        private long remainingElements;
        private int nextIndex;

        public EnumSetIterator() {
            this.remainingElements = elements;
            findNextIndex();
        }

        private void findNextIndex() {
            nextIndex = Long.numberOfTrailingZeros(remainingElements);
        }

        @Override
        public boolean hasNext() {
            return nextIndex < MAX_SIZE;
        }

        @Override
        public E next() {
            remainingElements &= ~(1L << nextIndex);

            final E element = token.getConstants().get(nextIndex);
            findNextIndex();

            return element;
        }
    }

    private ImmutableEnumSet (final EnumToken<E> token, final long elements) {
        this.token = token;
        this.elements = elements;
    }

    public int size() {
        return Long.bitCount(elements);
    }

    public boolean isEmpty() {
        return elements == 0;
    }

    public boolean contains(final E element) {
        return ((elements >>> element.ordinal()) & 0x01) != 0;
    }

    public boolean containsAll(final ImmutableEnumSet<E> that) {
        return (that.elements & ~this.elements) == 0;
    }

    public boolean containsAny(final ImmutableEnumSet<E> that) {
        return (this.elements & that.elements) != 0;
    }

    private static <E extends Enum<E>> void checkToken(EnumToken<E> token) {
        if (token.getConstants().size() > MAX_SIZE)
            throw new RuntimeException("Enum is too big for LongImmutableEnumSet");
    }

    public static <E extends Enum<E>> ImmutableEnumSet<E> of (final EnumToken<E> token, final E ...elements) {
        checkToken(token);

        long elementMask = 0;

        for (E element: elements)
            elementMask |= 1L << element.ordinal();

        return new ImmutableEnumSet<>(token, elementMask);
    }

    public static <E extends Enum<E>> ImmutableEnumSet<E> allOf (final EnumToken<E> token) {
        checkToken(token);

        final int constantCount = token.getConstants().size();
        final long elementMask = (constantCount == MAX_SIZE) ? ~0L : (1L << constantCount) - 1;

        return new ImmutableEnumSet<>(token, elementMask);
    }

    public static <E extends Enum<E>> ImmutableEnumSet<E> noneOf (final EnumToken<E> token) {
        checkToken(token);

        return new ImmutableEnumSet<>(token, 0L);
    }

    public static <E extends Enum<E>> ImmutableEnumSet<E> union (final ImmutableEnumSet<E> a, final ImmutableEnumSet<E> b) {
        return new ImmutableEnumSet<>(a.token, a.elements | b.elements);
    }

    public static <E extends Enum<E>> ImmutableEnumSet<E> intersection (final ImmutableEnumSet<E> a, final ImmutableEnumSet<E> b) {
        return new ImmutableEnumSet<>(a.token, a.elements & b.elements);
    }

    @Override
    public Iterator<E> iterator() {
        return new EnumSetIterator();
    }
}
