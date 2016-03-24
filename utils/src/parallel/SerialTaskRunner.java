package parallel;

public class SerialTaskRunner implements ITaskRunner {
	private Runnable task;
	
	public void startTask(final Runnable task) {
		assert this.task == null;
		
		this.task = task;
	}
	
	public void joinTask() {
		task.run();
		task = null;
	}

}
