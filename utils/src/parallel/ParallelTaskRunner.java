package parallel;


public class ParallelTaskRunner implements ITaskRunner {

	private Thread thread;
	private final Object monitor = new Object();
	
	private volatile boolean finish;
	private volatile Runnable task;
	private volatile boolean taskRunning;
	
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			while (!finish) {
				Runnable currentTask;
				
				// Wait for task
				do {
					if (finish)
						return;
					
					currentTask = task;
				} while (currentTask == null);
				
				task = null;
				
				// Run
				currentTask.run();
				taskRunning = false;
			}
		}
	};
	
	public void startTask(final Runnable task) {
		assert !this.taskRunning;
		assert this.task == null;
		
		this.taskRunning = true;
		this.task = task;
	}
	
	public void joinTask() {
		// Wait for finish
		while (taskRunning)
			;
		
		assert task == null;
	}
	
	public void start() {
		synchronized (monitor) {
			finish = false;
			
			thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.start();
		} 
	}
	
	public void stop() {
		synchronized (monitor) {
			finish = true;
			
			try {
				thread.join();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} 
	}

}
