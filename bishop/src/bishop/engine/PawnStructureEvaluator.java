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
	private final long[] frontOpenFileSquares;
	
	// Contains 1 on squares without own pawn in back of them.
	// Back pawn itself is not considered open. 
	private final long[] backOpenFileSquares;
	
	private final long[] pawnAttackableSquares;
	
	// Mask of squares that are protected by own pawns, not occupied by own pawn and not attackable
	// by opponent pawns (there is no opponent pawn on left and right columns in front of the square). 
	private final long[] secureSquares;
	
	private final PawnStructureEvaluatorSettings settings;
	
	public PawnStructureEvaluator(final PawnStructureEvaluatorSettings settings) {
		this.settings = settings;
		
		frontOpenFileSquares = new long[Color.LAST];
		backOpenFileSquares = new long[Color.LAST];
		pawnAttackableSquares = new long[Color.LAST];
		secureSquares = new long[Color.LAST];
	}

	public void clear() {
		Arrays.fill(frontOpenFileSquares, 0);
		Arrays.fill(backOpenFileSquares, 0);
		Arrays.fill(pawnAttackableSquares, 0);
		Arrays.fill(secureSquares, 0);
	}

	private void fillOpenFileSquares(final Position position) {
		// White
		long whiteNotFileOpenSquares = position.getPiecesMask(Color.WHITE, PieceType.PAWN);
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 8;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 16;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 32;
		
		frontOpenFileSquares[Color.WHITE] = ~(whiteNotFileOpenSquares >>> 8);
		
		// Black
		long blackNotFileOpenSquares = position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 8;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 16;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 32;
		
		frontOpenFileSquares[Color.BLACK] = ~(blackNotFileOpenSquares << 8);
	}

	private void fillOppositeFileAndAttackableSquares(final Position position) {
		// White
		final long whitePawnSquares = position.getPiecesMask(Color.WHITE, PieceType.PAWN);
		long whiteReachableSquares = whitePawnSquares;
		whiteReachableSquares |= whiteReachableSquares << 8;
		whiteReachableSquares |= whiteReachableSquares << 16;
		whiteReachableSquares |= whiteReachableSquares << 32;
		
		backOpenFileSquares[Color.WHITE] = ~whiteReachableSquares;
		pawnAttackableSquares[Color.WHITE] = BoardConstants.getPawnsAttackedSquares(Color.WHITE, (whitePawnSquares | whiteReachableSquares) & ~BoardConstants.RANK_18_MASK);
		
		// Black
		final long blackPawnSquares = position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		long blackReachableSquares = blackPawnSquares;
		blackReachableSquares |= blackReachableSquares >>> 8;
		blackReachableSquares |= blackReachableSquares >>> 16;
		blackReachableSquares |= blackReachableSquares >>> 32;
		
		backOpenFileSquares[Color.BLACK] = ~blackReachableSquares;
		pawnAttackableSquares[Color.BLACK] = BoardConstants.getPawnsAttackedSquares(Color.BLACK, (blackPawnSquares | blackReachableSquares) & ~BoardConstants.RANK_18_MASK);
	}
	
	private void fillSecureSquares(final Position position) {
		final long notPawnsSquares = ~position.getBothColorPiecesMask(PieceType.PAWN);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long pawnsMask = position.getPiecesMask(color, PieceType.PAWN);
			final long attackedSquares = BoardConstants.getPawnsAttackedSquares(color, pawnsMask);
			
			secureSquares[color] = attackedSquares & notPawnsSquares & ~pawnAttackableSquares[oppositeColor];
		}
	}
	
	private int evaluateUnprotectedOpenFilePawns(final Position position, final AttackCalculator attackCalculator) {
		int unprotectedOpenFilePawnsEvaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long oppositeRookMask = position.getPiecesMask(oppositeColor, PieceType.ROOK);
			
			if (oppositeRookMask != 0) {
				final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
				final long unprotectedPawnMask = ownPawnMask & ~attackCalculator.getPawnAttackedSquares(color);
				final long unprotectedOpenPawnMask = unprotectedPawnMask & backOpenFileSquares[oppositeColor];
				
				unprotectedOpenFilePawnsEvaluation += BitBoard.getSquareCount(unprotectedOpenPawnMask) * settings.getUnprotectedOpenFilePawnBonus(color);
			}
		}
		
		return unprotectedOpenFilePawnsEvaluation;
	}
	
	private int evaluatePawnStructure(final Position position, final AttackCalculator attackCalculator) {
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
		
		return evaluation;
	}
	
	public long getOpenFileSquares(final int color) {
		return frontOpenFileSquares[color];
	}

	public long getOppositeFileSquares(final int color) {
		return backOpenFileSquares[color];
	}

	public void calculate(final Position position) {
		fillOpenFileSquares(position);
		fillOppositeFileAndAttackableSquares(position);
		fillSecureSquares(position);
	}
	
	public int evaluate(final Position position, final AttackCalculator attackCalculator) {
		int evaluation = 0;
		
		evaluation += evaluatePawnStructure(position, attackCalculator);
		evaluation += evaluateUnprotectedOpenFilePawns(position, attackCalculator);
		
		return evaluation;		
	}

	public void writeLog(final PrintWriter writer) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Front open file squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, frontOpenFileSquares[color]);
			writer.println();			
		}
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Back open file squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, backOpenFileSquares[color]);
			writer.println();			
		}
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Secure squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, secureSquares[color]);
			writer.println();			
		}
	}
	
	public long getSecureSquares (final int color) {
		return secureSquares[color];
	}
}
