package bishopTests;

import bishop.base.*;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import org.junit.Assert;
import org.junit.Test;

import java.util.SplittableRandom;

public class BoardConstantsTest {
	@Test
	public void getSquareColorMaskTest() {
		Assert.assertEquals(BoardConstants.WHITE_SQUARE_MASK, BoardConstants.getSquareColorMask(Color.WHITE));
		Assert.assertEquals(BoardConstants.BLACK_SQUARE_MASK, BoardConstants.getSquareColorMask(Color.BLACK));
	}

	@Test
	public void getRankMaskTest() {
		Assert.assertEquals(BoardConstants.RANK_1_MASK, BoardConstants.getRankMask(Rank.R1));
		Assert.assertEquals(BoardConstants.RANK_2_MASK, BoardConstants.getRankMask(Rank.R2));
		Assert.assertEquals(BoardConstants.RANK_3_MASK, BoardConstants.getRankMask(Rank.R3));
		Assert.assertEquals(BoardConstants.RANK_4_MASK, BoardConstants.getRankMask(Rank.R4));
		Assert.assertEquals(BoardConstants.RANK_5_MASK, BoardConstants.getRankMask(Rank.R5));
		Assert.assertEquals(BoardConstants.RANK_6_MASK, BoardConstants.getRankMask(Rank.R6));
		Assert.assertEquals(BoardConstants.RANK_7_MASK, BoardConstants.getRankMask(Rank.R7));
		Assert.assertEquals(BoardConstants.RANK_8_MASK, BoardConstants.getRankMask(Rank.R8));
	}

	@Test
	public void getFileMaskTest() {
		Assert.assertEquals(BoardConstants.FILE_A_MASK, BoardConstants.getFileMask(File.FA));
		Assert.assertEquals(BoardConstants.FILE_B_MASK, BoardConstants.getFileMask(File.FB));
		Assert.assertEquals(BoardConstants.FILE_C_MASK, BoardConstants.getFileMask(File.FC));
		Assert.assertEquals(BoardConstants.FILE_D_MASK, BoardConstants.getFileMask(File.FD));
		Assert.assertEquals(BoardConstants.FILE_E_MASK, BoardConstants.getFileMask(File.FE));
		Assert.assertEquals(BoardConstants.FILE_F_MASK, BoardConstants.getFileMask(File.FF));
		Assert.assertEquals(BoardConstants.FILE_G_MASK, BoardConstants.getFileMask(File.FG));
		Assert.assertEquals(BoardConstants.FILE_H_MASK, BoardConstants.getFileMask(File.FH));
	}

	@Test
	public void getEpRankTest() {
		Assert.assertEquals(Rank.R4, BoardConstants.getEpRank(Color.WHITE));
		Assert.assertEquals(Rank.R5, BoardConstants.getEpRank(Color.BLACK));
	}

	@Test
	public void getEpSquareTest() {
		for (int file = File.FIRST; file < File.LAST; file++) {
			Assert.assertEquals(Square.onFileRank(file, Rank.R4), BoardConstants.getEpSquare(Color.WHITE, file));
			Assert.assertEquals(Square.onFileRank(file, Rank.R5), BoardConstants.getEpSquare(Color.BLACK, file));
		}
	}

	@Test
	public void getEpTargetSquareTest() {
		for (int file = File.FIRST; file < File.LAST; file++) {
			Assert.assertEquals(Square.onFileRank(file, Rank.R3), BoardConstants.getEpTargetSquare(Color.WHITE, file));
			Assert.assertEquals(Square.onFileRank(file, Rank.R6), BoardConstants.getEpTargetSquare(Color.BLACK, file));
		}
	}

	@Test
	public void getEpRankMaskTest() {
		Assert.assertEquals(BoardConstants.RANK_4_MASK, BoardConstants.getEpRankMask(Color.WHITE));
		Assert.assertEquals(BoardConstants.RANK_5_MASK, BoardConstants.getEpRankMask(Color.BLACK));
	}

