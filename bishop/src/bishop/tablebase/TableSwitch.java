package bishop.tablebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bishop.base.MaterialHash;
import bishop.base.IMaterialHashRead;
import bishop.base.IPosition;
import bishop.base.MirrorPosition;
import collections.ImmutableProbabilisticSet;

public class TableSwitch implements IPositionResultSource {
	
	private final Map<IMaterialHashRead, ITableRead> tableMap;
	private ImmutableProbabilisticSet<IMaterialHashRead> bothColorMaterialSet;
	
	
	public TableSwitch() {
		this.tableMap = new HashMap<>();
	}
	
	@Override
	public int getPositionResult(final IPosition position) {
		final int result = getPositionResultIfPossible(position);
		
		if (result == TableResult.UNKNOWN_MATERIAL)
			throw new RuntimeException("Unknown material " + position.getMaterialHash());
		
		return result;
	}
	
	public int getPositionResultIfPossible(final IPosition position) {
		final IMaterialHashRead directHash = position.getMaterialHash();
		
		if (!canProcessSource(directHash))
			return TableResult.UNKNOWN_MATERIAL;   // Speed up
		
		final IPositionResultSource directTable = tableMap.get(directHash);
		
		if (directTable != null) {
			return directTable.getPositionResult(position);
		}
		
		final IMaterialHashRead oppositeHash = directHash.getOpposite();
		final IPositionResultSource oppositeTable = tableMap.get(oppositeHash);
		
		if (oppositeTable != null) {
			final IPosition oppositePosition = new MirrorPosition(position);
			
			return oppositeTable.getPositionResult(oppositePosition);
		}
		
		return TableResult.UNKNOWN_MATERIAL;
	}
	
	public void setTables (final Map<MaterialHash, ? extends ITableRead> tables) {
		final List<MaterialHash> bothColorHashes = new ArrayList<>();
		tableMap.clear();
		
		for (Map.Entry<MaterialHash, ? extends ITableRead> entry: tables.entrySet()) {
			final MaterialHash materialHash = entry.getKey();
			final MaterialHash copyMaterialHash = materialHash.copy();
			final MaterialHash oppositeMaterialHash = materialHash.getOpposite();
			
			tableMap.put(copyMaterialHash, entry.getValue());
			
			bothColorHashes.add(copyMaterialHash);
			bothColorHashes.add(oppositeMaterialHash);
		}
		
		bothColorMaterialSet = new ImmutableProbabilisticSet<>(bothColorHashes);
	}
	
	public boolean canProcessSource(final IMaterialHashRead materialHash) {
		return bothColorMaterialSet.contains(materialHash);
	}
	
	public Set<IMaterialHashRead> getMaterialHashSet() {
		return Collections.unmodifiableSet(tableMap.keySet());
	}

	public ITableRead getTable(final MaterialHash materialHash) {
		return tableMap.get(materialHash);
	}
}
