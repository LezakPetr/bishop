package bishopTests;

import java.io.IOException;

public class BtTest extends EloTest {
	
	private static final long MAX_TIME = 900000;
	
	private long totalTime;
	
	@Override
	protected long getMaxTimeForPosition() {
		return MAX_TIME;
	}
	

	@Override
	protected void initialize() {
		totalTime = 0;
	}

	@Override
	protected void processResult(final boolean correct, final long time) {
		if (correct)
			totalTime += Math.min (time, MAX_TIME);
		else
			totalTime += MAX_TIME;
	}

	@Override
	protected int calculateElo() {
		return (int) (2450 - totalTime / 30000);
	}
	
	public static void main (final String[] args) throws IOException, InterruptedException {
		final BtTest test = new BtTest();
		test.runTest(args);
	}

}
