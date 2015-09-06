package bishop.engine;

import java.io.PrintWriter;
import java.util.Arrays;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Square;
import bishop.tables.FrontSquaresOnSameFileTable;
import bishop.tables.PawnAttackTable;

public class PawnStructureEvaluator {
	// Contains 1 on squares without own pawn in front of them.
	// Front pawn itself is also considered open. 
	private final long[] openFileSquares;
	
	private final long[] oppositeFileSquares;
	private final PawnStructureEvaluatorSettings settings;
	
	public PawnStructureEvaluator(final PawnStructureEvaluatorSettings settings) {
		this.settings = settings;
		
		openFileSquares = new long[Color.LAST];
		oppositeFileSquares = new long[Color.LAST];
	}

	public void clear() {
		Arrays.fill(openFileSquares, 0);
		Arrays.fill(oppositeFileSquares, 0);
	}

	private void fillOpenFileSquares(final Position position) {
		// White
		long whiteNotFileOpenSquares = position.getPiecesMask(Color.WHITE, PieceType.PAWN);
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 8;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 16;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 32;
		
		openFileSquares[Color.WHITE] = ~(whiteNotFileOpenSquares >>> 8);
		
		// Black
		long blackNotFileOpenSquares = position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 8;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 16;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 32;
		
		openFileSquares[Color.BLACK] = ~(blackNotFileOpenSquares << 8);
	}

	private void fillOppositeFileSquares(final Position position) {
		// White
		long whiteNotFileSquares = position.getPiecesMask(Color.WHITE, PieceType.PAWN);
		whiteNotFileSquares |= whiteNotFileSquares << 8;
		whiteNotFileSquares |= whiteNotFileSquares << 16;
		whiteNotFileSquares |= whiteNotFileSquares << 32;
		
		oppositeFileSquares[Color.WHITE] = ~whiteNotFileSquares;
		
		// Black
		long blackNotFileSquares = position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		blackNotFileSquares |= blackNotFileSquares >>> 8;
		blackNotFileSquares |= blackNotFileSquares >>> 16;
		blackNotFileSquares |= blackNotFileSquares >>> 32;
		
		oppositeFileSquares[Color.BLACK] = ~blackNotFileSquares;
	}
	
	private int evaluateUnprotectedOpenFilePawns(final Position position, final AttackCalculator attackCalculator) {
		int unprotectedOpenFilePawnsEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long oppositeRookMask = position.getPiecesMask(oppositeColor, PieceType.ROOK);
			
			if (oppositeRookMask != 0) {
				final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
				final long unprotectedPawnMask = ownPawnMask & ~attackCalculator.getPawnAttackedSquares(color);
				final long unprotectedOpenPawnMask = unprotectedPawnMask & oppositeFileSquares[oppositeColor];
				
				unprotectedOpenFilePawnsEvaluation += BitBoard.getSquareCount(unprotectedOpenPawnMask) * settings.getUnprotectedOpenFilePawnBonus(color);
			}
		}
		
		return unprotectedOpenFilePawnsEvaluation;
	}
	
	private int evaluateDoublePawns(final Position position) {
		int doublePawnEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
			final long doublePawns = ownPawnMask & ~openFileSquares[color];
			doublePawnEvaluation += BitBoard.getSquareCount(doublePawns) * settings.getDoublePawnBonus(color);
		}
		
		return doublePawnEvaluation;
	}
	
	public long getOpenFileSquares(final int color) {
		return openFileSquares[color];
	}

	public long getOppositeFileSquares(final int color) {
		return oppositeFileSquares[color];
	}

	public void calculate(final Position position) {
		fillOpenFileSquares(position);
		fillOppositeFileSquares(position);
	}
	
	public int evaluate(final Position position, final AttackCalculator attackCalculator) {
		int evaluation = 0;
		
		final long whiteRookMask = position.getPiecesMask(Color.WHITE, PieceType.ROOK);
		final long blackRookMask = position.getPiecesMask(Color.BLACK, PieceType.ROOK);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			
			final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
			final long oppositePawnMask = position.getPiecesMask(oppositeColor, PieceType.PAWN);
			
			for (BitLoop loop = new BitLoop(ownPawnMask); loop.hasNextSquare(); ) {
				final int square = loop.getNextSquare();
				final int rank = Square.getRank(square);
				final long frontSquaresOnThreeFiles = BoardConstants.getFrontSquaresOnThreeFiles(color, square);
				final long frontSquaresOnSameFile = FrontSquaresOnSameFileTable.getItem(color, square);
				final long rearSquaresOnSameFile = FrontSquaresOnSameFileTable.getItem(oppositeColor, square);
				
				// Passed pawn
				if ((oppositePawnMask & frontSquaresOnThreeFiles) == 0) {
					final long neighbourSquares = BoardConstants.getConnectedPawnSquareMask(square);
					
					if ((ownPawnMask & neighbourSquares) != 0) {
						evaluation += settings.getConnectedPassedPawnBonus (color, square);
					}
					else {
						final long protectingSquares = PawnAttackTable.getItem(oppositeColor, square);
						
						if ((ownPawnMask & protectingSquares) != 0) {
							evaluation += settings.getProtectedPassedPawnBonus(color, square);
						}
						else {
							evaluation += settings.getSinglePassedPawnBonus(color, square);
						}
					}
					
					// Pawn with rooks
					if ((whiteRookMask & rearSquaresOnSameFile) != 0 && (blackRookMask & frontSquaresOnSameFile) != 0) {
						evaluation += settings.getRookPawnBonus(color, rank);
					}
					
					if ((blackRookMask & rearSquaresOnSameFile) != 0 && (whiteRookMask & frontSquaresOnSameFile) != 0) {
						evaluation -= settings.getRookPawnBonus(color, rank);
					}
				}
				
				// Double pawn
				if ((frontSquaresOnSameFile & ownPawnMask) != 0) {
					evaluation += settings.getDoublePawnBonus(color);
				}
			}
		}
		
		evaluation += evaluateDoublePawns(position);
		
		return evaluation;		
	}

	private int evaluatePawn(final Position position, final int color, final int square) {
		int evaluation = 0;
		
		final int oppositeColor = Color.getOppositeColor(color);
		final long ownPawns = position.getPiecesMask(color, PieceType.PAWN);
		final long oppositePawns = position.getPiecesMask(oppositeColor, PieceType.PAWN);
		final boolean isProtected = (ownPawns & PawnAttackTable.getItem(oppositeColor, square)) != 0;
		final boolean isConnected = (ownPawns & BoardConstants.getConnectedPawnSquareMask(square)) != 0;
		final boolean isFree = (oppositePawns & BoardConstants.getFrontSquaresOnThreeFiles(color, square)) != 0;
		
		
		
		return evaluation;
	}

	public void writeLog(final PrintWriter writer) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Open file squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, openFileSquares[color]);
			writer.println();			
		}
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Opposite file squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, oppositeFileSquares[color]);
			writer.println();			
		}
	}
}
