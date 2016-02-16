package bishop.tablebase;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bishop.base.MaterialHash;
import bishop.base.IPosition;
import bishop.base.MirrorPosition;

public class TableSwitch implements IPositionResultSource {
	
	private final Map<MaterialHash, ITableRead> tableMap;
	private final Set<MaterialHash> bothColorMaterialSet;
	
	
	public TableSwitch() {
		this.tableMap = new HashMap<>();
		this.bothColorMaterialSet = new HashSet<>();
	}
	
	@Override
	public int getPositionResult(final IPosition position) {
		final MaterialHash directHash = position.getMaterialHash();
		final IPositionResultSource directTable = tableMap.get(directHash);
		
		if (directTable != null) {
			return directTable.getPositionResult(position);
		}
		
		final MaterialHash oppositeHash = directHash.getOpposite();
		final IPositionResultSource oppositeTable = tableMap.get(oppositeHash);
		
		if (oppositeTable != null) {
			final IPosition oppositePosition = new MirrorPosition(position);
			
			return oppositeTable.getPositionResult(oppositePosition);
		}
		
		throw new RuntimeException("Unknown material");
	}
	
	public void addTable (final MaterialHash materialHash, final ITableRead table) {
		final MaterialHash copyMaterialHash = materialHash.copy();
		final MaterialHash oppositeMaterialHash = materialHash.getOpposite();
		
		tableMap.put(copyMaterialHash, table);
		bothColorMaterialSet.add(copyMaterialHash);
		bothColorMaterialSet.add(oppositeMaterialHash);
	}
	
	public void removeSource (final MaterialHash materialHash) {
		final MaterialHash oppositeMaterialHash = materialHash.getOpposite();
		
		if (tableMap.remove(materialHash) != null && !tableMap.containsKey(oppositeMaterialHash)) {
			bothColorMaterialSet.remove(materialHash);
			bothColorMaterialSet.remove(oppositeMaterialHash);
		}
	}

	public boolean canProcessSource(final MaterialHash materialHash) {
		return bothColorMaterialSet.contains(materialHash);
	}
	
	public Set<MaterialHash> getMaterialHashSet() {
		return Collections.unmodifiableSet(tableMap.keySet());
	}

	public ITableRead getTable(final MaterialHash materialHash) {
		return tableMap.get(materialHash);
	}
}
