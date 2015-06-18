package bishop.tablebase;

import java.util.Arrays;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Position;

public class PreviousSymbolProbabilityModelSelector implements IProbabilityModelSelector {

	private final int symbolCount;
	private final int[] bishopMultiplicator;
	private final int[] previousSymbols;
	
	public PreviousSymbolProbabilityModelSelector(final int symbolCount, final MaterialHash materialHash) {
		this.symbolCount = symbolCount;
		
		this.bishopMultiplicator = new int[Color.LAST];
		
		int positionIndexCount = 1;
		
		for (int color = 0; color < Color.LAST; color++) {
			bishopMultiplicator[color] = materialHash.getPieceCount(color, PieceType.BISHOP) + 1;
			positionIndexCount *= bishopMultiplicator[color];
		}
		
		this.previousSymbols = new int[positionIndexCount];
		
		resetSymbols();
	}
	
	private int getPositionIndex(final Position position) {
		int index = 0;
		
		for (int color = Color.WHITE; color < Color.BLACK; color++) {
			final int multiplicator = bishopMultiplicator[color];
			
			if (multiplicator > 1) {
				final long bishopMask = position.getPiecesMask(color, PieceType.BISHOP);
				final int whiteSquaredBishopCount = BitBoard.getSquareCount(bishopMask & BoardConstants.WHITE_SQUARE_MASK);
				
				index = index * multiplicator + whiteSquaredBishopCount;
			}
		}
		
		return index;
	}
	
	@Override
	public int getModelIndex(final Position position) {
		final int positionIndex = getPositionIndex(position);
		final int previousSymbol = previousSymbols[positionIndex];
		
		return previousSymbol * previousSymbols.length + positionIndex; 
	}
	
	@Override
	public void addSymbol (final Position position, final int symbol) {
		final int positionIndex = getPositionIndex(position);
		previousSymbols[positionIndex] = symbol;
	}
	
	@Override
	public void resetSymbols() {
		Arrays.fill(previousSymbols, symbolCount);
	}
	
	@Override
	public int getModelCount() {
		return previousSymbols.length * (symbolCount + 1);
	}
}
