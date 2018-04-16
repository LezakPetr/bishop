package bishopTests;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.Color;
import bishop.base.Square;
import bishop.engine.BlockedPawnEndingEvaluator;
import bishop.engine.PawnEndingKey;
import bishop.engine.TablebasePositionEvaluator;
import bishop.tablebase.Classification;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class BlockedPawnEndingEvaluatorTest {
    private static class TestCase {
        private final long[] pawnMasks = new long[Color.LAST];

        public TestCase (final String whitePawnMask, final String blackPawnMask) {
            pawnMasks[Color.WHITE] = BitBoard.fromString(whitePawnMask);
            pawnMasks[Color.BLACK] = BitBoard.fromString(blackPawnMask);
        }
    }

    private final TestCase[] TEST_CASES = {
        new TestCase(
            "d5", "d6"
        ),
        new TestCase(
                "a5", "a6, a7"
        )
    };

    @Test
    public void test() {
        final String tbbsDir = "/home/petr/tbbs/";   // TODO
        final TablebasePositionEvaluator tablebaseEvaluator = new TablebasePositionEvaluator(new File(tbbsDir));

        for (TestCase testCase: TEST_CASES) {
            final BlockedPawnEndingEvaluator evaluator = new BlockedPawnEndingEvaluator(tablebaseEvaluator, new PawnEndingKey(testCase.pawnMasks));
            final long pawnOccupancy = testCase.pawnMasks[Color.WHITE] | testCase.pawnMasks[Color.BLACK];

            for (BitLoop whiteKingLoop = new BitLoop(~pawnOccupancy); whiteKingLoop.hasNextSquare(); ) {
                final int whiteKingSquare = whiteKingLoop.getNextSquare();

                for (BitLoop blackKingLoop = new BitLoop(~pawnOccupancy); blackKingLoop.hasNextSquare(); ) {
                    final int blackKingSquare = blackKingLoop.getNextSquare();

                    for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
                        int expectedClassification = evaluator.getTerminalClassification(whiteKingSquare, blackKingSquare, onTurn);

                        if (expectedClassification == Classification.ILLEGAL || expectedClassification == Classification.UNKNOWN)
                            expectedClassification = Classification.WIN;

                        final int givenClassification = evaluator.getClassification(whiteKingSquare, blackKingSquare, onTurn);

                        Assert.assertEquals(
                                "Different classification for " + Square.toString(blackKingSquare) + " " + Square.toString(whiteKingSquare) + " " + Color.getNotation(onTurn),
                                expectedClassification,
                                givenClassification
                        );
                    }
                }
            }
        }
    }
}
