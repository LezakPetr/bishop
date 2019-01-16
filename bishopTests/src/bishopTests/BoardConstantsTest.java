package bishopTests;

import bishop.base.*;
import bishop.tables.BetweenTable;
import org.junit.Assert;
import org.junit.Test;

public class BoardConstantsTest {
	@Test
	public void getSquareColorMaskTest() {
		Assert.assertEquals(BoardConstants.WHITE_SQUARE_MASK, BoardConstants.getSquareColorMask(Color.WHITE));
		Assert.assertEquals(BoardConstants.BLACK_SQUARE_MASK, BoardConstants.getSquareColorMask(Color.BLACK));
	}

	@Test
	public void getCastlingMiddleSquareMaskTest() {
		Assert.assertEquals(BetweenTable.getItem(Square.E1, Square.H1), BoardConstants.getCastlingMiddleSquareMask(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(BetweenTable.getItem(Square.E1, Square.A1), BoardConstants.getCastlingMiddleSquareMask(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(BetweenTable.getItem(Square.E8, Square.H8), BoardConstants.getCastlingMiddleSquareMask(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(BetweenTable.getItem(Square.E8, Square.A8), BoardConstants.getCastlingMiddleSquareMask(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingRookBeginSquareTest() {
		Assert.assertEquals(Square.H1, BoardConstants.getCastlingRookBeginSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.A1, BoardConstants.getCastlingRookBeginSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.H8, BoardConstants.getCastlingRookBeginSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.A8, BoardConstants.getCastlingRookBeginSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingRookTargetSquareTest() {
		Assert.assertEquals(Square.F1, BoardConstants.getCastlingRookTargetSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.D1, BoardConstants.getCastlingRookTargetSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.F8, BoardConstants.getCastlingRookTargetSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.D8, BoardConstants.getCastlingRookTargetSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingTargetSquareTest() {
		Assert.assertEquals(Square.G1, BoardConstants.getCastlingKingTargetSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.C1, BoardConstants.getCastlingKingTargetSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.G8, BoardConstants.getCastlingKingTargetSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.C8, BoardConstants.getCastlingKingTargetSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingMiddleSquareTest() {
		Assert.assertEquals(Square.F1, BoardConstants.getCastlingKingMiddleSquare(Color.WHITE, CastlingType.SHORT));
		Assert.assertEquals(Square.D1, BoardConstants.getCastlingKingMiddleSquare(Color.WHITE, CastlingType.LONG));
		Assert.assertEquals(Square.F8, BoardConstants.getCastlingKingMiddleSquare(Color.BLACK, CastlingType.SHORT));
		Assert.assertEquals(Square.D8, BoardConstants.getCastlingKingMiddleSquare(Color.BLACK, CastlingType.LONG));
	}

	@Test
	public void getCastlingKingChangeMaskTest() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
				Assert.assertEquals(
						BitBoard.of(
								BoardConstants.getCastlingKingBeginSquare(color),
								BoardConstants.getCastlingKingTargetSquare(color, castlingType)
						),
						BoardConstants.getCastlingKingChangeMask(color, castlingType)
				);
			}
		}
	}

	@Test
	public void getCastlingRookChangeMaskTest() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
				Assert.assertEquals(
						BitBoard.of(
								BoardConstants.getCastlingRookBeginSquare(color, castlingType),
								BoardConstants.getCastlingRookTargetSquare(color, castlingType)
						),
						BoardConstants.getCastlingRookChangeMask(color, castlingType)
				);
			}
		}
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
}
