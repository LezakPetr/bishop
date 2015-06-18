package bishop.tablebase;

import bishop.base.Position;

public interface IProbabilityModelSelector {

	public int getModelCount();

	public int getModelIndex(final Position position);

	public void addSymbol(final Position position, final int symbol);

	public void resetSymbols();
}
