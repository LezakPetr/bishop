package parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ParallelUtils {

	public static void runParallel (final ExecutorService executor, final int threadCount, final Callable<Object> callable) throws InterruptedException, ExecutionException {
		final List<Callable<Object>> processorList = new ArrayList<Callable<Object>>();
		
		for (int i = 0; i < threadCount; i++) {
			processorList.add (callable);
		}
		
		final List<Future<Object>> futureList = executor.invokeAll(processorList);
		
		for (Future<Object> future: futureList) {
			future.get();
		}
	}
}
