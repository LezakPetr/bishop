package bishop.base;

import java.util.Arrays;


public final class  MoveStack {
	
	private static final long MOVE_MASK = 0x00000000FFFFFFFFL;
	
	@SuppressWarnings("unused")
	private static final long EVALUATION_MASK = 0xFFFFFFFF00000000L;
	
	private static final long EVALUATION_SHIFT = 32;
	
	private final long[] stack;
	
	public MoveStack (final int size) {
		stack = new long[size];
	}
	
	public void getMove (final int index, final Move move) {
		move.setData((int) (stack[index] & MOVE_MASK));
	}

	public int getEvaluation (final int index) {
		return (int) (stack[index] >>> EVALUATION_SHIFT);
	}

	public void setRecord (final int index, final Move move, final int evaluation) {
		final long movePart = ((long) move.getData()) & MOVE_MASK;
		final long evaluationPart = (long) evaluation << EVALUATION_SHIFT;
		
		stack[index] = movePart | evaluationPart;
	}

	public void sortMoves(final int begin, final int end) {
		Arrays.sort(stack, begin, end);
	}

}
