package bishop.tablebase;

import bishop.base.IMoveWalker;
import bishop.base.Move;
import bishop.base.Position;

public class ReverseTableWalker  implements IMoveWalker {
	
	private long[] indexGroup = new long[TableDefinition.MAX_GROUP_SIZE];
	private Position position;
	private BitArray nextPositionsToCheck;
	private TableDefinition tableDefinition;
	
	public void setPosition (final Position position) {
		this.position = position;
	}
	
	public void setTableDefinition (final TableDefinition definition) {
		this.tableDefinition = definition;
	}
	
	@Override
	public boolean processMove(final Move move) {
		position.undoMove(move);
		
//		final MemoryTable table = bothTables.getBaseSource(onTurn);
//		final int count = table.getDefinition().calculateIndexGroup(position, indexGroup);
		
		final int count = tableDefinition.calculateIndexGroup(position, indexGroup);
		
		for (int i = 0; i < count; i++) {
			final long index = indexGroup[i];
			
			if (index >= 0)
				nextPositionsToCheck.setAt(index, true);
		}
		
		position.makeMove(move);
		
		return true;
	}

	public void setNextPositionsToCheck(final BitArray nextPositionsToCheck) {
		this.nextPositionsToCheck = nextPositionsToCheck;
	}
	
}
