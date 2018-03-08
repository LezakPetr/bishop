package bishopTests;

import bishop.base.IMoveGenerator;
import bishop.base.LegalMoveFinder;
import bishop.base.MateChecker;
import bishop.base.Move;
import bishop.base.MoveList;
import bishop.base.MoveType;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.PseudoLegalMoveGenerator;

public class PerftCalculator {
	
	public static class Statistics {
		public long nodeCount;
		public long captureCount;
		public long epCount;
		public long castlingCount;
		public long promotionCount;
		public long checkCount;
		public long mateCount;
		
		public Statistics() {
		}
		
		public Statistics (final long nodeCount, final long captureCount, final long epCount, final long castlingCount, final long promotionCount, final long checkCount, final long mateCount) {
			this.nodeCount = nodeCount;
			this.captureCount = captureCount;
			this.epCount = epCount;
			this.castlingCount = castlingCount;
			this.promotionCount = promotionCount;
			this.checkCount = checkCount;
			this.mateCount = mateCount;
		}

		public void clear() {
			this.nodeCount = 0;
			this.captureCount = 0;
			this.epCount = 0;
			this.castlingCount = 0;
			this.promotionCount = 0;
			this.checkCount = 0;
			this.mateCount = 0;
		}

		public void add(final Statistics that) {
			this.nodeCount += that.nodeCount;
			this.captureCount += that.captureCount;
			this.epCount += that.epCount;
			this.castlingCount += that.castlingCount;
			this.promotionCount += that.promotionCount;
			this.checkCount += that.checkCount;
			this.mateCount += that.mateCount;
		}
	}

	
	private static final int MAX_DEPTH = 10;
	private static final int MOVE_STACK_CAPACITY = MAX_DEPTH * PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION;
	
	private final IMoveGenerator moveGenerator = new PseudoLegalMoveGenerator();
	private final Statistics[] statisticsStack = new Statistics[MAX_DEPTH];
	private final MoveList moveStack = new MoveList(MOVE_STACK_CAPACITY);
	private int moveStackTop;
	private int maxDepth;
	private final Position currentPosition = new Position();
	private final MateChecker mateChecker = new MateChecker();
	
	public PerftCalculator() {
		for (int i = 0; i < MOVE_STACK_CAPACITY; i++)
			moveStack.add(new Move());
		
		for (int i = 0; i < MAX_DEPTH; i++)
			statisticsStack[i] = new Statistics();
		
		moveGenerator.setPosition(currentPosition);
		moveGenerator.setWalker(m -> {
			moveStack.set(moveStackTop, m);
			moveStackTop++;
			
			return true;
		});
	}
	
	public Statistics getPerft (final Position position, final int depth) {
		currentPosition.assign(position);
		maxDepth = depth;
		
		calculatePerft(0);
		
		return statisticsStack[0];
	}
	
	private void calculatePerft(final int depth) {
		final int moveStackBegin = moveStackTop;
		moveGenerator.generateMoves();
		final int moveStackEnd = moveStackTop;
		
		statisticsStack[depth].clear();
		
		for (int i = moveStackBegin; i < moveStackEnd; i++) {
			final Move move = moveStack.get(i);
			currentPosition.makeMove(move);
			
			if (!currentPosition.isKingNotOnTurnAttacked()) {
				if (depth + 1 < maxDepth)
					calculatePerft(depth + 1);
				else
					setMoveStatistics (move, statisticsStack[depth + 1]);
					
				
				statisticsStack[depth].add(statisticsStack[depth + 1]);
			}
			
			currentPosition.undoMove(move);
		}
		
		moveStackTop = moveStackBegin;
	}
	
	private void setMoveStatistics (final Move move, final Statistics statistics) {
		statistics.clear();
		
		statistics.nodeCount = 1;
		
		if ((move.getCapturedPieceType() != PieceType.NONE))
			statistics.captureCount = 1;
		
		switch (move.getMoveType()) {
			case MoveType.PROMOTION:
				statistics.promotionCount++;
				break;
				
			case MoveType.EN_PASSANT:
				statistics.epCount++;
				break;

			case MoveType.CASTLING:
				statistics.castlingCount++;
				break;
		}
		
		if (currentPosition.isCheck()) {
			statistics.checkCount++;
			
			if (mateChecker.isMateInCheck(currentPosition))
				statistics.mateCount++;
		}
	}
	
}
