package math;

public interface IMatrixRowIterator {
	public void next();
	public boolean isValid();

	public int getRowIndex();
	public IVectorRead getRow();
}
