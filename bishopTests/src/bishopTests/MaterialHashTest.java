package bishopTests;

import bishop.base.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.SplittableRandom;

public class MaterialHashTest {
	@Test
	public void testChangeToOpposite() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final MaterialHash hash = new MaterialHash();

			final int onTurn = rng.nextInt(Color.LAST);
			hash.setOnTurn(onTurn);

			final int pieceCounts[][] = new int[Color.LAST][PieceType.LAST];

			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
					final int count = rng.nextInt(MaterialHash.MAX_COUNT);
					pieceCounts[color][pieceType] = count;
					hash.addPiece(color, pieceType, count);
				}
			}

			final MaterialHash opposite = hash.getOpposite();

			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
					Assert.assertEquals(
							pieceCounts[Color.getOppositeColor(color)][pieceType],
							opposite.getPieceCount(color, pieceType)
					);
				}
			}

			Assert.assertEquals(Color.getOppositeColor(onTurn), opposite.getOnTurn());
		}
	}

	@Test
	public void testGetTotalPieceCount() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final MaterialHash hash = new MaterialHash();

			final int onTurn = rng.nextInt(Color.LAST);
			hash.setOnTurn(onTurn);

			int totalCount = 2;   // Two kings

			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
					final int upperBound = (rng.nextInt(16) == 0) ? MaterialHash.MAX_COUNT : 10;
					final int maxCount = Math.min(upperBound, MaterialHash.MAX_COUNT - totalCount);
					final int count = rng.nextInt(maxCount);
					totalCount += count;
					hash.addPiece(color, pieceType, count);
				}
			}

			Assert.assertEquals(totalCount, hash.getTotalPieceCount());
		}
	}

	@Test
	public void testIsAloneKing() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final MaterialHash hash = new MaterialHash();

			final int onTurn = rng.nextInt(Color.LAST);
			hash.setOnTurn(onTurn);

			final int[] totalCount = new int[Color.LAST];

			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
					final int count = (rng.nextBoolean()) ? rng.nextInt(MaterialHash.MAX_COUNT) : 0 ;
					totalCount[color] += count;
					hash.addPiece(color, pieceType, count);
				}
			}

			for (int color = Color.FIRST; color < Color.LAST; color++)
				Assert.assertEquals(totalCount[color] == 0, hash.isAloneKing(color));
		}
	}

	@Test
	public void testHasQueenRookOrPawn() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final MaterialHash hash = new MaterialHash();

			final int onTurn = rng.nextInt(Color.LAST);
			hash.setOnTurn(onTurn);

			int effectiveCount = 0;

			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
					final int count = (rng.nextBoolean()) ? rng.nextInt(MaterialHash.MAX_COUNT) : 0 ;
					hash.addPiece(color, pieceType, count);

					if (pieceType == PieceType.QUEEN || pieceType == PieceType.ROOK || pieceType == PieceType.PAWN)
						effectiveCount += count;
				}
			}

			Assert.assertEquals(effectiveCount > 0, hash.hasQueenRookOrPawn());
		}
	}

	@Test
	public void testHasQueenRookOrPawnOnSide() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final MaterialHash hash = new MaterialHash();

			final int onTurn = rng.nextInt(Color.LAST);
			hash.setOnTurn(onTurn);

			final int[] effectiveCount = new int[Color.LAST];

			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
					final int count = (rng.nextBoolean()) ? rng.nextInt(MaterialHash.MAX_COUNT) : 0 ;
					hash.addPiece(color, pieceType, count);

					if (pieceType == PieceType.QUEEN || pieceType == PieceType.ROOK || pieceType == PieceType.PAWN)
						effectiveCount[color] += count;
				}
			}

			for (int color = Color.FIRST; color < Color.LAST; color++)
				Assert.assertEquals(effectiveCount[color] > 0, hash.hasQueenRookOrPawnOnSide(color));
		}
	}

	@Test
	public void testHasFigure() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final MaterialHash hash = new MaterialHash();

			final int onTurn = rng.nextInt(Color.LAST);
			hash.setOnTurn(onTurn);

			int effectiveCount = 0;

			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
					final int count = (rng.nextBoolean()) ? rng.nextInt(MaterialHash.MAX_COUNT) : 0 ;
					hash.addPiece(color, pieceType, count);

					if (PieceType.isFigure(pieceType))
						effectiveCount += count;
				}
			}

			Assert.assertEquals(effectiveCount > 0, hash.hasFigure());
		}
	}

}
