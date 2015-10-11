package bishop.base;

import java.util.EnumSet;
import java.util.Set;

import bishop.tables.EmptyEpMaskTable;

public class PositionValidator {
	
	public enum Error {
		KING_COUNT,   // Each side must have exactly one king
		NOT_ON_TURN_CHECK,   // Side, that is not on turn, is in the check
		CASTLING_RIGHTS,   // There is rights to castle, but king or rook is not in their initial position
		PAWNS_ON_RANK_18,   // There are pawns on first or eight rank
		WRONG_EP_FILE   // EP file is set, but pawn cannot been advanced by two squares
	}
	
	private Position position;
	private Set<Error> errorSet;
	
	
	public PositionValidator() {
		errorSet = EnumSet.noneOf (Error.class);
	}
	
	
	public void setPosition(final Position position) {
		this.position = position;
	}
	
	public Set<Error> getErrorSet() {
		return errorSet;
	}
	
	public boolean checkPosition() {
		errorSet.clear();
		
		if (checkKingCount()) {
			checkNotOnTurnCheck();
			checkCastlingRights();
		}
		
		checkPawnsOnBorderRanks();
		checkWrongEpFile();
		
		return errorSet.isEmpty();
	}

	private boolean checkKingCount() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long kingBoard = position.getPiecesMask(color, PieceType.KING);
			final int kingCount = BitBoard.getSquareCount(kingBoard);
			
			if (kingCount != 1) {
				errorSet.add(Error.KING_COUNT);
				return false;
			}
		}
		
		return true;
	}

	private void checkNotOnTurnCheck() {
		final int onTurn = position.getOnTurn();
		final int kingPos = position.getKingPosition(Color.getOppositeColor(onTurn));
		
		if (position.isSquareAttacked(onTurn, kingPos)) {
			errorSet.add(Error.NOT_ON_TURN_CHECK);
		}
	}

	private void checkCastlingRights() {
		final CastlingRights castlingRights = position.getCastlingRights();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int type = CastlingType.FIRST; type != CastlingType.LAST; type++) {
				if (castlingRights.isRight(color, type) && !isCastlingRightPossible (position, color, type)) {
					errorSet.add(Error.CASTLING_RIGHTS);
				}
			}
		}
	}
	
	public static boolean isCastlingRightPossible (final Position position, final int color, final int type) {
		final long kingMask = position.getPiecesMask(color, PieceType.KING);
		final long rookMask = position.getPiecesMask(color, PieceType.ROOK);

		final int rookBeginSquare = BoardConstants.getCastlingRookBeginSquare(color, type);
		final int kingBeginSquare = BoardConstants.getCastlingKingBeginSquare(color);
		
		final boolean rookOk = (rookMask & BitBoard.getSquareMask(rookBeginSquare)) != 0;
		final boolean kingOk = (kingMask & BitBoard.getSquareMask(kingBeginSquare)) != 0;
		
		return rookOk && kingOk;
	}

	private void checkPawnsOnBorderRanks() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long pawnBoard = position.getPiecesMask(color, PieceType.PAWN);
			
			if ((pawnBoard & BoardConstants.RANK_18_MASK) != 0) {
				errorSet.add(Error.PAWNS_ON_RANK_18);
				break;
			}
		}
	}

	private void checkWrongEpFile() {
		final int epFile = position.getEpFile();
		final int color = Color.getOppositeColor(position.getOnTurn());
		
		if (epFile != File.NONE) {
			final int epSquare = BoardConstants.getEpSquare(color, epFile);
			
			if ((position.getPiecesMask(color, PieceType.PAWN) & BitBoard.getSquareMask(epSquare)) == 0) {
				errorSet.add(Error.WRONG_EP_FILE);
				return;
			}
			
			final long freeMask = EmptyEpMaskTable.getItem(color, epFile);
			
			if ((position.getOccupancy() & freeMask) != 0 || !position.isEnPassantPossible()) {
				errorSet.add(Error.WRONG_EP_FILE);
				return;
			}
		}
	}
	
}
