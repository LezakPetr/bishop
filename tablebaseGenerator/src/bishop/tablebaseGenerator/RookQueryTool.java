package bishop.tablebaseGenerator;

import java.io.File;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.Piece;
import bishop.base.Position;
import bishop.base.Rank;
import bishop.base.Square;
import bishop.tablebase.FileNameCalculator;
import bishop.tablebase.FilePositionResultSource;
import bishop.tablebase.TableBlockCache;
import bishop.tablebase.TableResult;

public class RookQueryTool {

	private static final long PAWN_MASK = ~BoardConstants.RANK_18_MASK & ~BoardConstants.FILE_A_MASK & ~BoardConstants.FILE_B_MASK;
	private static final int CACHE_SIZE = 20000;
	
	public static void main(final String[] args) {
		final String directory = args[0];
		final int onTurn = Color.WHITE;
		final MaterialHash materialHash = new MaterialHash("01001-01000", onTurn);
		final TableBlockCache blockCache = new TableBlockCache(CACHE_SIZE);
		final File file = new File(FileNameCalculator.getAbsolutePath(directory, materialHash));
		final FilePositionResultSource resultSource = new FilePositionResultSource(file, blockCache);
		
		final Position position = new Position();
		
		for (BitLoop pawnLoop = new BitLoop(PAWN_MASK); pawnLoop.hasNextSquare(); ) {
			final int pawnSquare = pawnLoop.getNextSquare();
			final int pawnFile = Square.getFile(pawnSquare);
			
			for (int whiteRookFile = bishop.base.File.FB; whiteRookFile < pawnFile; whiteRookFile++) {
				double minPercent = 100.0;
				
				for (int blackKingRank = Rank.R3; blackKingRank < Rank.R8; blackKingRank++) {
					int validCount = 0;
					int winCount = 0;
					
					for (int blackRookSquare = Square.FIRST; blackRookSquare < Square.LAST; blackRookSquare++) {
						position.clearPosition();
						position.setOnTurn(onTurn);
						position.setSquareContent(pawnSquare, Piece.WHITE_PAWN);
						position.setSquareContent(pawnSquare + 8, Piece.WHITE_KING);
						position.setSquareContent(Square.onFileRank(whiteRookFile, Rank.R1), Piece.WHITE_ROOK);
						position.setSquareContent(blackRookSquare, Piece.BLACK_ROOK);
						position.setSquareContent(Square.onFileRank(whiteRookFile - 1, blackKingRank), Piece.BLACK_KING);
						position.refreshCachedData();
						
						if (BitBoard.getSquareCount(position.getOccupancy()) == 5) {
							final int result = resultSource.getPositionResult(position);
							
							if (result != TableResult.ILLEGAL) {
								validCount++;
								
								if (TableResult.isWin(result))
									winCount++;
							}
						}
					}
					
					final double percent = 100.0 * winCount / validCount;
					minPercent = Math.min(minPercent, percent);
				}
				
				System.out.println (Square.toString(pawnSquare) + " " + bishop.base.File.toChar(whiteRookFile) + " " + Math.round(minPercent) + "%");
			}
			
			System.out.println();
		}

	}
}
