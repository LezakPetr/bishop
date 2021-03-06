package bishop.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import bishop.base.Game;
import bishop.base.IGameNode;
import bishop.base.ITreeIterator;
import bishop.base.PgnReader;
import bishop.base.Position;

public final class BookSource implements IBook<BookRecord> {

	private final HashMap<Position, BookRecord> positionMap;
	
	
	public BookSource() {
		positionMap = new HashMap<>();
	}
	
	/**
	 * Finds record of given position.
	 * @param position position
	 * @return corresponding record or null if not found
	 */
	public BookRecord getRecord (final Position position) {
		return positionMap.get(position);
	}
	
	public void addRecord (final BookRecord record) {
		final Position position = record.getPosition();
		
		if (positionMap.containsKey(position))
			throw new RuntimeException("Dupplicate position");
		
		positionMap.put(position, record);
	}
	
	public void addGame (final Game game) {
		final Stack<ITreeIterator<IGameNode>> nodeStack = new Stack<>();
		nodeStack.add(game.getRootIterator());
		
		while (!nodeStack.isEmpty()) {
			final ITreeIterator<IGameNode> currentNode = nodeStack.pop();
			final List<BookMove> moveList = new LinkedList<>();
			
			if (currentNode.hasChild()) {
				final ITreeIterator<IGameNode> child = currentNode.copy();
				child.moveFirstChild();
				
				while (true) {
					final IGameNode node = child.getItem();
					final BookMove bookMove = new BookMove();
					
					bookMove.setMove(node.getMove().copy());
					
					moveList.add(bookMove);
					nodeStack.add(child.copy());
					
					if (child.hasNextSibling())
						child.moveNextSibling();
					else
						break;
				}
			}
			
			final Position position = currentNode.getItem().getTargetPosition().copy();
			BookRecord record = this.getRecord(position);
			
			if (record == null) {
				record = new BookRecord();
				record.getPosition().assign(position);
				
				addRecord(record);
			}
			
			for (BookMove bookMove: moveList) {
				record.addMove(bookMove);
			}
		}
	}
	
	public void addPgn(final PgnReader pgn) {
		for (Game game: pgn.getGameList()) {
			addGame (game);
		}
	}

	public void clear() {
		positionMap.clear();
	}
	
	public int getPositionCount() {
		return positionMap.size();
	}

	@Override
	public Collection<BookRecord> getAllRecords() {
		return positionMap.values();
	}
}
