package regression;

import collections.ImmutableEnumSet;
import utils.EnumToken;

public enum ScalarFieldCharacteristic {
    VALUE,
    GRADIENT,
    HESSIAN;

    public static final EnumToken<ScalarFieldCharacteristic> TOKEN = EnumToken.of(ScalarFieldCharacteristic.class);

    public static ImmutableEnumSet<ScalarFieldCharacteristic> SET_VALUE = ImmutableEnumSet.of (TOKEN, VALUE);
    public static ImmutableEnumSet<ScalarFieldCharacteristic> SET_GRADIENT = ImmutableEnumSet.of (TOKEN, GRADIENT);
    public static ImmutableEnumSet<ScalarFieldCharacteristic> SET_HESSIAN = ImmutableEnumSet.of (TOKEN, HESSIAN);

    public static ImmutableEnumSet<ScalarFieldCharacteristic> SET_GRADIENT_HESSIAN = ImmutableEnumSet.of (TOKEN, GRADIENT, HESSIAN);
    public static ImmutableEnumSet<ScalarFieldCharacteristic> SET_VALUE_GRADIENT = ImmutableEnumSet.of (TOKEN, VALUE, GRADIENT);
    public static ImmutableEnumSet<ScalarFieldCharacteristic> SET_ALL = ImmutableEnumSet.allOf (TOKEN);

}
