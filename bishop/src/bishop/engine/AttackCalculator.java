package bishop.engine;

import java.util.Arrays;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.CrossDirection;
import bishop.base.LineAttackTable;
import bishop.base.LineIndexer;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.tables.BetweenTable;

public class AttackCalculator {

	public static final int MIN_ATTACK_EVALUATION = 0;
	public static final int MAX_REASONABLE_ATTACK_EVALUATION = 300;
	
	private static final int QUEEN_ATTACK_COEFF = 2;
	private static final int ROOK_ATTACK_COEFF = 2;
	private static final int BISHOP_ATTACK_COEFF = 1;
	private static final int KNIGHT_ATTACK_COEFF = 1;
	private static final int PAWN_ATTACK_COEFF = 1;
	
	private final long[] kingMasks = new long[Color.LAST];   // Mask of kings
	private final long[] pawnAttackedSquares = new long[Color.LAST];

	private final long[] directlyAttackedSquares = new long[Color.LAST];   // Squares attacked by some piece
	private final int[] mobility = new int[PieceType.LAST];
	private final int[] attackEvaluation = new int[Color.LAST];   // Attack evaluation (always positive)

	public void calculate(final Position position, final AttackEvaluationTableGroup attackTables, final MobilityCalculator mobilityCalculator) {
		fillKingMasks(position);
		calculatePawnAttacks(position);
		calculateMobility(position, mobilityCalculator);
		calculateAttacks(position, attackTables);
	}

	private void fillKingMasks(final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			kingMasks[color] = position.getPiecesMask(color, PieceType.KING);
		}
	}

	private void calculatePawnAttacks(final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long pawnsMask = position.getPiecesMask(color, PieceType.PAWN);
			
			pawnAttackedSquares[color] = BoardConstants.getPawnsAttackedSquares(color, pawnsMask);
		}
	}
	
	private void calculateMobility(final Position position, final MobilityCalculator mobilityCalculator) {
		Arrays.fill(mobility, 0);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long oppositePawnAttacks = pawnAttackedSquares[oppositeColor];
			final long freeSquares = ~(oppositePawnAttacks | position.getColorOccupancy(color));

			// Bishop
			final long bishopAttackedSquares = mobilityCalculator.getBishopAttackedSquares(color);
			mobility[PieceType.BISHOP] += Color.colorNegate(color, BitBoard.getSquareCount(bishopAttackedSquares & freeSquares));

			// Rook
			final long rookAttackedSquares = mobilityCalculator.getRookAttackedSquares(color);
			mobility[PieceType.ROOK] += Color.colorNegate(color, BitBoard.getSquareCount(rookAttackedSquares & freeSquares));

			// Queen
			final long queenAttackedSquares = mobilityCalculator.getQueenAttackedSquares(color);
			mobility[PieceType.QUEEN] += Color.colorNegate(color, BitBoard.getSquareCount(queenAttackedSquares & freeSquares));
			
			//Knight
			final long knightAttackedSquares = mobilityCalculator.getKnightAttackedSquares(color);
			mobility[PieceType.KNIGHT] += Color.colorNegate(color, BitBoard.getSquareCount(knightAttackedSquares & freeSquares));

			directlyAttackedSquares[color] = mobilityCalculator.getAllAttackedSquares(color);
		}
	}

	private void calculateAttacks(final Position position, final AttackEvaluationTableGroup attackTables) {
		final long blockingSquareMask = position.getBothColorPiecesMask(PieceType.PAWN);
		final BitLoop sourceSquareLoop = new BitLoop();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final LineAttackEvaluationTable attackTable = attackTables.getLineAttackTable(oppositeColor);
			final long orthogonalNonZeroSquares = attackTable.getNonZeroSquares(CrossDirection.ORTHOGONAL);
			final long diagonalNonZeroSquares = attackTable.getNonZeroSquares(CrossDirection.DIAGONAL);
			int evaluation = 0;
			
			// Bishop
			final long bishopMask = position.getPiecesMask(color, PieceType.BISHOP) & diagonalNonZeroSquares;
			
			for (sourceSquareLoop.init(bishopMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int index = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquareMask);
				evaluation += BISHOP_ATTACK_COEFF * attackTable.getAttackEvaluation (index);
			}

			// Rook
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK) & orthogonalNonZeroSquares;
			
			for (sourceSquareLoop.init(rookMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int index = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquareMask);
				evaluation += ROOK_ATTACK_COEFF * attackTable.getAttackEvaluation (index);
			}

			// Queen
			final long queenMask = position.getPiecesMask(color, PieceType.QUEEN) & (orthogonalNonZeroSquares | diagonalNonZeroSquares);
			
			for (sourceSquareLoop.init(queenMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int indexDiagonal = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquareMask);
				final int diagonalEvaluation = attackTable.getAttackEvaluation (indexDiagonal);
				
				final int indexOrthogonal = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquareMask);
				final int orthogonalEvaluation = attackTable.getAttackEvaluation (indexOrthogonal);
				
				evaluation += QUEEN_ATTACK_COEFF * (diagonalEvaluation + orthogonalEvaluation);
			}
			
			// Knight
			final ShortAttackEvaluationTable knightTable = attackTables.getKnightTable (oppositeColor);
			final long knightMask = position.getPiecesMask(color, PieceType.KNIGHT) & knightTable.getNonZeroSquares();
			
			for (sourceSquareLoop.init(knightMask); sourceSquareLoop.hasNextSquare(); ) {
				final int knightSquare = sourceSquareLoop.getNextSquare();
				
				evaluation += KNIGHT_ATTACK_COEFF * knightTable.getAttackEvaluation(knightSquare);
			}
			
			// Pawn
			final ShortAttackEvaluationTable pawnTable = attackTables.getPawnTable (oppositeColor);
			final long pawnMask = position.getPiecesMask(color, PieceType.PAWN) & pawnTable.getNonZeroSquares();
			
			for (sourceSquareLoop.init(pawnMask); sourceSquareLoop.hasNextSquare(); ) {
				final int pawnSquare = sourceSquareLoop.getNextSquare();
				
				evaluation += PAWN_ATTACK_COEFF * pawnTable.getAttackEvaluation(pawnSquare);
			}
			
			attackEvaluation[color] = evaluation;
		}
	}
	
	public int getMobility (final int pieceType) {
		return mobility[pieceType];
	}
	
	public int getAttackEvaluation(final int color) {
		return attackEvaluation[color];
	}

	public long getDirectlyAttackedSquares(final int color) {
		return directlyAttackedSquares[color];
	}

}
