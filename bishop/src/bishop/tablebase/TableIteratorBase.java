package bishop.tablebase;

import bishop.base.IPosition;
import bishop.base.Position;

public abstract class TableIteratorBase implements ITableIteratorRead {
	
	private final TableDefinition tableDefinition;
	private final int[] combinationIndices;
	private int chunkIndex;
	private Chunk chunk;
	private long tableIndex;
	
	public TableIteratorBase(final TableDefinition tableDefinition, final long beginIndex) {
		this.tableDefinition = tableDefinition;
		
		final int definitionCount = tableDefinition.getCombinationDefinitionCount();
		this.combinationIndices = new int[definitionCount];
		
		updateCachedData();
		moveForward(beginIndex);
	}
	
	public TableIteratorBase(final TableIteratorBase orig) {
		this.tableDefinition = orig.tableDefinition;
		this.chunkIndex = orig.chunkIndex;
		this.chunk = orig.chunk;
		this.tableIndex = orig.tableIndex;
		
		final int combinationCount = orig.combinationIndices.length;
		this.combinationIndices = new int[combinationCount];

		System.arraycopy(orig.combinationIndices, 0, this.combinationIndices, 0, combinationCount);
	}
	
	@Override
	public boolean isValid() {
		return isValidChunk();
	}

	private boolean isValidChunk() {
		return chunkIndex < tableDefinition.getChunkCount();
	}
	
	@Override
	public void next() {
		int i = combinationIndices.length - 1;
				
		while (i >= 0) {
			final SquareCombination combination = chunk.getSquareCombinationAt(i);
			combinationIndices[i]++;
			
			if (combinationIndices[i] < combination.getCount())
				break;
			
			combinationIndices[i] = 0;
			i--;
		}
		
		if (i < 0) {
			chunkIndex++;
		}

		updateCachedData();
	}

	@Override
	public void moveForward(final long count) {
		tableIndex += count;
		chunkIndex = tableDefinition.getChunkAtTableIndex (tableIndex);
		
		if (isValid()) {
			chunk = tableDefinition.getChunkAt(chunkIndex);
			long remaining = tableIndex - chunk.getBeginIndex();
			
			if (remaining < 0 || remaining >= (chunk.getEndIndex() - chunk.getBeginIndex()))
				throw new RuntimeException("Wrong remaining count");
			
			for (int i = 0; i < combinationIndices.length; i++) {
				final SquareCombination combination = chunk.getSquareCombinationAt(i);
				final long multiplicator = chunk.getMultiplicatorAt(i);
				final long combinationIndex = remaining / multiplicator;
				
				if (combinationIndex >= combination.getCount()) {
					chunkIndex = tableDefinition.getChunkCount();
					break;
				}
				
				combinationIndices[i] = (int) combinationIndex;
				remaining -= combinationIndex * multiplicator;
			}
		}
		
		updateCachedData();
	}
	
	@Override
	public void fillPosition(final Position position) {
		position.clearPosition();
		chunk.fillFixedDataToPosition(position);
		
		for (int i = 0; i < combinationIndices.length; i++) {
			final SquareCombination combination = chunk.getSquareCombinationAt(i);
			
			combination.setToPosition (position, combinationIndices[i]);
		}
		
		position.refreshCachedData();
	}
	
	private void updateCachedData() {
		if (isValidChunk()) {
			chunk = tableDefinition.getChunkAt(chunkIndex);
			tableIndex = tableDefinition.calculateTableIndex(chunkIndex, combinationIndices);
		}
		else {
			chunk = null;
			tableIndex = -1;
		}
	}

	@Override
	public long getTableIndex() {
		return tableIndex;
	}

	@Override
	public int getChunkIndex() {
		return chunkIndex;
	}

	@Override
	public long getPositionCount() {
		return tableDefinition.getTableIndexCount();
	}

}
