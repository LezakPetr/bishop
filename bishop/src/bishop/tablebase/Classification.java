package bishop.tablebase;

import utils.IntArrayBuilder;

import java.util.function.IntUnaryOperator;

public class Classification {

    public static final int DRAW = 0;
    public static final int WIN = 1;
    public static final int LOSE = 2;
    public static final int COUNT_LEGAL = 3;

    public static final int ILLEGAL = 3;
    public static final int UNKNOWN = 4;

    public static final int COUNT_ALL = 5;

    private static final int[] OPPOSITE_CLASSIFICATIONS = new IntArrayBuilder (COUNT_ALL, IntUnaryOperator.identity())
            .put(WIN, LOSE)
            .put(LOSE, WIN)
            .build();

    public static int getOpposite (final int classification) {
        return OPPOSITE_CLASSIFICATIONS[classification];
    }
}
