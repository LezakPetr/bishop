package bishop.base;

import bishop.tables.FigureAttackTable;
import bishop.tables.MateCheckerTables;

public class MateChecker {

	private final IMoveWalker walker = new IMoveWalker() {
		public boolean processMove (final Move move) {
			legalMoveFound = true;
			return false;
		}
	};
	
	private final LegalMoveGenerator generator;
	private boolean legalMoveFound;

	public MateChecker() {
		generator = new LegalMoveGenerator();
		generator.setWalker(walker);
		generator.setReduceMovesInCheck(true);
		generator.setGenerateMovesOfPiece(PieceType.KING, false);
	}

	public boolean isMate (final Position position) {
		final boolean isCheck = position.isCheck();
		
		if (!isCheck)
			return false;
		
		return isMateInCheck(position);
	}

	public boolean isMateInCheck(final Position position) {
		return !existLegalKingMove(position) && !existOtherMove(position);
	}
	
	private boolean existLegalKingMove(final Position position) {
		final int onTurn = position.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);
		final int ownKingSquare = position.getKingPosition(onTurn);

		final int oppositeKingSquare = position.getKingPosition(oppositeColor);
		
		// King
		long attackedSquares = FigureAttackTable.getItem(PieceType.KING, oppositeKingSquare);
		
		// Knight
		final long oppositeKnightMask = position.getPiecesMask(oppositeColor, PieceType.KNIGHT) & MateCheckerTables.getKnightAffectingSquares(ownKingSquare);
		
		for (BitLoop loop = new BitLoop(oppositeKnightMask); loop.hasNextSquare(); ) {
			final int sourceSquare = loop.getNextSquare();
			attackedSquares |= FigureAttackTable.getItem(PieceType.KNIGHT, sourceSquare);
		}
		
		// Pawns
		final long oppositePawnMask = position.getPiecesMask(oppositeColor, PieceType.PAWN);
		attackedSquares |= BoardConstants.getPawnsAttackedSquares(oppositeColor, oppositePawnMask);
		
		// Bishop
		final long occupancy = position.getOccupancy();
		final long ownKingMask = BitBoard.getSquareMask(ownKingSquare);
		final long blockers = occupancy & ~ownKingMask;

		final long diagonalAffectingSquares = MateCheckerTables.getLineAffectingSquares(CrossDirection.DIAGONAL, ownKingSquare);
		final long oppositeBishopMask = position.getPiecesMask(oppositeColor, PieceType.BISHOP) & diagonalAffectingSquares;
		
		for (BitLoop loop = new BitLoop(oppositeBishopMask); loop.hasNextSquare(); ) {
			final int sourceSquare = loop.getNextSquare();
			
			final int index = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockers);
			attackedSquares |= LineAttackTable.getAttackMask(index);
		}

		// Rook
		final long orthogonalAffectingSquares = MateCheckerTables.getLineAffectingSquares(CrossDirection.ORTHOGONAL, ownKingSquare);
		final long oppositeRookMask = position.getPiecesMask(oppositeColor, PieceType.ROOK) & orthogonalAffectingSquares;
		
		for (BitLoop loop = new BitLoop(oppositeRookMask); loop.hasNextSquare(); ) {
			final int sourceSquare = loop.getNextSquare();
			
			final int index = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockers);
			attackedSquares |= LineAttackTable.getAttackMask(index);
		}

		// Queen
		final long oppositeQueenMask = position.getPiecesMask(oppositeColor, PieceType.QUEEN) & (orthogonalAffectingSquares | diagonalAffectingSquares);
		
		for (BitLoop loop = new BitLoop(oppositeQueenMask); loop.hasNextSquare(); ) {
			final int sourceSquare = loop.getNextSquare();
			
			final int indexDiagonal = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockers);
			attackedSquares |= LineAttackTable.getAttackMask(indexDiagonal);
			
			final int indexOrthogonal = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockers);
			attackedSquares |= LineAttackTable.getAttackMask(indexOrthogonal);
		}
		
		final long ownPieces = position.getColorOccupancy(onTurn);
		
		return (FigureAttackTable.getItem(PieceType.KING, ownKingSquare) & ~attackedSquares & ~ownPieces) != 0;
	}
	
	private boolean existOtherMove(final Position position) {
		legalMoveFound = false;
		
		generator.setPosition(position);
		generator.generateMoves();
		
		return legalMoveFound;
	}

}
