package utilsTest;


import org.junit.Assert;
import org.junit.Test;
import parallel.ParallelTaskRunner;

public class ParallelTaskRunnerTest {

	private static class Task implements Runnable {
		public int param;
		public int result;

		@Override
		public void run() {
			result = 2 * param;
		}
		
	}
	
	private void doTest (final ParallelTaskRunner runner, final int count) {
		final Task task = new Task();
		
		for (int i = 0; i < count; i++) {
			task.param = i;
			runner.startTask(task);
			runner.joinTask();
			
			Assert.assertEquals(2 * i, task.result);
		}		
	}
	
	@Test
	public void test() {
		final int count = 30000000;
		
		final ParallelTaskRunner runner = new ParallelTaskRunner();
		runner.start();
		
		doTest(runner, count / 2);
		
		final long t1 = System.currentTimeMillis();
		doTest(runner, count);
		final long t2 = System.currentTimeMillis();
		final long time = 1000000L * (t2 - t1) / count;
		
		System.out.println(time + "ns");
		
		runner.stop();
	}
}
