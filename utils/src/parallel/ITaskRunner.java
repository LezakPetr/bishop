package parallel;


public interface ITaskRunner {
	public void startTask(final Runnable task);
	
	public void joinTask();
}
