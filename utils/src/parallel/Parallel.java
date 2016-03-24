package parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Holder;

/**
 * Parallel utilities.
 * @author Ing. Petr Ležák
 */
public class Parallel {
	
	public static final int DEFAULT_RUNNER_COUNT = 16;
	
	private final ExecutorService executor;
	private final int threadCount;
	
	private final ITaskRunner[] allTaskRunners;
	private final ParallelTaskRunner[] parallelTaskRunners;
	
	public Parallel (final ExecutorService executor, final int threadCount, final int allRunnerCount, final int parallelRunnerCount) {
		this.executor = executor;
		this.threadCount = threadCount;
		this.allTaskRunners = new ITaskRunner[allRunnerCount];
		this.parallelTaskRunners = new ParallelTaskRunner[parallelRunnerCount];
		
		for (int i = 0; i < parallelRunnerCount; i++) {
			parallelTaskRunners[i] = new ParallelTaskRunner();
			allTaskRunners[i] = parallelTaskRunners[i];
		}
		
		for (int i = parallelRunnerCount; i < allRunnerCount; i++)
			allTaskRunners[i] = new SerialTaskRunner();
	}

	public Parallel(final int threadCount, final int allRunnerCount, final int parallelRunnerCount) {
		this (Executors.newFixedThreadPool(threadCount), threadCount, allRunnerCount, parallelRunnerCount);
	}

	public Parallel() {
		this(Runtime.getRuntime().availableProcessors(), DEFAULT_RUNNER_COUNT, 0);
	}

	public Parallel(final int threadCount) {
		this(threadCount, DEFAULT_RUNNER_COUNT, threadCount - 1);
	}

	/**
	 * Invokes all callables. If any of them returns an exception then RuntimeException is thrown.
	 * @param callableList list of callables to call.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void invokeAll (final List<? extends Callable<Throwable>> callableList) throws InterruptedException, ExecutionException {
		final List<Future<Throwable>> futureList = executor.invokeAll(callableList);
		Throwable firstThrowable = null;
		
		for (Future<Throwable> future: futureList) {
			final Throwable th = future.get();
			
			if (firstThrowable == null && th != null)
				firstThrowable = th;
		}
		
		if (firstThrowable != null)
			throw new RuntimeException("Inner exception", firstThrowable);
	}
	
	public static void invokaAllSerial(List<Callable<Throwable>> callableList) {
		Throwable firstThrowable = null;
		
		for (Callable<Throwable> callable: callableList) {
			Throwable th;
		
			try {
				th = callable.call();
			}
			catch (Throwable t) {
				th = t;
			}

			if (firstThrowable == null && th != null)
				firstThrowable = th;
		}
		
		if (firstThrowable != null)
			throw new RuntimeException("Inner exception", firstThrowable);			
	}
	
	/**
	 * Invokes given callable on threadCount threads. 
	 * @param callable callable to invoke
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void runParallel (final Callable<Throwable> callable) throws InterruptedException, ExecutionException {
		final List<Callable<Throwable>> processorList = new ArrayList<Callable<Throwable>>();
		processorList.addAll(Collections.nCopies(threadCount, callable));
		
		invokeAll (processorList);
	}
	
	/**
	 * Ensures calling body.run (index) for from <= index < to.
	 * @param from lower index boundary (inclusive)
	 * @param to upper index boundary (inclusive)
	 * @param body body to call
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void parallelFor (final int from, final int to, final IForBody body) throws InterruptedException, ExecutionException {
		final Holder<Integer> indexHolder = new Holder<>(from);
		
		runParallel(new Callable<Throwable>() {
			public Throwable call() {
				try {
					while (true) {
						final int index;
						
						synchronized (indexHolder) {
							index = indexHolder.value;
							indexHolder.value = index + 1;
						}
						
						if (index >= to)
							break;
						
						body.run(index);
					}
					
					
					return null;
				}
				catch (Throwable th) {
					return th;
				}
			}
		});
	}

	/**
	 * Returns number of threads.
	 * @return number of threads
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * Returns executor service.
	 * @return executor service
	 */
	public ExecutorService getExecutor() {
		return executor;
	}

	/**
	 * Shots down the executor.
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);
	}
	
	public void startTaskRunners() {
		for (ParallelTaskRunner runner: parallelTaskRunners)
			runner.start();
	}

	public void stopTaskRunners() {
		for (ParallelTaskRunner runner: parallelTaskRunners)
			runner.stop();
	}
	
	public ITaskRunner getTaskRunner (final int index) {
		return allTaskRunners[index];
	}

}
