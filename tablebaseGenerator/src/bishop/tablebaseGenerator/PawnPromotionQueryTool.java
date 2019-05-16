package bishop.tablebaseGenerator;

import bishop.base.*;
import bishop.engine.PawnEndingKey;
import bishop.engine.PawnPromotionEstimator;
import bishop.tablebase.*;
import bishop.tables.PawnAttackTable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.SplittableRandom;

public class PawnPromotionQueryTool {

	private static final int SAMPLE_COUNT = 100000;
	private static final int MAX_PAWN_COUNT = 2;

	public static void main(final String[] args) throws FileNotFoundException {
		final String directory = args[0];
		final TableSwitch resultSource = new TableSwitch(new File (directory));

		final SplittableRandom rng = new SplittableRandom(12345);
		final Position position = new Position();

		try (PrintWriter csv = new PrintWriter(args[1])) {
			csv.println(PawnPromotionEstimator.FEATURES_HEADER + ",isWin,isLose,position");

			for (int i = 0; i < SAMPLE_COUNT; i++) {
				final int pawnCount = rng.nextInt(MAX_PAWN_COUNT + 1);

				position.clearPosition();
				position.setOnTurn(Color.BLACK);

				for (int j = 0; j < pawnCount; j++) {
					position.setSquareContent(
							rng.nextInt(Square.FIRST_PAWN_SQUARE, Square.LAST_PAWN_SQUARE),
							(rng.nextBoolean()) ? Piece.WHITE_PAWN : Piece.BLACK_PAWN
					);
				}

				position.setSquareContent(rng.nextInt(Square.FIRST, Square.LAST), Piece.WHITE_KING);
				position.setSquareContent(rng.nextInt(Square.FIRST, Square.LAST), Piece.BLACK_KING);
				position.setSquareContent(rng.nextInt(Square.A8, Square.LAST), Piece.WHITE_QUEEN);
				position.refreshCachedData();

				final boolean checkByPawn = ((position.getPiecesMask(Color.WHITE, PieceType.PAWN) & PawnAttackTable.getItem(Color.BLACK, position.getKingPosition(Color.BLACK))) != 0);

				if (BitBoard.getSquareCount(position.getOccupancy()) == 3 + pawnCount && !checkByPawn) {
					final int result = resultSource.getPositionResult(position);

					if (result != TableResult.ILLEGAL) {
						final PawnPromotionEstimator estimator = new PawnPromotionEstimator();
						estimator.init(
								new PawnEndingKey(
										getPawnAndQueenMask(position, Color.WHITE),
										getPawnAndQueenMask(position, Color.BLACK)
								),
								position.getKingPosition(Color.WHITE),
								position.getKingPosition(Color.BLACK)
						);

						csv.print(estimator.getFeatures());

						if (TableResult.isWin(result))
							csv.print(",0,1");
						else if (TableResult.isLose(result))
							csv.print(",1,0");
						else
							csv.print(",0,0");

						csv.print(",");
						csv.println(position);
					}
				}
			}
		}
	}

	private static long getPawnAndQueenMask(Position position, int white) {
		return position.getPiecesMask(white, PieceType.PAWN) | position.getPiecesMask(white, PieceType.QUEEN);
	}

}
