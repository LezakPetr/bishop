package bishopTests;

import bishop.base.*;
import bishop.engine.StaticExchangeEvaluator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

public class StaticExchangeEvaluatorTest {
    private static final PieceTypeEvaluations pte = PieceTypeEvaluations.DEFAULT;

    private static class StaticExchangeTestCase {
        public final String position;
        public final int color;
        public final int square;
        public final int evaluation;

        public StaticExchangeTestCase(final String position, final int color, final int square, final int evaluation) {
            this.position = position;
            this.color = color;
            this.square = square;
            this.evaluation = evaluation;
        }
    }

    @Test
    public void staticExchangeTest() throws IOException {
        final StaticExchangeTestCase[] testCaseArray = {
                new StaticExchangeTestCase("3r3k/6b1/8/8/3r4/8/2N2Q2/7K w - - 0 1", Color.WHITE, Square.D4, pte.getPieceTypeEvaluation(PieceType.ROOK) - pte.getPieceTypeEvaluation(PieceType.KNIGHT)),
                new StaticExchangeTestCase("7k/2Q5/3B2n1/8/5n2/8/8/7K w - - 0 1", Color.WHITE, Square.F4, 2 * pte.getPieceTypeEvaluation(PieceType.KNIGHT) - pte.getPieceTypeEvaluation(PieceType.BISHOP)),
                new StaticExchangeTestCase("5k2/8/2p5/1N6/2P5/8/8/4K3 b - - 0 1", Color.BLACK, Square.B5, pte.getPieceTypeEvaluation(PieceType.KNIGHT) - pte.getPieceTypeEvaluation(PieceType.PAWN)),
                new StaticExchangeTestCase("8/8/4k3/3N4/8/8/8/4K2B b - - 0 1", Color.BLACK, Square.D5, 0),
                new StaticExchangeTestCase("8/8/4k3/3N4/8/5K2/8/7B b - - 0 1", Color.BLACK, Square.D5, pte.getPieceTypeEvaluation(PieceType.KNIGHT))
        };

        final Fen fen = new Fen();

        for (StaticExchangeTestCase testCase: testCaseArray) {
            fen.readFenFromString(testCase.position);

            final Position beginPosition = fen.getPosition();
            final Position position = beginPosition.copy();

            final StaticExchangeEvaluator staticExchangeEvaluator = new StaticExchangeEvaluator(position, pte);

            final int evaluation = staticExchangeEvaluator.getStaticExchangeEvaluationOfSquare(testCase.color, testCase.square);

            Assert.assertEquals(testCase.position, testCase.evaluation, evaluation);
            Assert.assertEquals(testCase.position, beginPosition, position);

            Assert.assertTrue(testCase.position, staticExchangeEvaluator.isStaticExchangeEvaluationOfSquareAtLeast(testCase.color, testCase.square, testCase.evaluation - 1));
            Assert.assertTrue(testCase.position, staticExchangeEvaluator.isStaticExchangeEvaluationOfSquareAtLeast(testCase.color, testCase.square, testCase.evaluation));
            Assert.assertFalse(testCase.position, staticExchangeEvaluator.isStaticExchangeEvaluationOfSquareAtLeast(testCase.color, testCase.square, testCase.evaluation + 1));
        }
    }

    private static class StaticExchangeMoveTestCase {
        public final String position;
        public final String move;
        public final int evaluation;

        public StaticExchangeMoveTestCase(final String position, final String move, final int evaluation) {
            this.position = position;
            this.move = move;
            this.evaluation = evaluation;
        }
    }

    @Test
    public void staticExchangeMoveTest() throws IOException {
        final StaticExchangeMoveTestCase[] testCaseArray = {
                new StaticExchangeMoveTestCase("3r3k/6b1/8/8/3r4/8/2N2Q2/7K w - - 0 1", "Qxd4", pte.getPieceTypeEvaluation(PieceType.ROOK) - pte.getPieceTypeEvaluation(PieceType.QUEEN)),
                new StaticExchangeMoveTestCase("7k/2Q5/3B2n1/8/5n2/8/8/7K w - - 0 1", "Bxf4", 2 * pte.getPieceTypeEvaluation(PieceType.KNIGHT) - pte.getPieceTypeEvaluation(PieceType.BISHOP)),
                new StaticExchangeMoveTestCase("5k2/8/2p5/1N6/2P5/8/8/4K3 b - - 0 1", "cxb5", pte.getPieceTypeEvaluation(PieceType.KNIGHT) - pte.getPieceTypeEvaluation(PieceType.PAWN)),
                new StaticExchangeMoveTestCase("5k2/8/2p5/1N6/2P5/8/8/4K3 b - - 0 1", "c5", 0),
                new StaticExchangeMoveTestCase("8/8/4k3/3N4/8/5K2/8/7B b - - 0 1", "Kxd5", pte.getPieceTypeEvaluation(PieceType.KNIGHT)),
                new StaticExchangeMoveTestCase("QR6/7k/8/8/7q/8/6P1/6K1 b - - 0 1", "Qe1", 0),
                new StaticExchangeMoveTestCase("QR6/7k/8/8/8/8/6PK/4q3 b - - 0 1", "Qh4", 0)
        };

        final Fen fen = new Fen();

        for (StaticExchangeMoveTestCase testCase: testCaseArray) {
            fen.readFenFromString(testCase.position);

            final Position beginPosition = fen.getPosition();
            final Position position = beginPosition.copy();

            final StandardAlgebraicNotationReader moveReader = new StandardAlgebraicNotationReader();
            final Move move = new Move();
            moveReader.readMove(new PushbackReader(new StringReader(testCase.move)), position, move);

            final StaticExchangeEvaluator staticExchangeEvaluator = new StaticExchangeEvaluator(position, pte);
            final int evaluation = staticExchangeEvaluator.getStaticExchangeEvaluationOfMove(position.getOnTurn(), move);

            Assert.assertEquals(testCase.position, testCase.evaluation, evaluation);
            Assert.assertEquals(testCase.position, beginPosition, position);

            final boolean isNonLosing = staticExchangeEvaluator.isMoveNonLosing(position.getOnTurn(), move);
            Assert.assertEquals(testCase.position, testCase.evaluation >= 0, isNonLosing);
        }
    }

}
