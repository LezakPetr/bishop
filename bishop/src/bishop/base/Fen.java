package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;

import utils.IoUtils;

/**
 * Class for input/output of Forsyth–Edwards Notation.
 * @author Bc. Petr Ležák
 */
public class Fen {

	private final Position position = new Position();
	private int halfMoveClock = 0;
	private int moveNumber = 1;

	/**
	 * Writes piece placement into writer.
	 * @param writer target PrintWriter
	 */
	private void writePieces (final PrintWriter writer) {
		for (int rank = Rank.R8; rank >= Rank.R1; rank--) {
			int blankCount = 0;

			for (int file = File.FA; file <= File.FH; file++) {
				final int square = Square.onFileRank(file, rank);

				final Piece piece = position.getSquareContent (square);

				if (piece != null) {
					if (blankCount > 0) {
						writer.print(blankCount);

						blankCount = 0;
					}

					piece.write(writer);
				}
				else
					blankCount++;
			}

			if (blankCount > 0)
				writer.print(blankCount);

			if (rank > Rank.R1)
				writer.print('/');
		}
	}

	/**
	 * Writes castling availability into writer.
	 * @param writer target PrintWriter
	 */
	private void writeCastling (final PrintWriter writer) {
		final CastlingRights castlingRights = position.getCastlingRights();

		// White
		if (castlingRights.isRight(Color.WHITE, CastlingType.SHORT))
			writer.print('K');

		if (castlingRights.isRight (Color.WHITE, CastlingType.LONG))
			writer.print('Q');

		// Black
		if (castlingRights.isRight (Color.BLACK, CastlingType.SHORT))
			writer.print('k');

		if (castlingRights.isRight (Color.BLACK, CastlingType.LONG))
			writer.print('q');

		if (castlingRights.isEmpty())
			writer.print('-');
	}

	/**
	 * Writes en passant target square into writer.
	 * @param writer target PrintWriter
	 */
	private void writeEpSquare (final PrintWriter writer) {
		final int file = position.getEpFile();

		if (file == File.NONE)
			writer.write('-');
		else {
			final int onTurn = position.getOnTurn();
			final int rank = (onTurn == Color.WHITE) ? Rank.R6 : Rank.R3;
			final int epSquare = Square.onFileRank(file, rank);

			Square.write(writer, epSquare);
		}
	}

	/**
	 * Reads piece placement from reader.
	 * @param reader source reader
	 * @throws IOException when IO failure occurs or when there is wrong format of FEN pieces
	 */
	private void readPieces (final PushbackReader reader) throws IOException {
		for (int rank = Rank.R8; rank >= Rank.R1; rank--) {
			int file = File.FIRST;

			while (file < File.LAST) {
				final char c = IoUtils.readChar(reader);

				if (Character.isDigit(c)) {
					final int blankCount = Character.getNumericValue(c);

					if (blankCount < 0 || blankCount > File.LAST)
						throw new RuntimeException("Unexpected digit in FEN");

					for (int i = 0; i < blankCount; i++) {
						if (!File.isValid(file))
							throw new RuntimeException ("File coordinate overflow");

						final int square = Square.onFileRank(file, rank);

						position.setSquareContent(square, null);
						file++;
					}
				}
				else {
					if (!File.isValid (file))
						throw new IOException ("File coordinate overflow");

					reader.unread (c);

					final Piece piece = Piece.read (reader);
					final int square = Square.onFileRank(file, rank);

					position.setSquareContent (square, piece);
					file++;
				}
			}

			// Rank delimiter
			if (rank > Rank.R1) {
				final char c = IoUtils.readChar(reader);

				if (c != '/')
					throw new IOException("Expected '/' character");
			}
		}
	}