	@Test
	public void getPawnInitialSquare() {
		Assert.assertEquals(Square.A2, BoardConstants.getPawnInitialSquare(Color.WHITE, File.FA));
		Assert.assertEquals(Square.B2, BoardConstants.getPawnInitialSquare(Color.WHITE, File.FB));
		Assert.assertEquals(Square.C2, BoardConstants.getPawnInitialSquare(Color.WHITE, File.FC));
		Assert.assertEquals(Square.D2, BoardConstants.getPawnInitialSquare(Color.WHITE, File.FD));
		Assert.assertEquals(Square.E2, BoardConstants.getPawnInitialSquare(Color.WHITE, File.FE));
		Assert.assertEquals(Square.F2, BoardConstants.getPawnInitialSquare(Color.WHITE, File.FF));
		Assert.assertEquals(Square.G2, BoardConstants.getPawnInitialSquare(Color.WHITE, File.FG));
		Assert.assertEquals(Square.H2, BoardConstants.getPawnInitialSquare(Color.WHITE, File.FH));

		Assert.assertEquals(Square.A7, BoardConstants.getPawnInitialSquare(Color.BLACK, File.FA));
		Assert.assertEquals(Square.B7, BoardConstants.getPawnInitialSquare(Color.BLACK, File.FB));
		Assert.assertEquals(Square.C7, BoardConstants.getPawnInitialSquare(Color.BLACK, File.FC));
		Assert.assertEquals(Square.D7, BoardConstants.getPawnInitialSquare(Color.BLACK, File.FD));
		Assert.assertEquals(Square.E7, BoardConstants.getPawnInitialSquare(Color.BLACK, File.FE));
		Assert.assertEquals(Square.F7, BoardConstants.getPawnInitialSquare(Color.BLACK, File.FF));
		Assert.assertEquals(Square.G7, BoardConstants.getPawnInitialSquare(Color.BLACK, File.FG));
		Assert.assertEquals(Square.H7, BoardConstants.getPawnInitialSquare(Color.BLACK, File.FH));
	}

	@Test
	public void getKingSquareDistanceTest() {
		for (int rank1 = Rank.FIRST; rank1 < Rank.LAST; rank1++) {
			for (int file1 = File.FIRST; file1 < File.LAST; file1++) {
				for (int rank2 = Rank.FIRST; rank2 < Rank.LAST; rank2++) {
					for (int file2 = File.FIRST; file2 < File.LAST; file2++) {
						final int dFile = Math.abs(file2 - file1);
						final int dRank = Math.abs(rank2 - rank1);
						final int distance = Math.max(dFile, dRank);

						Assert.assertEquals(
								distance,
								BoardConstants.getKingSquareDistance(
										Square.onFileRank(file1, rank1),
										Square.onFileRank(file2, rank2)
								)
						);
					}
				}
			}
		}
	}

	@Test
	public void getPawnPromotionRank() {
		Assert.assertEquals(Rank.R8, BoardConstants.getPawnPromotionRank(Color.WHITE));
		Assert.assertEquals(Rank.R1, BoardConstants.getPawnPromotionRank(Color.BLACK));
	}

