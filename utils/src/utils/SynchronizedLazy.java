package utils;

import java.util.function.Supplier;

public class SynchronizedLazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private volatile T value;

    private SynchronizedLazy(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        final T cachedValueNoLock = value;

        if (cachedValueNoLock != null)
            return cachedValueNoLock;

        synchronized (this) {
            final T cachedValueWithLock = value;

            if (cachedValueWithLock != null)
                return value;

            final T calculatedValue = supplier.get();
            value = calculatedValue;

            return calculatedValue;
        }
    }

    public static <T> SynchronizedLazy<T> of(final Supplier<T> supplier) {
        return new SynchronizedLazy<>(supplier);
    }
}
