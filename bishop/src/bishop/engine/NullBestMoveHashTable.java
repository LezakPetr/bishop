package bishop.engine;

import bishop.base.Move;
import bishop.base.Position;

public class NullBestMoveHashTable implements IBestMoveHashTable {
	@Override
	public int getRecord(Position position) {
		return Move.NONE_COMPRESSED_MOVE;
	}

	@Override
	public void updateRecord(final Position position, final int horizon, final int compressedBestMove) {
	}

	@Override
	public void clear() {
	}
}
