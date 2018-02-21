package bishopTests;

import java.io.IOException;

public class BtTest extends EloTest {
	
	private static final long MAX_TIME = 900000;
	
	private final long baseElo;
	private long totalTime;
	
	public BtTest(final long baseElo) {
		this.baseElo = baseElo;
	}
	
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
		return (int) (baseElo - totalTime / 30000);
	}
	
	public static void main (final String[] args) throws IOException, InterruptedException {
		final String base = args[2];
		final int baseElo = Integer.parseInt(base);
		
		final BtTest test = new BtTest(baseElo);
		test.runTest(args);
	}

}
