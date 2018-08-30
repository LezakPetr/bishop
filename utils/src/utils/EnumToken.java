package utils;

import collections.ImmutableList;

import java.util.List;

public class EnumToken<E extends Enum<E>> {
    private final Class<E> enumClass;
    private final List<E> constants;

    private EnumToken (final Class<E> enumClass) {
        this.enumClass = enumClass;
        this.constants = ImmutableList.of(enumClass.getEnumConstants());
    }

    public Class<E> getEnumClass() {
        return enumClass;
    }

    public List<E> getConstants() {
        return constants;
    }

    public static <E extends Enum<E>> EnumToken<E> of (final Class<E> enumClass) {
        return new EnumToken<>(enumClass);
    }
}
