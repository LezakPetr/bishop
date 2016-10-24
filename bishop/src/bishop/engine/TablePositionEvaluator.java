package bishop.engine;

import java.io.PrintWriter;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public final class TablePositionEvaluator {
	
	private static final long[] PAWN_MASKS = {
		BoardConstants.PAWN_ALLOWED_SQUARES & (BoardConstants.FILE_C_MASK | BoardConstants.FILE_D_MASK | BoardConstants.FILE_E_MASK | BoardConstants.FILE_F_MASK),
		BoardConstants.PAWN_ALLOWED_SQUARES & (BoardConstants.FILE_D_MASK | BoardConstants.FILE_E_MASK),
		BitBoard.EMPTY
	};
	
	private final TablePositionEvaluatorSettings settings;
	
	public TablePositionEvaluator(final TablePositionEvaluatorSettings settings) {
		this.settings = settings;
	}

	public int evaluatePosition (final Position position) {
		int tableEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final long board = position.getPiecesMask(color, pieceType);
				
				for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
					final int square = loop.getNextSquare();
					
					tableEvaluation += settings.getPieceEvaluationTable().getEvaluation(color, pieceType, square);
				}
			}
		}

		return tableEvaluation;
	}
	
	public void writeLog (final PrintWriter writer) {
	}

}
