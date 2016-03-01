package bishop.engine;

import bishop.base.Move;
import bishop.base.MoveStack;


public class EvaluatedMoveList extends MoveStack {
	
	private int size;
	
	public EvaluatedMoveList (final int size) {
		super (size);
	}
	
	public int getSize() {
		return size;
	}
	
	public void addRecord (final Move move, final int evaluation) {
		setRecord(size, move, evaluation);
		size++;
	}

	public void assign(final EvaluatedMoveList orig) {
		this.size = orig.size;
		this.copyRecords(orig, 0, 0, orig.size);
	}

	public void clear() {
		size = 0;
	}
	
	@Override
	public String toString() {
		return size + ": " + super.toString();
	}
}
