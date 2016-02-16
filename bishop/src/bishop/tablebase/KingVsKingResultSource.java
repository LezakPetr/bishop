package bishop.tablebase;

import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.IPosition;

public class KingVsKingResultSource implements IPositionResultSource {

	@Override
	public int getPositionResult(final IPosition position) {
		final int whiteKingSquare = position.getKingPosition(Color.WHITE);
		final int blackKingSquare = position.getKingPosition(Color.BLACK);
		final int distance = BoardConstants.getKingSquareDistance(whiteKingSquare, blackKingSquare);
		
		if (distance > 1)
			return TableResult.DRAW;
		else
			return TableResult.ILLEGAL;
	}

}
