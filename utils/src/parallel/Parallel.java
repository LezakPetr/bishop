package parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
	 * Ensures calling body.accept(e) for every item e in items.
	 * @param items items
	 * @param body body to call
	 */
	public <T> void parallelForEach (final Collection<T> items, final Consumer<T> body) throws InterruptedException, ExecutionException {
		final List<T> list;
		
		if (items instanceof List<?>)
			list = (List<T>) items;
		else
			list = new ArrayList<T>(items);
		
		final int size = list.size();
		
		parallelFor(0, size, (i) -> body.accept(list.get(i)));
	}

	/**
	 * Ensures calling body.run (index) for from <= index < to.
	 * @param from lower index boundary (inclusive)
	 * @param to upper index boundary (exclusive)
	 * @param body body to call
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void parallelFor (final int from, final int to, final IForBody body) throws InterruptedException, ExecutionException {
		final AtomicInteger sharedIndex = new AtomicInteger(from);
		
		runParallel(new Callable<Throwable>() {
			public Throwable call() {
				try {
					while (true) {
						final int index = sharedIndex.getAndIncrement();
						
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
	public void shutdown() {
		try {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot shutdown parallel", ex);
		}
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
