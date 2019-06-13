package bishop.base;

import java.util.Arrays;


public class MoveStack {
	
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
	
	public void copyRecords (final MoveStack source, final int srcIndex, final int dstIndex, final int size) {
		System.arraycopy(source.stack, srcIndex, this.stack, dstIndex, size);
	}

	public int findBestMove(final int begin, final int end) {
		// This is a little hack. Because evaluation is stored in MSB bytes,
		// it drives order.
		int bestIndex = -1;
		long bestMove = Long.MIN_VALUE;

		for (int i = begin; i < end; i++) {
			final long move = stack[i];

			if (move >= bestMove) {
				bestIndex = i;
				bestMove = move;
			}
		}

		return bestIndex;
	}

	public void swapMoves (final int index1, final int index2) {
		final long tmp = stack[index1];
		stack[index1] = stack[index2];
		stack[index2] = tmp;
	}

	public void sortMoves(final int begin, final int end) {
		// This is a little hack. Because evaluation is stored in MSB bytes,
		// it drives order. So worst moves will be in the begin, best in the end,
		Arrays.sort(stack, begin, end);
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		final Move move = new Move();
		
		for (int i = 0; i < stack.length; i++) {
			if (i > 0)
				builder.append(' ');
			
			getMove(i, move);
			builder.append(move.toString());
		}
		
		return builder.toString();
	}

}
