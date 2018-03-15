package bishopTests;

import java.io.IOException;

import bishopTests.PerftCalculator.Statistics;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.Fen;

public class PerftTest {
	

	private static class TestCase {
		private final String position;
		private final Statistics[] statistics;
		
		public TestCase (final String position, final Statistics ...statistics) {
			this.position = position;
			this.statistics = statistics;
		}
	}
	
	private static final TestCase[] TEST_CASES = {
		new TestCase(
			"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
			//                  node  capture    EP castling promotion   check   mate
			new Statistics(       20,       0,    0,       0,        0,      0,     0),
			new Statistics(      400,       0,    0,       0,        0,      0,     0),
			new Statistics(     8902,      34,    0,       0,        0,     12,     0),
			new Statistics(   197281,    1576,    0,       0,        0,    469,     8),
			new Statistics(  4865609,   82719,  258,       0,        0,  27351,   347),
			new Statistics(119060324, 2812008, 5248,       0,        0, 809099, 10828)
		),
		new TestCase(
			"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
			//                  node   capture     EP castling promotion    check   mate
			new Statistics(       48,        8,     0,       2,        0,       0,     0),
			new Statistics(     2039,      351,     1,      91,        0,       3,     0),
			new Statistics(    97862,    17102,    45,    3162,        0,     993,     1),
			new Statistics(  4085603,   757163,  1929,  128013,    15172,   25523,    43),
			new Statistics(193690690, 35043416, 73365, 4993637,     8392, 3309887, 30171)
		),
		new TestCase(
			"8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
			//                  node   capture      EP castling promotion     check   mate
			new Statistics(       14,        1,      0,       0,        0,        2,     0),
			new Statistics(      191,       14,      0,       0,        0,       10,     0),
			new Statistics(     2812,      209,      2,       0,        0,      267,     0),
			new Statistics(    43238,     3348,    123,       0,        0,     1680,    17),
			new Statistics(   674624,    52051,   1165,       0,        0,    52950,     0),
			new Statistics( 11030083,   940350,  33325,       0,     7552,   452473,  2733),
			new Statistics(178633661, 14519036, 294874,       0,   140024, 12797406,    87)
		),
		new TestCase(
			"r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
			//                  node    capture    EP  castling promotion    check    mate
			new Statistics(        6,         0,    0,        0,        0,       0,      0),
			new Statistics(      264,        87,    0,        6,       48,      10,      0),
			new Statistics(     9467,      1021,    4,        0,      120,      38,     22),
			new Statistics(   422333,    131393,    0,     7795,    60032,   15492,      5),
			new Statistics( 15833292,   2046173, 6512,        0,   329464,  200568,  50562),
			new Statistics(706045033, 210369132,  212, 10882006, 81102984, 26973664, 81076)
		)
	};
	
	@Test
	public void test() throws IOException {
		final PerftCalculator calculator = new PerftCalculator();
		final Fen fen = new Fen();
		final Statistics totalStatistics = new Statistics();
		
		final long t1 = System.currentTimeMillis();
		
		for (TestCase testCase: TEST_CASES) {
			fen.readFenFromString(testCase.position);
			
			for (int i = 0; i < testCase.statistics.length; i++) {
				final Statistics calculatedStatistics = calculator.getPerft(fen.getPosition(), i + 1);
				totalStatistics.add(calculatedStatistics);
				
				final Statistics expectedStatistics = testCase.statistics[i];
				
				Assert.assertEquals(expectedStatistics.nodeCount, calculatedStatistics.nodeCount);
				Assert.assertEquals(expectedStatistics.captureCount, calculatedStatistics.captureCount);
				Assert.assertEquals(expectedStatistics.epCount, calculatedStatistics.epCount);
				Assert.assertEquals(expectedStatistics.castlingCount, calculatedStatistics.castlingCount);
				Assert.assertEquals(expectedStatistics.promotionCount, calculatedStatistics.promotionCount);
				Assert.assertEquals(expectedStatistics.checkCount, calculatedStatistics.checkCount);
				Assert.assertEquals(expectedStatistics.mateCount, calculatedStatistics.mateCount);
			}
		}
		
		final long t2 = System.currentTimeMillis();
		final double nps = 1e-3 * totalStatistics.nodeCount / (t2 - t1);
		
		System.out.println("Speed: " + nps + " Mnps");
	}
	
}