	/**
	 * Reads castling availability from reader.
	 * @param reader source reader
	 * @throws IOException when IO failure occurs or when there is wrong format of FEN castling
	 */
	private void readCastling (final PushbackReader reader) throws IOException {
		final CastlingRights castlingRights = new CastlingRights();

		while (true) {
			final char c = IoUtils.readChar(reader);

			if (Character.isWhitespace(c)) {
				reader.unread(c);
				break;
			}

			switch (c) {
			case 'K':
				castlingRights.setRight (Color.WHITE, CastlingType.SHORT, true);
				break;

			case 'Q':
				castlingRights.setRight (Color.WHITE, CastlingType.LONG, true);
				break;

			case 'k':
				castlingRights.setRight (Color.BLACK, CastlingType.SHORT, true);
				break;

			case 'q':
				castlingRights.setRight (Color.BLACK, CastlingType.LONG, true);
				break;

			case '-':
				break;

			default:
				throw new IOException("Unrecognized character");
			}
		}

		position.setCastlingRights (castlingRights);
	}

	/**
	 * Reads en passant target square from reader.
	 * @param reader source reader
	 * @throws IOException when IO failure occurs or where there is wrong format of FEN EP square
	 */
	private void readEpSquare (final PushbackReader reader) throws IOException {
		final char c = IoUtils.readChar(reader);

		if (c == '-')
			position.setEpFile (File.NONE);
		else {
			reader.unread(c);

			final int epSquare = Square.read(reader);
			final int epRank = Square.getRank(epSquare);
			final int onTurn = position.getOnTurn();

			if ((onTurn == Color.WHITE && epRank == Rank.R6) || (onTurn == Color.BLACK && epRank == Rank.R3)) {
				final int epFile = Square.getFile(epSquare);
				position.setEpFile (epFile);
				
				position.refreshCachedData();
				
				if (!position.isEnPassantPossible())
					position.setEpFile (File.NONE);
			}
			else
				throw new IOException ("Invalid EP square");
		}

	}

	/**
	 * Returns position.
	 */
	public Position getPosition() {
		return position;
	}


	/**
	 * Sets position.
	 * @param position position to assign
	 */
	public void setPosition (final Position position) {
		this.position.assign (position);
	}

	/**
	 * Returns half move clock.
	 * @return number of half-moves after last pawn advance or capture
	 */
	public int getHalfMoveClock() {
		return halfMoveClock;
	}

	/**
	 * Sets half move clock.
	 * @param halfMoveClock number of half-moves after last pawn advance or capture
	 */
	public void setHalfMoveClock (final int halfMoveClock) {
		this.halfMoveClock = halfMoveClock;
	}

	/**
	 * Returns move number.
	 * @return move number (starting at 1)
	 */
	public int getMoveNumber() {
		return moveNumber;
	}

	/**
	 * Sets move number.
	 * @param moveNumber move number (starting at 1)
	 */
	public void setMoveNumber (final int moveNumber) {
		this.moveNumber = moveNumber;
	}

	// Writes FEN into stream.
	public void writeFen (final PrintWriter writer) {
		writePieces (writer);
		writer.print(' ');
		Color.write (writer, position.getOnTurn());
		writer.print(' ');
		writeCastling (writer);
		writer.print(' ');
		writeEpSquare (writer);

		writer.print(' ');
		writer.print(halfMoveClock);
		writer.print(' ');
		writer.print(moveNumber);
	}

	/**
	 * Reads FEN from reader.
	 * @param reader source reader
	 * @throws IOException when IO failure occurs or where there is wrong format of FEN
	 */
	public void readFen (final PushbackReader reader) throws IOException {
		position.clearPosition();

		readPieces (reader);
		IoUtils.skipWhiteSpace(reader);

		final int onTurn = Color.read (reader);
		position.setOnTurn (onTurn);

		IoUtils.skipWhiteSpace(reader);
		readCastling (reader);
		IoUtils.skipWhiteSpace(reader);
		readEpSquare (reader);
		IoUtils.skipWhiteSpace(reader);

		halfMoveClock = IoUtils.readInt (reader);
		moveNumber = IoUtils.readInt (reader);

		position.refreshCachedData();
		validatePosition();
	}
	
	private void validatePosition() throws IOException {
		final PositionValidator validator = new PositionValidator();
		validator.setPosition(position);
		
		if (!validator.checkPosition())
			throw new IOException("Position is not valid");
	}

	/**
	 * Reads FEN from string.
	 * @param str source string
	 * @throws IOException when IO failure occurs or where there is wrong format of FEN
	 */
	public void readFenFromString (final String str) throws IOException {
		final PushbackReader reader = IoUtils.getPushbackReader (str);

		readFen(reader);
	}
}
