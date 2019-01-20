package bishop.base;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.List;

import utils.IoUtils;

public class StandardAlgebraicNotationReader extends StandardAlgebraicNotationBase implements INotationReader {
	
	private static final String IGNORED_CHARACTERS = "?!" + CAPTURE_MARK + CHECK_MARK + MATE_MARK;
	private static final String TERMINATE_CHARACTERS = " \n\r\t)";
	
	private final Position position = new Position();
	private final MoveParser parser = new MoveParser();
	
	
	private String readMoveCore (final PushbackReader reader) throws IOException {
		IoUtils.skipWhiteSpace(reader);
		
		final StringBuilder builder = new StringBuilder();
		
		while (!IoUtils.isEndOfStream(reader)) {
			final char c = IoUtils.readChar(reader);

			if (TERMINATE_CHARACTERS.indexOf(c) >= 0) {
				reader.unread(c);
				break;
			}
			
			if (IGNORED_CHARACTERS.indexOf(c) < 0) {
				builder.append(c);
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * Reads move from given reader.
	 * @param reader source reader
	 * @param position begin position of the move
	 * @param move read move will be assigned here
	 */
	public void readMove (final PushbackReader reader, final Position position, final Move move) throws IOException {
		this.position.assign(position);
		parser.initPosition(position);
		
		final String core = readMoveCore(reader);
		filterMoves(core);
		
		final List<Move> moveList = parser.getMoveList();
		final int moveCount = moveList.size();
		
		if (moveCount > 1)
			throw new RuntimeException("Move '" + core + "' is ambiguous");

		if (moveCount < 1)
			throw new RuntimeException("Move '" + core + "' is not legal");

		move.assign(moveList.get(0));
	}
	
	private void filterMoves(final String core) throws IOException {
		// Castling
		if (core.equals (SHORT_CASTLING_MARK)) {
			filterCastling (CastlingType.SHORT);
			return;
		}
		
		if (core.equals (LONG_CASTLING_MARK)) {
			filterCastling (CastlingType.LONG);
			return;
		}
		
		final int promotionMarkIndex = core.indexOf(PROMOTION_MARK);
		
		if (promotionMarkIndex < 0) {
			// Normal move or en-passant
			filterNormalMove (core);
		}
		else {
			// Promotion move
			if (promotionMarkIndex != core.length() - 2)
				throw new RuntimeException("Wrong format of promotion piece");
			
			final String normalCore = core.substring(0, promotionMarkIndex);
			filterNormalMove (normalCore);
			
			final String promotionPieceStr = core.substring(promotionMarkIndex+1);
			final StringReader promotionPieceReader = new StringReader(promotionPieceStr);
			final int promotionPieceType = PieceType.read(promotionPieceReader);
			
			parser.filterByPromotionPieceType(promotionPieceType);
		}
	}
	
	private void filterNormalMove(final String core) throws IOException {
		final int coreLength = core.length();
		
		// Target square
		if (coreLength < 2)
			throw new RuntimeException("Move core is too short");
		
		final int beginSquareEnd = coreLength - 2;
		final String targetSquareString = core.substring(beginSquareEnd);
		final StringReader targetSquareReader = new StringReader(targetSquareString);
		final int targetSquare = Square.read(targetSquareReader);
		parser.filterByTargetSquare(targetSquare);
		
		// Moving piece type
		final char firstChar = core.charAt(0);
		final int movingPieceType;
		final int beginSquareBegin;
		
		if (coreLength > 2 && Character.isUpperCase(firstChar)) {
			movingPieceType = PieceType.fromChar(firstChar);
			beginSquareBegin = 1;
		}
		else {
			movingPieceType = PieceType.PAWN;
			beginSquareBegin = 0;
		}
		
		parser.filterByMovingPieceType(movingPieceType);
		
		// Begin file or rank
		for (int i = beginSquareBegin; i < beginSquareEnd; i++) {
			final char ch = core.charAt(i);
			
			if (Character.isLetter(ch)) {
				final int file = File.fromChar (ch);
				parser.filterByBeginFile(file);
			}
			
			if (Character.isDigit(ch)) {
				final int rank = Rank.fromChar (ch);
				parser.filterByBeginRank(rank);
			}			
		}
	}

	private void filterCastling (final int castlingType) {
		parser.filterByMoveType (MoveType.CASTLING);
		
		final int onTurn = position.getOnTurn();
		final int targetSquare = CastlingConstants.getCastlingKingTargetSquare(onTurn, castlingType);
		
		parser.filterByTargetSquare(targetSquare);
	}

}
