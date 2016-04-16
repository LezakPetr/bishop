package bishop.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

import utils.CharacterFilters;
import utils.IoUtils;

public class PgnReader extends Pgn {
	
	private final StandardAlgebraicNotationReader sanReader = new StandardAlgebraicNotationReader();
	protected final Stack<ITreeIterator<IGameNode>> nodeStack = new Stack<ITreeIterator<IGameNode>>();
	
	
	public void readPgnFromStream (final InputStream stream) throws IOException {
		final PushbackReader pushbackReader = createPushbackReader(stream);
		
		readPgn(pushbackReader);
	}

	public Game readSingleGame (final PushbackReader reader) throws IOException {
		try {
			gameList.clear();
			
			if (readSingleGameInternal(reader))
				return gameList.get(0);
		}
		catch (Throwable th) {
			throwExceptionWithInfo(reader, th);
		}
		
		return null;
	}

	public void readPgn (final PushbackReader reader) throws IOException {
		try {
			gameList.clear();
			
			while (readSingleGameInternal(reader))
				;
		}
		catch (Throwable th) {
			throwExceptionWithInfo(reader, th);
		}
	}

	private void throwExceptionWithInfo(final PushbackReader reader, Throwable th) throws IOException {
		final String next = IoUtils.readString(reader, CharacterFilters.getNonEndOfLineFilter());

		throw new RuntimeException("Cannot read PGN, error before " + next, th);
	}

	private boolean readSingleGameInternal(final PushbackReader reader) throws IOException {
		IoUtils.skipWhiteSpace(reader);
		
		if (IoUtils.isEndOfStream(reader))
			return false;
		
		final Game game = readGame(reader);
		gameList.add(game);
		
		return true;
	}
	
	private Game readGame(final PushbackReader reader) throws IOException {
		final Game game = new Game();
		
		readHeader(reader, game);
		readMoves(reader, game);		
		
		return game;
	}
	
	private void readHeader (final PushbackReader reader, final Game game) throws IOException {
		while (true) {
			IoUtils.skipWhiteSpace(reader);
			
			final char ch = IoUtils.readChar(reader);
			reader.unread(ch);
			
			if (ch == BEGIN_TAG_CHAR) {
				readTag (reader, game);
			}
			else
				break;
		}
	}
	
	private void readTag(final PushbackReader reader, final Game game) throws IOException {
		IoUtils.checkExpectedChar(reader, BEGIN_TAG_CHAR);
		
		final String name = IoUtils.readString(reader);
		final String value = IoUtils.readQuotedString(reader);
		
		IoUtils.checkExpectedChar(reader, END_TAG_CHAR);
		
		processTag (name, value, game);
	}
	
	private void readMoves (final PushbackReader reader, final Game game) throws IOException {
		nodeStack.clear();
		nodeStack.push(game.getRootIterator());
		
		while (processMovetextToken(reader, game))
			;
	}
	
	private void processTag (final String name, final String value, final Game game) throws IOException {
		final GameHeader header = game.getHeader();
				
		if (name.equalsIgnoreCase(TAG_EVENT)) {
			header.setEvent(value);
		}
		
		if (name.equalsIgnoreCase(TAG_SITE)) {
			header.setSite(value);
		}

		if (name.equalsIgnoreCase(TAG_DATE)) {
			header.setDate(value);
		}

		if (name.equalsIgnoreCase(TAG_ROUND)) {
			header.setRound(value);
		}

		if (name.equalsIgnoreCase(TAG_WHITE)) {
			header.setWhite(value);
		}

		if (name.equalsIgnoreCase(TAG_BLACK)) {
			header.setBlack(value);
		}

		if (name.equalsIgnoreCase(TAG_RESULT)) {
			header.setResult(GameResult.fromString(value));
		}
		
		if (name.equalsIgnoreCase(TAG_FEN)) {
			processFen (value, game);
		}
	}
	
	private boolean processMovetextToken (final PushbackReader reader, final Game game) throws IOException {
		IoUtils.skipWhiteSpace(reader);
		
		final char firstChar = IoUtils.readChar(reader);
		
		// Brackets
		if (firstChar == '(') {
			final ITreeIterator<IGameNode> currentIt = nodeStack.peek().copy();
			currentIt.moveParent();
			
			nodeStack.push(currentIt);
			return true;
		}
		
		if (firstChar == ')') {
			nodeStack.pop();
			return true;
		}
		
		// Commentary
		if (firstChar == BEGIN_COMMENTARY_CHAR) {
			readCommentary(reader);
			
			return true;
		}
		
		if (firstChar == ';') {
			while (!IoUtils.isEndOfStream(reader) && IoUtils.readChar(reader) != '\n')
				;
			
			return true;
		}
		
		reader.unread(firstChar);
		
		if (Character.isLetter(firstChar)) {
			// Move
			final ITreeIterator<IGameNode> currentIt = nodeStack.pop();
			final IGameNode currentNode = currentIt.getItem();
			
			final Move move = new Move();
			sanReader.readMove(reader, currentNode.getTargetPosition(), move);
			
			final ITreeIterator<IGameNode> nextIt = game.addChild(currentIt, move);
			nodeStack.add(nextIt);
		}
		else {
			if (firstChar == ANNOTATION_MARK) {
				IoUtils.readChar(reader);
				
				final int nag = IoUtils.readInt(reader);
				final Annotation annotation = Annotation.withNag (nag);
				final ITreeIterator<IGameNode> lastIt = nodeStack.peek();
				
				lastIt.getItem().setAnnotation (annotation);
			}
			else {
				// Non move tokens
				final String token = IoUtils.readString(reader, nonMoveTokenFilter);
				
				if (token.isEmpty()) {
					// Unknown character - skip
					IoUtils.readChar(reader);
				}
				else {
					// Game result mark
					for (GameResult result: GameResult.values()) {
						if (token.equalsIgnoreCase(result.toString()))
							return false;
					}
				}
			}
		}
		
		return true;
	}

	private void readCommentary(final PushbackReader reader) throws IOException {
		final StringBuilder builder = new StringBuilder();
		
		while (true) {
			final char ch = IoUtils.readChar(reader);
			
			if (ch == END_COMMENTARY_CHAR)
				break;
			
			builder.append(ch);
		}
		
		final ITreeIterator<IGameNode> lastIt = nodeStack.peek();
		lastIt.getItem().setCommentary(builder.toString());
	}
	
	private void processFen (final String value, final Game game) throws IOException {
		final StringReader stringReader = new StringReader(value);
		final PushbackReader pushbackReader = new PushbackReader(stringReader);
		final Fen fen = new Fen();
		
		fen.readFen(pushbackReader);
		game.newGame(fen.getPosition());
	}

	public static PushbackReader createPushbackReader(final InputStream stream) throws UnsupportedEncodingException {
		final InputStreamReader streamReader = new InputStreamReader(stream, PGN_ENCODING);
		final PushbackReader pushbackReader = new PushbackReader(streamReader);
		
		return pushbackReader;
	}
	
}
