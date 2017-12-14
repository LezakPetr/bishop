package math;

public interface IVectorIterator {

	public void next();
	public boolean isValid();
	
	public int getIndex();
	public double getElement();
}
