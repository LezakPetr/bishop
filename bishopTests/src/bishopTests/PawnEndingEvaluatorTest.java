package bishopTests;

import bishop.base.*;
import bishop.engine.*;
import bishop.tablebase.Classification;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PawnEndingEvaluatorTest {

    private static final String TBBS_DIR = "/home/petr/tbbs/";   // TODO
    private static final int SPEED_COUNT = 20000;

    private static class TestCase {
        private final long[] pawnMasks = new long[Color.LAST];

        public TestCase (final String whitePawnMask, final String blackPawnMask) {
            pawnMasks[Color.WHITE] = BitBoard.fromString(whitePawnMask);
            pawnMasks[Color.BLACK] = BitBoard.fromString(blackPawnMask);
        }
    }

    private final TestCase[] NON_TERMINAL_TEST_CASES = {
        new TestCase(
            "d5", "d6"
        ),
        new TestCase(
            "a5", "a6, a7"
        ),
        new TestCase(
            "b5", ""
        ),
        new TestCase(
                "", "d5"
        ),
        new TestCase(
                "", "d5"
        ),
        new TestCase(
            "b7", "d2"
        ),
        new TestCase(
            "b7", "d4"
        ),
        new TestCase(
        "b7", "d5"
        ),
        new TestCase(
        "b7, c7", ""
		)
    };

    private final TestCase[] TERMINAL_TEST_CASES = {
        new TestCase(
                "b8", "d7"
        ),
        new TestCase(
                "b8", "d5"
        ),
        new TestCase(
                "b7", "d1"
        )
    };

	private final TestCase[] COMPUTABLE_TEST_CASES = {
			new TestCase(
					"e5, f4", "e6, f5"
			),
			new TestCase(
					"e5, f4, g3, h4", "e6, f5, g4, h5"
			)

	};

	private final TestCase[] NON_COMPUTABLE_TEST_CASES = {
			new TestCase(
					"e5, f4", "e6, f5, e7"
			),
			new TestCase(
					"e5, f4", "e6"
			),
			new TestCase(
					"b2, b3, c4", "b4, c5"
			)
	};

	@Test
    public void test() {
        final TablebasePositionEvaluator tablebaseEvaluator = new TablebasePositionEvaluator(new File(TBBS_DIR));
        final PawnEndingTableRegister register = new PawnEndingTableRegister(tablebaseEvaluator);

        for (TestCase testCase: NON_TERMINAL_TEST_CASES) {
            final List<String> errors = new ArrayList<>();

            final PawnEndingKey key = new PawnEndingKey(testCase.pawnMasks);
            final PawnEndingTerminalPositionEvaluator evaluator = new PawnEndingTerminalPositionEvaluator(tablebaseEvaluator, register, key);
            final PawnEndingTable table = PawnEndingEvaluator.calculateTable(null, register, new PawnEndingKey(testCase.pawnMasks));
            final long pawnOccupancy = testCase.pawnMasks[Color.WHITE] | testCase.pawnMasks[Color.BLACK];
            final TablebasePawnEndingTable tablebaseTable = new TablebasePawnEndingTable(key, tablebaseEvaluator);
            final int[] allowedOnTurns = getAllowedOnTurns(testCase);

            for (BitLoop whiteKingLoop = new BitLoop(~pawnOccupancy); whiteKingLoop.hasNextSquare(); ) {
                final int whiteKingSquare = whiteKingLoop.getNextSquare();

                for (BitLoop blackKingLoop = new BitLoop(~pawnOccupancy); blackKingLoop.hasNextSquare(); ) {
                    final int blackKingSquare = blackKingLoop.getNextSquare();

                    for (int onTurn: allowedOnTurns) {
                        verifyPosition(errors, tablebaseTable, table, whiteKingSquare, blackKingSquare, onTurn);
                    }
                }
            }

            printErrors(errors, key);
        }
    }

    private void printErrors(List<String> errors, PawnEndingKey key) {
        if (errors.size() > 0) {
            System.out.println ("For key: " + key);
            for (String error: errors)
                System.out.println (error);

            Assert.fail("Different classification: " + errors.size());
        }
    }

    @Test
    public void testTerminal() {
        final TablebasePositionEvaluator tablebaseEvaluator = new TablebasePositionEvaluator(new File(TBBS_DIR));
        final PawnEndingTableRegister register = new PawnEndingTableRegister(tablebaseEvaluator);

        for (TestCase testCase: TERMINAL_TEST_CASES) {
            final List<String> errors = new ArrayList<>();

            final PawnEndingKey key = new PawnEndingKey(testCase.pawnMasks);
            final PawnEndingTerminalPositionEvaluator evaluator = new PawnEndingTerminalPositionEvaluator(tablebaseEvaluator, register, key);
            evaluator.setUseTablebase(false);

            final int onTurn = Color.getOppositeColor(key.getPromotedPawnColor());
            final PawnEndingTable table = evaluator.calculateTable(onTurn);
            final long pawnOccupancy = testCase.pawnMasks[Color.WHITE] | testCase.pawnMasks[Color.BLACK];

            final TablebasePawnEndingTable tablebaseTable = new TablebasePawnEndingTable(key, tablebaseEvaluator);

            for (BitLoop whiteKingLoop = new BitLoop(~pawnOccupancy); whiteKingLoop.hasNextSquare(); ) {
                final int whiteKingSquare = whiteKingLoop.getNextSquare();

                for (BitLoop blackKingLoop = new BitLoop(~pawnOccupancy); blackKingLoop.hasNextSquare(); ) {
                    final int blackKingSquare = blackKingLoop.getNextSquare();

                    verifyPosition(errors, tablebaseTable, table, whiteKingSquare, blackKingSquare, onTurn);
                }
            }

            printErrors(errors, key);
        }
    }

    private int[] getAllowedOnTurns(TestCase testCase) {
        return Color.stream()
                        .filter(c -> (testCase.pawnMasks[c] & BoardConstants.RANK_18_MASK) == 0)
                        .toArray();
    }

    private void verifyPosition(List<String> errors, TablebasePawnEndingTable tablebaseTable, PawnEndingTable table, int whiteKingSquare, int blackKingSquare, int onTurn) {
        int expectedClassification = tablebaseTable.getTablebaseEvaluation(whiteKingSquare, blackKingSquare, onTurn);

        if (expectedClassification != Classification.ILLEGAL) {
            if (expectedClassification == Classification.ILLEGAL || expectedClassification == Classification.UNKNOWN)
                expectedClassification = Classification.WIN;

            final int givenClassification = (onTurn == Color.WHITE) ?
                    table.getClassification(whiteKingSquare, blackKingSquare, onTurn) :
                    table.getClassification(blackKingSquare, whiteKingSquare, onTurn);

            if (givenClassification != expectedClassification)
                errors.add("BK = " + Square.toString(blackKingSquare) + ", WK = " + Square.toString(whiteKingSquare) + ", onTurn = " + Color.getNotation(onTurn) + ", expected = " + expectedClassification + ", given = " + givenClassification);
        }
    }

    private void testSpeed(TablebasePositionEvaluator tablebaseEvaluator, final PawnEndingTableRegister register) {
        for (TestCase testCase: NON_TERMINAL_TEST_CASES) {
            for (int i = 0; i < SPEED_COUNT; i++) {
                final PawnEndingEvaluator evaluator = new PawnEndingEvaluator(register, new PawnEndingKey(testCase.pawnMasks));
                evaluator.getTable();
            }
        }
    }

    @Test
    public void speedTest()
    {
        final TablebasePositionEvaluator tablebaseEvaluator = new TablebasePositionEvaluator(new File(TBBS_DIR));
        final PawnEndingTableRegister register = new PawnEndingTableRegister(tablebaseEvaluator);

        testSpeed(tablebaseEvaluator, register);

        final long t1 = System.currentTimeMillis();
        testSpeed(tablebaseEvaluator, register);
        final long t2 = System.currentTimeMillis();
        final long dt = t2 - t1;
        final long speed = 1000 * SPEED_COUNT * NON_TERMINAL_TEST_CASES.length / dt;

        System.out.println("t = " + dt + " ms, speed = " + speed + " tables/s");
    }

    @Test
	public void testComputable() {
		final List<TestCase> testCases = new ArrayList<>();
		testCases.addAll(Arrays.asList(NON_TERMINAL_TEST_CASES));
		testCases.addAll(Arrays.asList(TERMINAL_TEST_CASES));
		testCases.addAll(Arrays.asList(COMPUTABLE_TEST_CASES));

		for (TestCase testCase: testCases) {
			final PawnEndingKey key = new PawnEndingKey(testCase.pawnMasks);
			Assert.assertTrue(key.estimateComplexity() < Long.MAX_VALUE);
		}
	}

	@Test
	public void testNotComputable() {
		for (TestCase testCase: NON_COMPUTABLE_TEST_CASES) {
			final PawnEndingKey key = new PawnEndingKey(testCase.pawnMasks);
			Assert.assertEquals(Long.MAX_VALUE, key.estimateComplexity());
		}
	}

}