	@Test
	public void getPawnPromotionSquare() {
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int file = Square.getFile(square);
			final int rank = Square.getRank(square);

			Assert.assertEquals(
					Square.onFileRank(file, Rank.R8),
					BoardConstants.getPawnPromotionSquare(
							Color.WHITE,
							Square.onFileRank(file, rank)
					)
			);

			Assert.assertEquals(
					Square.onFileRank(file, Rank.R1),
					BoardConstants.getPawnPromotionSquare(
							Color.BLACK,
							Square.onFileRank(file, rank)
					)
			);
		}
	}

	@Test
	public void getPawnPromotionDistanceTest() {
		for (int file = File.FIRST; file < File.LAST; file++) {
			Assert.assertEquals(0, BoardConstants.getPawnPromotionDistance(Color.WHITE, Square.onFileRank(file, Rank.R8)));
			Assert.assertEquals(1, BoardConstants.getPawnPromotionDistance(Color.WHITE, Square.onFileRank(file, Rank.R7)));
			Assert.assertEquals(2, BoardConstants.getPawnPromotionDistance(Color.WHITE, Square.onFileRank(file, Rank.R6)));
			Assert.assertEquals(3, BoardConstants.getPawnPromotionDistance(Color.WHITE, Square.onFileRank(file, Rank.R5)));
			Assert.assertEquals(4, BoardConstants.getPawnPromotionDistance(Color.WHITE, Square.onFileRank(file, Rank.R4)));
			Assert.assertEquals(5, BoardConstants.getPawnPromotionDistance(Color.WHITE, Square.onFileRank(file, Rank.R3)));
			Assert.assertEquals(5, BoardConstants.getPawnPromotionDistance(Color.WHITE, Square.onFileRank(file, Rank.R2)));

			Assert.assertEquals(0, BoardConstants.getPawnPromotionDistance(Color.BLACK, Square.onFileRank(file, Rank.R1)));
			Assert.assertEquals(1, BoardConstants.getPawnPromotionDistance(Color.BLACK, Square.onFileRank(file, Rank.R2)));
			Assert.assertEquals(2, BoardConstants.getPawnPromotionDistance(Color.BLACK, Square.onFileRank(file, Rank.R3)));
			Assert.assertEquals(3, BoardConstants.getPawnPromotionDistance(Color.BLACK, Square.onFileRank(file, Rank.R4)));
			Assert.assertEquals(4, BoardConstants.getPawnPromotionDistance(Color.BLACK, Square.onFileRank(file, Rank.R5)));
			Assert.assertEquals(5, BoardConstants.getPawnPromotionDistance(Color.BLACK, Square.onFileRank(file, Rank.R6)));
			Assert.assertEquals(5, BoardConstants.getPawnPromotionDistance(Color.BLACK, Square.onFileRank(file, Rank.R7)));
		}
	}

	@Test
	public void getPawnRankOffsetTest() {
		Assert.assertEquals(+1, BoardConstants.getPawnRankOffset(Color.WHITE));
		Assert.assertEquals(-1, BoardConstants.getPawnRankOffset(Color.BLACK));
	}

	private void testGetFrontSquaresOnThreeFiles(final int color, final int square, final String expectedBoard) {
		Assert.assertEquals(
				BitBoard.fromString(expectedBoard),
				BoardConstants.getFrontSquaresOnThreeFiles(color, square)
		);
	}

	@Test
	public void getFrontSquaresOnThreeFilesTest() {
		testGetFrontSquaresOnThreeFiles(Color.WHITE, Square.A4, "a5, a6, a7, a8, b5, b6, b7, b8");
		testGetFrontSquaresOnThreeFiles(Color.WHITE, Square.H5, "g6, g7, g8, h6, h7, h8");
		testGetFrontSquaresOnThreeFiles(Color.WHITE, Square.C6, "b7, b8, c7, c8, d7, d8");
		testGetFrontSquaresOnThreeFiles(Color.BLACK, Square.A4, "a3, a2, a1, b3, b2, b1");
		testGetFrontSquaresOnThreeFiles(Color.BLACK, Square.H5, "g4, g3, g2, g1, h4, h3, h2, h1");
		testGetFrontSquaresOnThreeFiles(Color.BLACK, Square.E2, "d1, e1, f1");
	}

	private void testGetFrontSquaresOnNeighborFiles(final int color, final int square, final String expectedBoard) {
		Assert.assertEquals(
				BitBoard.fromString(expectedBoard),
				BoardConstants.getFrontSquaresOnNeighborFiles(color, square)
		);
	}

	@Test
	public void getFrontSquaresOnNeighborFilesTest() {
		testGetFrontSquaresOnNeighborFiles(Color.WHITE, Square.A4, "b5, b6, b7, b8");
		testGetFrontSquaresOnNeighborFiles(Color.WHITE, Square.H5, "g6, g7, g8");
		testGetFrontSquaresOnNeighborFiles(Color.WHITE, Square.C6, "b7, b8, d7, d8");
		testGetFrontSquaresOnNeighborFiles(Color.BLACK, Square.A4, "b3, b2, b1");
		testGetFrontSquaresOnNeighborFiles(Color.BLACK, Square.H5, "g4, g3, g2, g1");
		testGetFrontSquaresOnNeighborFiles(Color.BLACK, Square.E2, "d1, f1");
	}

	private void testGetPawnBlockingSquares(final int color, final int square, final String expectedBoard) {
		Assert.assertEquals(
				BitBoard.fromString(expectedBoard),
				BoardConstants.getPawnBlockingSquares(color, square)
		);
	}

	@Test
	public void getPawnBlockingSquaresTest() {
		testGetPawnBlockingSquares(Color.WHITE, Square.A4, "a5, a6, a7, a8, b6, b7, b8");
		testGetPawnBlockingSquares(Color.WHITE, Square.H5, "g7, g8, h6, h7, h8");
		testGetPawnBlockingSquares(Color.WHITE, Square.C6, "b8, c7, c8, d8");
		testGetPawnBlockingSquares(Color.BLACK, Square.A4, "a3, a2, a1, b2, b1");
		testGetPawnBlockingSquares(Color.BLACK, Square.H5, "g3, g2, g1, h4, h3, h2, h1");
		testGetPawnBlockingSquares(Color.BLACK, Square.E2, "e1");
	}

	private void testGetSquaresInFrontInclusive(final int color, final int square, final String expectedBoard) {
		Assert.assertEquals(
				BitBoard.fromString(expectedBoard),
				BoardConstants.getSquaresInFrontInclusive(color, square)
		);
	}

	@Test
	public void getSquaresInFrontInclusiveTest() {
		testGetSquaresInFrontInclusive(Color.WHITE, Square.A4, "a4, a5, a6, a7, a8");
		testGetSquaresInFrontInclusive(Color.WHITE, Square.H5, "h5, h6, h7, h8");
		testGetSquaresInFrontInclusive(Color.WHITE, Square.C6, "c6, c7, c8");
		testGetSquaresInFrontInclusive(Color.BLACK, Square.A4, "a4, a3, a2, a1");
		testGetSquaresInFrontInclusive(Color.BLACK, Square.H5, "h5, h4, h3, h2, h1");
		testGetSquaresInFrontInclusive(Color.BLACK, Square.E2, "e2, e1");
	}

	@Test
	public void getConnectedPawnSquareMaskTest() {
		for (int file = File.FIRST; file < File.LAST; file++) {
			for (int rank = Rank.FIRST; rank < Rank.LAST; rank++) {
				final int square = Square.onFileRank(file, rank);

				long expectedMask = BitBoard.EMPTY;

				if (file > File.FA)
					expectedMask |= BitBoard.of(Square.onFileRank(file - 1, rank));

				if (file < File.FH)
					expectedMask |= BitBoard.of(Square.onFileRank(file + 1, rank));

				Assert.assertEquals(expectedMask, BoardConstants.getConnectedPawnSquareMask(square));
			}
		}
	}

	@Test
	public void getAllConnectedPawnSquareMaskTest() {
		final SplittableRandom rng = new SplittableRandom();

		for (int i = 0; i < 100000; i++) {
			long sourceMask = BitBoard.EMPTY;
			long expectedMask = BitBoard.EMPTY;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				if (rng.nextBoolean()) {
					sourceMask |= BitBoard.of(square);
					expectedMask |= BoardConstants.getConnectedPawnSquareMask(square);
				}
			}

			Assert.assertEquals(expectedMask, BoardConstants.getAllConnectedPawnSquareMask(sourceMask));
		}
	}

	@Test
	public void getFirstRankMaskTest() {
		Assert.assertEquals(BoardConstants.RANK_1_MASK, BoardConstants.getFirstRankMask(Color.WHITE));
		Assert.assertEquals(BoardConstants.RANK_8_MASK, BoardConstants.getFirstRankMask(Color.BLACK));
	}

	@Test
	public void getSecondRankMaskTest() {
		Assert.assertEquals(BoardConstants.RANK_2_MASK, BoardConstants.getSecondRankMask(Color.WHITE));
		Assert.assertEquals(BoardConstants.RANK_7_MASK, BoardConstants.getSecondRankMask(Color.BLACK));
	}

	@Test
	public void getPawnsAttackedSquaresFromLeftTest() {
		final SplittableRandom rng = new SplittableRandom();

		for (int i = 0; i < 100000; i++) {
			long sourceMask = BitBoard.EMPTY;
			long expectedWhiteMask = BitBoard.EMPTY;
			long expectedBlackMask = BitBoard.EMPTY;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				final int file = Square.getFile(square);
				final int rank = Square.getRank(square);

				if (rng.nextInt(16) < 2) {
					sourceMask |= BitBoard.of(square);

					if (file < File.FH && rank < Rank.R8)
						expectedWhiteMask |= BitBoard.of(Square.onFileRank(file + 1, rank + 1));

					if (file < File.FH && rank > Rank.R1)
						expectedBlackMask |= BitBoard.of(Square.onFileRank(file + 1, rank - 1));
				}
			}

			Assert.assertEquals(expectedWhiteMask, BoardConstants.getPawnsAttackedSquaresFromLeft(Color.WHITE, sourceMask));
			Assert.assertEquals(expectedBlackMask, BoardConstants.getPawnsAttackedSquaresFromLeft(Color.BLACK, sourceMask));
		}
	}

	@Test
	public void getPawnsAttackedSquaresFromRightTest() {
		final SplittableRandom rng = new SplittableRandom();

		for (int i = 0; i < 100000; i++) {
			long sourceMask = BitBoard.EMPTY;
			long expectedWhiteMask = BitBoard.EMPTY;
			long expectedBlackMask = BitBoard.EMPTY;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				final int file = Square.getFile(square);
				final int rank = Square.getRank(square);

				if (rng.nextInt(16) < 2) {
					sourceMask |= BitBoard.of(square);

					if (file > File.FA && rank < Rank.R8)
						expectedWhiteMask |= BitBoard.of(Square.onFileRank(file - 1, rank + 1));

					if (file > File.FA && rank > Rank.R1)
						expectedBlackMask |= BitBoard.of(Square.onFileRank(file - 1, rank - 1));
				}
			}

			Assert.assertEquals(expectedWhiteMask, BoardConstants.getPawnsAttackedSquaresFromRight(Color.WHITE, sourceMask));
			Assert.assertEquals(expectedBlackMask, BoardConstants.getPawnsAttackedSquaresFromRight(Color.BLACK, sourceMask));
		}
	}

	@Test
	public void getPawnsAttackedSquaresTest() {
		final SplittableRandom rng = new SplittableRandom();

		for (int i = 0; i < 100000; i++) {
			long sourceMask = BitBoard.EMPTY;
			long expectedWhiteMask = BitBoard.EMPTY;
			long expectedBlackMask = BitBoard.EMPTY;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				final int file = Square.getFile(square);
				final int rank = Square.getRank(square);

				if (rng.nextInt(16) < 2) {
					sourceMask |= BitBoard.of(square);

					if (file < File.FH && rank < Rank.R8)
						expectedWhiteMask |= BitBoard.of(Square.onFileRank(file + 1, rank + 1));

					if (file < File.FH && rank > Rank.R1)
						expectedBlackMask |= BitBoard.of(Square.onFileRank(file + 1, rank - 1));

					if (file > File.FA && rank < Rank.R8)
						expectedWhiteMask |= BitBoard.of(Square.onFileRank(file - 1, rank + 1));

					if (file > File.FA && rank > Rank.R1)
						expectedBlackMask |= BitBoard.of(Square.onFileRank(file - 1, rank - 1));
				}
			}

			Assert.assertEquals(expectedWhiteMask, BoardConstants.getPawnsAttackedSquares(Color.WHITE, sourceMask));
			Assert.assertEquals(expectedBlackMask, BoardConstants.getPawnsAttackedSquares(Color.BLACK, sourceMask));
		}
	}

	@Test
	public void getKingsAttackedSquaresTest() {
		final SplittableRandom rng = new SplittableRandom();

		for (int i = 0; i < 100000; i++) {
			long sourceMask = BitBoard.EMPTY;
			long expectedMask = BitBoard.EMPTY;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				if (rng.nextInt(16) < 2) {
					sourceMask |= BitBoard.of(square);
					expectedMask |= FigureAttackTable.getItem(PieceType.KING, square);
				}
			}

			Assert.assertEquals(expectedMask, BoardConstants.getKingsAttackedSquares(sourceMask));
		}
	}

	@Test
	public void getPawnSingleMoveSquaresTest() {
		final SplittableRandom rng = new SplittableRandom();

		for (int i = 0; i < 100000; i++) {
			final int color = rng.nextInt(Color.LAST);

			long sourceMask = BitBoard.EMPTY;
			long expectedMask = BitBoard.EMPTY;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				if (rng.nextInt(16) < 2) {
					sourceMask |= BitBoard.of(square);

					final int targetSquare = square + File.COUNT * BoardConstants.getPawnRankOffset(color);

					if (Square.isValid(targetSquare))
						expectedMask |= BitBoard.of(targetSquare);
				}
			}

			Assert.assertEquals(expectedMask, BoardConstants.getPawnSingleMoveSquares(color, sourceMask));
		}
	}

	@Test
	public void getPrevEpFileMaskTest() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int file = File.FIRST; file < File.LAST; file++) {
				final long epSquareMask = BoardConstants.getPrevEpFileMask(color, file);

				if (file == File.FA) {
					Assert.assertEquals(BitBoard.EMPTY, epSquareMask);
				} else {
					Assert.assertEquals(1, BitBoard.getSquareCount(epSquareMask));

					final int epSquare = BitBoard.getFirstSquare(epSquareMask);
					Assert.assertEquals(file - 1, Square.getFile(epSquare));
					Assert.assertEquals(BoardConstants.getEpRank(color), Square.getRank(epSquare));
				}
			}
		}
	}

	@Test
	public void getNextEpFileMaskTest() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int file = File.FIRST; file < File.LAST; file++) {
				final long epSquareMask = BoardConstants.getNextEpFileMask(color, file);

				if (file == File.FH) {
					Assert.assertEquals(BitBoard.EMPTY, epSquareMask);
				} else {
					Assert.assertEquals(1, BitBoard.getSquareCount(epSquareMask));

					final int epSquare = BitBoard.getFirstSquare(epSquareMask);
					Assert.assertEquals(file + 1, Square.getFile(epSquare));
					Assert.assertEquals(BoardConstants.getEpRank(color), Square.getRank(epSquare));
				}
			}
		}
	}

	@Test
	public void getPieceAllowedSquaresTest() {
		Assert.assertEquals(BoardConstants.PAWN_ALLOWED_SQUARES, BoardConstants.getPieceAllowedSquares(PieceType.PAWN));

		for (int pieceType = PieceType.FIGURE_FIRST; pieceType < PieceType.FIGURE_LAST; pieceType++)
			Assert.assertEquals(BitBoard.FULL, BoardConstants.getPieceAllowedSquares(pieceType));
	}

	@Test
	public void getMinFileDistanceTest() {
		Assert.assertEquals(File.LAST, BoardConstants.getMinFileDistance(0, File.FB));

		final int fileMask = (1 << File.FB) | (1 << File.FC) | (1 << File.FD) | (1 << File.FH);
		Assert.assertEquals(2, BoardConstants.getMinFileDistance(fileMask, File.FA));
		Assert.assertEquals(1, BoardConstants.getMinFileDistance(fileMask, File.FB));
		Assert.assertEquals(0, BoardConstants.getMinFileDistance(fileMask, File.FC));
		Assert.assertEquals(1, BoardConstants.getMinFileDistance(fileMask, File.FD));
		Assert.assertEquals(2, BoardConstants.getMinFileDistance(fileMask, File.FE));
		Assert.assertEquals(2, BoardConstants.getMinFileDistance(fileMask, File.FF));
		Assert.assertEquals(1, BoardConstants.getMinFileDistance(fileMask, File.FG));
		Assert.assertEquals(0, BoardConstants.getMinFileDistance(fileMask, File.FH));
	}

	@Test
	public void getKingNearSquaresTest() {
		for (int kingSquare = Square.FIRST; kingSquare < Square.LAST; kingSquare++) {
			long expectedMask = BitBoard.EMPTY;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				if (BoardConstants.getKingSquareDistance(kingSquare, square) <= 1)
					expectedMask |= BitBoard.of(square);
			}

			Assert.assertEquals(expectedMask, BoardConstants.getKingNearSquares(kingSquare));
		}
	}

	@Test
	public void getKingSafetyFarSquaresTest() {
		Assert.assertEquals(
				BitBoard.fromString("f8, g8, h8, f7, g7, h7, f6, g6, h6"),
				BoardConstants.getKingSafetyFarSquares(Square.G8)
		);

		Assert.assertEquals(
				BitBoard.fromString("a1, b1, a2, b2, a3, b3"),
				BoardConstants.getKingSafetyFarSquares(Square.A1)
		);
	}
}