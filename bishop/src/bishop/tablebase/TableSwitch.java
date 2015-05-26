package bishop.tablebase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import bishop.base.MaterialHash;
import bishop.base.Position;

public class TableSwitch implements IPositionResultSource {
	
	private final HashMap<MaterialHash, ITableRead> tableMap;
	
	
	public TableSwitch() {
		this.tableMap = new HashMap<MaterialHash, ITableRead>();		
	}
	
	@Override
	public int getPositionResult(final Position position) {
		final MaterialHash directHash = position.getMaterialHash();
		final IPositionResultSource directTable = tableMap.get(directHash);
		
		if (directTable != null) {
			return directTable.getPositionResult(position);
		}
		
		final MaterialHash oppositeHash = directHash.copy();
		oppositeHash.changeToOpposite();
		
		final IPositionResultSource oppositeTable = tableMap.get(oppositeHash);
		
		if (oppositeTable != null) {
			final Position oppositePosition = new Position();
			oppositePosition.assignMirror (position);
			
			return oppositeTable.getPositionResult(oppositePosition);
		}
		
		throw new RuntimeException("Unknown material");
	}
	
	public void addTable (final MaterialHash materialHash, final ITableRead table) {
		tableMap.put(materialHash.copy(), table);
	}
	
	public void removeSource (final MaterialHash materialHash) {
		tableMap.remove(materialHash);
	}

	public boolean canProcessSource(final MaterialHash materialHash) {
		final MaterialHash oppositeHash = materialHash.copy();
		oppositeHash.changeToOpposite();

		return tableMap.containsKey(materialHash) || tableMap.containsKey(oppositeHash);
	}
	
	public Set<MaterialHash> getMaterialHashSet() {
		return Collections.unmodifiableSet(tableMap.keySet());
	}

	public ITableRead getTable(final MaterialHash materialHash) {
		return tableMap.get(materialHash);
	}
}
