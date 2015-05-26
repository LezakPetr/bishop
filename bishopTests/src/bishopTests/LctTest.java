package bishopTests;

import java.io.IOException;

public class LctTest extends EloTest {
	
	private static final long MAX_TIME = 10 * 60 * 1000;   // 10 min [ms]
	
	private int elo;
	
	@Override
	protected long getMaxTimeForPosition() {
		return MAX_TIME;
	}

	@Override
	protected void initialize() {
		elo = 1900;
	}
	
	private static int getPoints(final boolean correct, final long time) {
		if (correct) {
			final int seconds = (int) (time / 1000);
			
			if (seconds < 10)
				return 30;

			if (seconds < 30)
				return 25;

			if (seconds < 90)
				return 20;

			if (seconds < 210)
				return 15;

			if (seconds < 390)
				return 10;

			return 5;
		}
		
		return 0;
	}

	@Override
	protected void processResult(final boolean correct, final long time) {
		final int points = getPoints(correct, time);
		elo += points;
		
		System.out.println ("Points: " + points);
	}

	@Override
	protected int calculateElo() {
		return elo;
	}
	
	public static void main (final String[] args) throws IOException, InterruptedException {
		final LctTest test = new LctTest();
		test.runTest(args);
	}

}
