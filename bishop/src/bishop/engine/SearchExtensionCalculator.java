package bishop.engine;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.Move;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.tables.FigureAttackTable;

public class SearchExtensionCalculator {
	
	
	private static final int MAX_NULL_MATE_DEPTH = 3;
	private static final int MIN_NULL_MATE_DEPTH = 1;
	private static final int NULL_MATE_REDUCTION = 2;
	
	private static final int MIN_KING_ESCAPE_SQUARE_COUNT = 1;
	private static final int MAX_KING_ESCAPE_SQUARE_COUNT = 3;
	
	private final MateFinder finder;
	private SearchSettings settings;
	
	private int[] checkExtensionByEscapeSquares = new int[MAX_KING_ESCAPE_SQUARE_COUNT + 1];
	
	public SearchExtensionCalculator() {
		finder = new MateFinder();
		finder.setMaxDepth(MAX_NULL_MATE_DEPTH, 0, 0);
	}
	
	public int getExtension (final Position position, final boolean isCheck, final HashRecord hashRecord, final int horizon, final AttackCalculator attackCalculator) {
		if (isCheck) {
			final int freeSquareCount;
			
			if (isDoubleCheck(position))
				freeSquareCount = 0;
			else
				freeSquareCount = getKingEscapeSquareCount(position);
			
			return checkExtensionByEscapeSquares[freeSquareCount];
		}
		else {
			if (isNullMate(position, horizon))
				return settings.getMateExtension();
			
			if (isRankAttack (position))
				return settings.getRankAttackExtension();
			
			if (attackCalculator.isPin())
				return settings.getPinExtension();
			
			return 0;
		}
	}

	private boolean isNullMate(final Position position, final int horizon) {
		final int optimalDepthInMoves = (horizon >> (ISearchEngine.HORIZON_FRACTION_BITS + 1)) - NULL_MATE_REDUCTION;
		final int depthInMoves = Math.min(Math.max(optimalDepthInMoves, MIN_NULL_MATE_DEPTH), MAX_NULL_MATE_DEPTH);
		
		final Move move = new Move();
		move.createNull(position.getCastlingRights().getIndex(), position.getEpFile());
		
		position.makeMove(move);
		
		finder.setPosition(position);
		final boolean isWin = finder.findWin(depthInMoves) >= Evaluation.MATE_MIN;
		
		position.undoMove(move);
		
		return isWin;
	}

	private boolean isDoubleCheck(final Position position) {
		final int onTurn = position.getOnTurn();
		final int notOnTurn = Color.getOppositeColor(onTurn);
		final int kingSquare = position.getKingPosition(onTurn);
		final int attackCount = position.getCountOfAttacks(notOnTurn, kingSquare);
		
		return attackCount >= 2;
	}

	private int getKingEscapeSquareCount(final Position position) {
		final int onTurn = position.getOnTurn();
		final int notOnTurn = Color.getOppositeColor(onTurn);
		final int kingSquare = position.getKingPosition(onTurn);
		final long nearKingSquares = FigureAttackTable.getItem(PieceType.KING, kingSquare);
		final long ownOccupancy = position.getColorOccupancy(onTurn);
		int escapeSquareCount = 0;
		
		for (BitLoop loop = new BitLoop(nearKingSquares); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			
			if ((ownOccupancy & BitBoard.getSquareMask(square)) == 0 && !position.isSquareAttacked(notOnTurn, square))
				escapeSquareCount++;
			
			if (escapeSquareCount >= MAX_KING_ESCAPE_SQUARE_COUNT)
				break;
		}
		
		return escapeSquareCount;
	}
	
	private boolean isRankAttack(final Position position) {
		final int onTurn = position.getOnTurn();
		final int notOnTurn = Color.getOppositeColor(onTurn);
		final long firstRankMask = BoardConstants.getFirstRankMask(onTurn);
		final long secondRankMask = BoardConstants.getSecondRankMask(onTurn);
		final long ownKingMask = position.getPiecesMask(onTurn, PieceType.KING);
		
		if ((ownKingMask & (firstRankMask | secondRankMask)) == 0)
			return false;
			
		final long oppositeQueenMask = position.getPiecesMask(notOnTurn, PieceType.QUEEN);
		final long oppositeRookMask = position.getPiecesMask(notOnTurn, PieceType.ROOK);
		final long heavyFigureMask = oppositeQueenMask | oppositeRookMask;
		
		return BitBoard.getSquareCount(heavyFigureMask & firstRankMask) >= 2 ||
		       BitBoard.getSquareCount(heavyFigureMask & secondRankMask) >= 2;
	}
	
	public void setSearchSettings(final SearchSettings settings) {
		this.settings = settings;
		
		final double y1 = settings.getAttackCheckExtension();
		final double y2 = settings.getSimpleCheckExtension();
		
		for (int i = 0; i <= MAX_KING_ESCAPE_SQUARE_COUNT; i++) {
			final int count = Math.max(i, MIN_KING_ESCAPE_SQUARE_COUNT);
			final double extension = y1 + (y2 - y1) * (count - MIN_KING_ESCAPE_SQUARE_COUNT) / (MAX_KING_ESCAPE_SQUARE_COUNT - MIN_KING_ESCAPE_SQUARE_COUNT);
			
			checkExtensionByEscapeSquares[i] = (int) Math.round(extension);
		}
	}
}
