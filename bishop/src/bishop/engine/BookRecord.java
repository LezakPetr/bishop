package bishop.engine;

import bishop.base.Move;
import bishop.base.Position;
import bishop.base.StandardAlgebraicNotationWriter;
import utils.IntUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class BookRecord {
	
	private static final int MIN_RELATIVE_MOVE_REPETITION = 2;   // Minimal relative occurrence of move in a position to be considered as a good move [%]
	private static final int MIN_BALANCE_DIFFERENCE = -20;   // Minimal difference between balances of source and target positions so the move is considered as a good one [%]
	
	public static final int MAX_REPETITION_COUNT = 1000;   // Maximal stored repetition count, larger counts are saturated on that value


	private final Position position;
	private int repetitionCount;
	private int balance;
	private final Map<Move, BookMove> moveMap;
	
	public BookRecord() {
		position = new Position();
		moveMap = new HashMap<>();
	}

	public Position getPosition() {
		return position;
	}
	
	public BookMove findMove (final Move move) {
		return moveMap.get(move);
	}
	
	public int getMoveCount() {
		return moveMap.size();
	}
	
	public void addMove (final BookMove bookMove) {
		moveMap.putIfAbsent(bookMove.getMove(), bookMove);
	}
	
	public boolean isGoodMove(final BookMove move) {
		return move.getRelativeMoveRepetition() >= MIN_RELATIVE_MOVE_REPETITION &&
				-move.getTargetPositionBalance() - balance >= MIN_BALANCE_DIFFERENCE;
	}

	/**
	 * Returns random good move.
	 * Uses round-robin algorithm to chose random move from list of good moves.
	 * @param random random number generator
	 * @return move
	 */
	public BookMove getRandomMove(final Random random) {
		final List<BookMove> goodMoveList = moveMap.values().stream()
				.filter(this::isGoodMove)
				.collect(Collectors.toList());
		
		final int moveCount = goodMoveList.size();
		
		if (moveCount <= 0)
			return null;
		
		final int repetitionSum = goodMoveList.stream().mapToInt(BookMove::getRelativeMoveRepetition).sum();
		final int r = random.nextInt(repetitionSum);
		
		int partialSum = 0;
		
		for (BookMove move: goodMoveList) {
			partialSum += move.getRelativeMoveRepetition();
			
			if (r < partialSum)
				return move;
		}
		
		throw new RuntimeException("Internal error in getRandomMove");
	}
	
	public void assign(final BookRecord orig) {
		this.position.assign(orig.position);
		
		this.moveMap.clear();
		
		for (BookMove move: orig.moveMap.values()) {
			final BookMove copyMove = move.copy();
			
			this.moveMap.put(copyMove.getMove(), copyMove);
		}
	}

	public void removeAllMoves() {
		this.moveMap.clear();
	}

	public Map<Move, BookMove> getMoveMap() {
		return Collections.unmodifiableMap(moveMap);
	}

	public int getBalance() {
		return this.balance;
	}

	public void setBalance(final int balance) {
		this.balance = balance;
	}

	public void logRecord(final List<String> additionalInfo) {
		final String repetitionRelation = (repetitionCount == MAX_REPETITION_COUNT) ? ">=" : "=";
		additionalInfo.add("Repetitions " + repetitionRelation + " " + repetitionCount + ", balance = " + balance + "%");
		
		final List<BookMove> moveList = moveMap.values().stream()
				.sorted((a, b) -> b.getRelativeMoveRepetition() - a.getRelativeMoveRepetition())
				.collect(Collectors.toList());
		
		final StringWriter moveLog = new StringWriter();
		final PrintWriter moveLogWriter = new PrintWriter(moveLog);
		final StandardAlgebraicNotationWriter notationWriter = new StandardAlgebraicNotationWriter();
		
		for (BookMove move: moveList) {
			notationWriter.writeMove(moveLogWriter, position, move.getMove());
			moveLogWriter.print(" ");
			moveLogWriter.print(move.getRelativeMoveRepetition());
			moveLogWriter.print("% ");
			moveLogWriter.print(IntUtils.intToStringWithSignum(-move.getTargetPositionBalance()));
			moveLogWriter.print("%; ");
		}

		moveLogWriter.flush();
		additionalInfo.add(moveLog.toString());
	}
	
	@Override
	public boolean equals (final Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		
		final BookRecord that = (BookRecord) obj;
		
		return this.position.equals(that.position)
				&& this.repetitionCount == that.repetitionCount
				&& this.balance == that.balance
				&& this.moveMap.equals(that.moveMap);
	}
	
	@Override
	public int hashCode() {
		int hash = position.hashCode();
		hash = 31 * hash + repetitionCount;
		hash = 31 * hash + balance;
		hash = 31 * hash + moveMap.hashCode();
		
		return hash;
	}
	
	@Override
	public String toString() {
		return position.toString() + " " + repetitionCount + " " + balance + " " + moveMap.toString();
	}

	public int getRepetitionCount() {
		return repetitionCount;
	}

	public void setRepetitionCount(final long repetitionCount) {
		this.repetitionCount = (int) Math.min(repetitionCount, MAX_REPETITION_COUNT);
	}

}
