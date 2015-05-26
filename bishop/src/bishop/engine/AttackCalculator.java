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
	
	private final long[] kingMasks = new long[Color.LAST];   // Mask of kings
	private final long[] pawnAttackedSquares = new long[Color.LAST];   // Squares attacked by pawns
	private final long[] directlyAttackedSquares = new long[Color.LAST];   // Squares attacked by some piece
	private final long[] indirectlyAttackedSquares = new long[Color.LAST];   // Squares attacked by some piece, only pawns are blocking squares
	private final int[] mobility = new int[Piece.LAST_PROMOTION_FIGURE_INDEX];
	private int attackEvaluation;
	private boolean canBeMate;
	private boolean hasPin;
	
	public void calculate(final Position position, final AttackEvaluationTable attackTable) {
		fillKingMasks(position);
		calculatePawnAttacks(position);
		calculateMobility(position);
		calculateAttacks(position, attackTable);
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
			final long freeSquares = ~(oppositePawnAttacks | occupancy);
			final long blockingSquares = occupancy & ~kingMasks[oppositeColor];
			long ownAttackedSquares = pawnAttackedSquares[color];
			
			final long oppositeRooks = position.getPiecesMask(oppositeColor, PieceType.ROOK);
			final long oppositeQueens = position.getPiecesMask(oppositeColor, PieceType.QUEEN);
			final long oppositeKings = position.getPiecesMask(oppositeColor, PieceType.KING);
			
			// Bishop
			final long bishopMask = position.getPiecesMask(color, PieceType.BISHOP);
			
			for (sourceSquareLoop.init(bishopMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int index = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquares);
				final long attack = LineAttackTable.getAttackMask(index);
				final int mobilityCount = BitBoard.getSquareCount(attack & freeSquares);
				
				mobility[Piece.getPromotionFigureIndex(color, PieceType.BISHOP)] += mobilityCount;
				ownAttackedSquares |= attack;
				hasPin |= isPin (sourceSquare, oppositeRooks | oppositeQueens | oppositeKings, index);
			}

			// Rook
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK);
			
			for (sourceSquareLoop.init(rookMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int index = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquares);
				final long attack = LineAttackTable.getAttackMask(index);
				final int mobilityCount = BitBoard.getSquareCount(attack & freeSquares);
				
				mobility[Piece.getPromotionFigureIndex(color, PieceType.ROOK)] += mobilityCount;
				ownAttackedSquares |= attack;
				hasPin |= isPin (sourceSquare, oppositeQueens | oppositeKings, index);
			}

			// Queen
			final long queenMask = position.getPiecesMask(color, PieceType.QUEEN);
			
			for (sourceSquareLoop.init(queenMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int indexDiagonal = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquares);
				final long attackDiagonal = LineAttackTable.getAttackMask(indexDiagonal);
				
				final int indexOrthogonal = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquares);
				final long attackOrthogonal = LineAttackTable.getAttackMask(indexOrthogonal);
				
				final int mobilityCount = BitBoard.getSquareCount((attackDiagonal | attackOrthogonal) & freeSquares);
				mobility[Piece.getPromotionFigureIndex(color, PieceType.QUEEN)] += mobilityCount;
				
				ownAttackedSquares |= attackOrthogonal;
				ownAttackedSquares |= attackDiagonal;
			}
			
			//Knight
			final long knightMask = position.getPiecesMask(color, PieceType.KNIGHT);
			
			for (sourceSquareLoop.init(knightMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				final long attack = FigureAttackTable.getItem(PieceType.KNIGHT, sourceSquare);
				
				final int mobilityCount = BitBoard.getSquareCount(attack & freeSquares);
				mobility[Piece.getPromotionFigureIndex(color, PieceType.KNIGHT)] += mobilityCount;
				ownAttackedSquares |= attack;
			}
			
			// King
			final int kingSquare = position.getKingPosition(color);
			ownAttackedSquares |= FigureAttackTable.getItem(PieceType.KING, kingSquare);
			
			directlyAttackedSquares[color] = ownAttackedSquares;
		}
	}
	
	private boolean isPin(final int sourceSquare, final long pieceMask, final int index) {
		final long pins = LineAttackTable.getPinMask(index) & pieceMask;
		
		for (BitLoop loop = new BitLoop(pins); loop.hasNextSquare(); ) {
			final int pinSquare = loop.getNextSquare();
			
			if ((BetweenTable.getItem(sourceSquare, pinSquare) & pieceMask) != 0)
				return true;
		}
		
		return false;
	}

	private void calculateAttacks(final Position position, final AttackEvaluationTable attackTable) {
		final long blockingSquareMask = position.getPiecesMask(Color.WHITE, PieceType.PAWN) | position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		final BitLoop sourceSquareLoop = new BitLoop();
		
		attackEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			long ownAttackedSquares = BitBoard.EMPTY;
			
			// Bishop
			final long bishopMask = position.getPiecesMask(color, PieceType.BISHOP);
			
			for (sourceSquareLoop.init(bishopMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int index = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquareMask);
				attackEvaluation += attackTable.getAttackEvaluation (color, index);
				
				final long attack = LineAttackTable.getAttackMask(index);
				ownAttackedSquares |= attack;
			}

			// Rook
			final long rookMask = position.getPiecesMask(color, PieceType.ROOK);
			
			for (sourceSquareLoop.init(rookMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int index = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquareMask);
				attackEvaluation += attackTable.getAttackEvaluation (color, index);
				
				final long attack = LineAttackTable.getAttackMask(index);
				ownAttackedSquares |= attack;
			}

			// Queen
			final long queenMask = position.getPiecesMask(color, PieceType.QUEEN);
			
			for (sourceSquareLoop.init(queenMask); sourceSquareLoop.hasNextSquare(); ) {
				final int sourceSquare = sourceSquareLoop.getNextSquare();
				
				final int indexDiagonal = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquareMask);
				attackEvaluation += attackTable.getAttackEvaluation (color, indexDiagonal);
				
				final int indexOrthogonal = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquareMask);
				attackEvaluation += attackTable.getAttackEvaluation (color, indexOrthogonal);
				
				final long attackDiagonal = LineAttackTable.getAttackMask(indexDiagonal);
				final long attackOrthogonal = LineAttackTable.getAttackMask(indexOrthogonal);
				ownAttackedSquares |= attackDiagonal | attackOrthogonal;
			}
			
			indirectlyAttackedSquares[color] = ownAttackedSquares;
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
	
	public int getMobility (final int color, final int pieceType) {
		return mobility[Piece.getPromotionFigureIndex(color, pieceType)];
	}
	
	public int getAttackEvaluation() {
		return attackEvaluation;
	}
	
	public boolean isKingAttacked (final int color) {
		final int oppositeColor = Color.getOppositeColor(color);
		
		return (directlyAttackedSquares[oppositeColor] & kingMasks[color]) != 0;
	}

	public long getPawnAttackedSquares(final int color) {
		return pawnAttackedSquares[color];
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
