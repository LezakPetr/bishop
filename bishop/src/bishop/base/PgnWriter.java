package bishop.base;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Stack;

import utils.IoUtils;

public class PgnWriter extends Pgn {
	
	private final StandardAlgebraicNotationWriter sanWriter = new StandardAlgebraicNotationWriter();
	private PrintWriter writer;
	private PrintWriter moveWriter;
	private boolean isNewVariation;
	
	private static final int MOVE_LINE_LENGTH = 80;
	
	
	private abstract class StackRecord {
		// Processes this record.
		public abstract void process();
	}
	
	private class GameNodeRecord extends StackRecord {
		private final ITreeIterator<IGameNode> nodeIterator;
		
		public GameNodeRecord (final ITreeIterator<IGameNode> nodeIterator) {
			this.nodeIterator = nodeIterator;
		}
		
		// Processes this record.
		public void process() {
			final IGameNode currentNode = nodeIterator.getItem();
			
			final ITreeIterator<IGameNode> parentIterator = nodeIterator.copy();
			parentIterator.moveParent();
			
			final Position beginPosition = parentIterator.getItem().getTargetPosition();
			writeOneMove(beginPosition, currentNode);
	
			pushChild();
			pushNextSibling();
		}
		
		private void pushChild() {
			if (nodeIterator.hasChild()) {
				final ITreeIterator<IGameNode> childIterator = nodeIterator.copy();
				childIterator.moveFirstChild();
				
				nodeStack.push(new GameNodeRecord (childIterator));
			}
		}
		
		private void pushNextSibling() {
			if (nodeIterator.hasNextSibling()) {
				moveWriter.write("( ");
				isNewVariation = true;
				
				nodeStack.push(new RightBracketNodeRecord());
				
				final ITreeIterator<IGameNode> siblingIterator = nodeIterator.copy();
				siblingIterator.moveNextSibling();
				
				nodeStack.push(new GameNodeRecord (siblingIterator));
			}
		}
	}
	
	private class RightBracketNodeRecord extends StackRecord {
		// Processes this record.
		public void process() {
			moveWriter.write(") ");
			isNewVariation = true;
		}
	}
	
	
	protected final Stack<StackRecord> nodeStack = new Stack<>();
	
	
	private void writeTag (final String name, final String value) {
		writer.print(BEGIN_TAG_CHAR);
		writer.print(name);
		
		writer.print(" ");
		
		IoUtils.writeQuotedString(writer, value);
		
		writer.println("" + END_TAG_CHAR);
	}

	private void writeHeader (final Game game) {
		final GameHeader header = game.getHeader();
			
		writeTag(TAG_EVENT, header.getEvent());
		writeTag(TAG_SITE, header.getSite());
		writeTag(TAG_DATE, header.getDate());
		writeTag(TAG_ROUND, header.getRound());
		writeTag(TAG_WHITE, header.getWhite());
		writeTag(TAG_BLACK, header.getBlack());
		writeTag(TAG_RESULT, header.getResult().toString());
		
		final Position startPosition = game.getRootIterator().getItem().getTargetPosition();
		
		if (!startPosition.equals(Position.INITIAL_POSITION)) {
			writeFen (startPosition);
		}
	}
	
	private void writeMoveNumber (final Position beginPosition, final IGameNode node) {
		moveWriter.print(node.getMoveNumber());
		
		if (beginPosition.getOnTurn() == Color.WHITE)
			moveWriter.write(". ");
		else
			moveWriter.write("... ");
	}
	
	private void writeOneMove (final Position beginPosition, final IGameNode currentNode) {
		if (beginPosition.getOnTurn() == Color.WHITE || isNewVariation) {
			writeMoveNumber(beginPosition, currentNode);
			isNewVariation = false;
		}
		
		sanWriter.writeMove(moveWriter, beginPosition, currentNode.getMove());
		moveWriter.write(' ');
		
		writeAnnotation(currentNode);
		writeCommentary(currentNode);
	}

	private void writeAnnotation(final IGameNode currentNode) {
		final Annotation annotation = currentNode.getAnnotation();
		
		if (annotation != null && annotation != Annotation.NONE) {
			moveWriter.write(ANNOTATION_MARK);
			moveWriter.write(Integer.toString(annotation.getNag()));
			moveWriter.write(' ');
		}
	}
	
	private void writeCommentary(final IGameNode currentNode) {
		final String commentary = currentNode.getCommentary();
		
		if (commentary != null && !commentary.isEmpty()) {
			moveWriter.write(BEGIN_COMMENTARY_CHAR);
			moveWriter.write(commentary);
			moveWriter.write(END_COMMENTARY_CHAR);
			moveWriter.write(' ');
		}
	}
	
	private void writeMoves (final Game game) {
		final ITreeIterator<IGameNode> rootIterator = game.getRootIterator();
		nodeStack.clear();
		
		if (rootIterator.hasChild()) {
			rootIterator.moveFirstChild();
			
			nodeStack.push(new GameNodeRecord(rootIterator));
		}
		
		isNewVariation = true;
		
		while (!nodeStack.empty()) {
			final StackRecord currentRecord = nodeStack.pop();
			currentRecord.process();
		}
	}
	
	public void writePgnToStream (final OutputStream stream) throws IOException {
		final OutputStreamWriter streamWriter = new OutputStreamWriter(stream, PGN_ENCODING);
		final PrintWriter printWriter = new PrintWriter(streamWriter);
		
		writePgn(printWriter);
		
		printWriter.flush();
		streamWriter.flush();
	}
	
	public void writePgn (final PrintWriter writer) {
		this.writer = writer;
		this.moveWriter = new PrintWriter(new LineBreakWriter(writer, MOVE_LINE_LENGTH));
		
		try {
			for (Game game: gameList) {
				writeHeader(game);
				writer.println();
				
				writeMoves(game);
				moveWriter.flush();
				
				writer.println(game.getHeader().getResult().toString());
				writer.println();
			}
		}
		finally {
			this.writer = null;
			this.moveWriter = null;
		}
	}

	private void writeFen(final Position startPosition) {
		final Fen fen = new Fen();
		fen.setPosition(startPosition);
		
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		
		fen.writeFen(printWriter);
		printWriter.flush();
		
		writeTag(TAG_FEN, stringWriter.toString());
	}

}
