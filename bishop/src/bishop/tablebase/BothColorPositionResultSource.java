package bishop.tablebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bishop.base.Color;
import bishop.base.IPosition;

public class BothColorPositionResultSource<T extends IPositionResultSource> implements IPositionResultSource {
	private final List<T> baseSources;
	
	public BothColorPositionResultSource () {
		this.baseSources = new ArrayList<>(Color.LAST);
		this.baseSources.addAll(Collections.nCopies(Color.LAST, null));
	}
	
	public T getBaseSource (final int onTurn) {
		return baseSources.get(onTurn);
	}
	
	public void setBaseSource (final int onTurn, final T source) {
		baseSources.set(onTurn, source);
	}

	@Override
	public int getPositionResult(final IPosition position) {
		final int onTurn = position.getOnTurn();
		final IPositionResultSource baseSource = getBaseSource(onTurn);
		
		return baseSource.getPositionResult(position);
	}

	public void clear() {
		for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
			setBaseSource(onTurn, null);
		}
	}

	public boolean hasOnTurn(final int onTurn) {
		return getBaseSource(onTurn) != null;
	}

}
