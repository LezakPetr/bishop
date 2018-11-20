package bishop.engine;

import java.util.Arrays;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.CrossDirection;
import bishop.base.LineAttackTable;
import bishop.base.LineIndexer;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;

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
	private boolean canBeMate;
	private boolean hasPin;

	public void calculate(final Position position, final AttackEvaluationTableGroup attackTables) {
		fillKingMasks(position);
		calculatePawnAttacks(position);
		calculateMobility(position);
		calculateAttacks(position, attackTables);
		calculateCanBeMate(position);
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
	
	private void calculateMobility(final Position position) {
		final long occupancy = position.getOccupancy();
		final BitLoop sourceSquareLoop = new BitLoop();
		hasPin = false;

		Arrays.fill(mobility, 0);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long oppositePawnAttacks = pawnAttackedSquares[oppositeColor];
			final long freeSquares = ~(oppositePawnAttacks | position.getColorOccupancy(color));
			final long blockingSquares = occupancy & ~kingMasks[oppositeColor];
			long ownAttackedSquares = pawnAttackedSquares[color];
			
			final long oppositeRooks = position.getPiecesMask(oppositeColor, PieceType.ROOK);
			final long oppositeQueens = position.getPiecesMask(oppositeColor, PieceType.QUEEN);
			final long oppositeKings = position.getPiecesMask(oppositeColor, PieceType.KING);
			
			// Bishop
			final long bishopMask = position.getPiecesMask(color, PieceType.BISHOP);
			long bishopAttackedSquares = BitBoard.EMPTY;
			
			for (sourceSquareLoop.init(bishopMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int index = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquares);
				final long attack = LineAttackTable.getAttackMask(index);
				bishopAttackedSquares |= attack;

				final long pinTargets = oppositeRooks | oppositeQueens | oppositeKings;

				if ((pinTargets & attack) != 0)      // Speedup - there must be some attacked piece which is pinned																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																													
					hasPin |= isPin (sourceSquare, pinTargets, index);
			}

			mobility[PieceType.BISHOP] += Color.colorNegate(color, BitBoard.getSquareCount(bishopAttackedSquares & freeSquares));
			ownAttackedSquares |= bishopAttackedSquares;

			// Rook
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK);
			long rookAttackedSquares = BitBoard.EMPTY;
			
			for (sourceSquareLoop.init(rookMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int index = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquares);
				final long attack = LineAttackTable.getAttackMask(index);
				rookAttackedSquares |= attack;

				final long pinnedSquares = oppositeQueens | oppositeKings;

				if ((pinnedSquares & attack) != 0)   // Speedup - there must be some attacked piece which is pinned
					hasPin |= isPin (sourceSquare, pinnedSquares, index);
			}

			mobility[PieceType.ROOK] += Color.colorNegate(color, BitBoard.getSquareCount(rookAttackedSquares & freeSquares));
			ownAttackedSquares |= rookAttackedSquares;

			// Queen
			final long queenMask = position.getPiecesMask(color, PieceType.QUEEN);
			long queenAttackedSquares = BitBoard.EMPTY;
			
			for (sourceSquareLoop.init(queenMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int indexDiagonal = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquares);
				final long attackDiagonal = LineAttackTable.getAttackMask(indexDiagonal);
				
				final int indexOrthogonal = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquares);
				final long attackOrthogonal = LineAttackTable.getAttackMask(indexOrthogonal);

				final long attack = attackOrthogonal | attackDiagonal;
				queenAttackedSquares |= attack;
			}

			mobility[PieceType.QUEEN] += Color.colorNegate(color, BitBoard.getSquareCount(queenAttackedSquares & freeSquares));
			ownAttackedSquares |= queenAttackedSquares;
			
			//Knight
			final long knightMask = position.getPiecesMask(color, PieceType.KNIGHT);
			long knightAttackedSquares = BitBoard.EMPTY;
			
			for (sourceSquareLoop.init(knightMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				final long attack = FigureAttackTable.getItem(PieceType.KNIGHT, sourceSquare);
				knightAttackedSquares |= attack;
			}

			mobility[PieceType.QUEEN] += Color.colorNegate(color, BitBoard.getSquareCount(knightAttackedSquares & freeSquares));
			ownAttackedSquares |= knightAttackedSquares;

			// King
			final int kingSquare = position.getKingPosition(color);
			final long kingAttack = FigureAttackTable.getItem(PieceType.KING, kingSquare);
			ownAttackedSquares |= kingAttack;
			
			directlyAttackedSquares[color] = ownAttackedSquares;
		}
	}

	/**
	 * Checks if there is a pin by piece from sourceSquare to two pieces with pieceMask.
	 * @param sourceSquare square with pinning piece
	 * @param pieceMask mask of possible pinned and pinned-to squares
	 * @param index cross index
	 * @return true if there is a pin, false if not
	 */
	private boolean isPin(final int sourceSquare, final long pieceMask, final int index) {
		final long pins = LineAttackTable.getPinMask(index) & pieceMask;
		
		for (BitLoop loop = new BitLoop(pins); loop.hasNextSquare(); ) {
			final int pinSquare = loop.getNextSquare();
			
			if ((BetweenTable.getItem(sourceSquare, pinSquare) & pieceMask) != 0)
				return true;
		}
		
		return false;
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
	
	private void calculateCanBeMate(final Position position) {
		final int onTurn = position.getOnTurn();
		final int notOnTurn = Color.getOppositeColor(onTurn);
		final int kingSquare = position.getKingPosition(onTurn);
		final long kingMask = BitBoard.getSquareMask(kingSquare);
		
		final long requiredSquares = kingMask | FigureAttackTable.getItem(PieceType.KING, kingSquare);
		final long inaccessibleSquares = (position.getColorOccupancy(onTurn) & ~kingMask) | directlyAttackedSquares[notOnTurn];
		
		canBeMate = ((~inaccessibleSquares & requiredSquares) == 0);
	}
	
	public int getMobility (final int pieceType) {
		return mobility[pieceType];
	}
	
	public int getAttackEvaluation(final int color) {
		return attackEvaluation[color];
	}
	
	public boolean isKingAttacked (final int color) {
		final int oppositeColor = Color.getOppositeColor(color);
		
		return (directlyAttackedSquares[oppositeColor] & kingMasks[color]) != 0;
	}

	public long getDirectlyAttackedSquares(final int color) {
		return directlyAttackedSquares[color];
	}
	
	public boolean getCanBeMate() {
		return canBeMate;
	}
	
	public boolean isPin() {
		return hasPin;
	}
}
