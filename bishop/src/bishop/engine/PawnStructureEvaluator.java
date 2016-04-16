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
	// Contains 1 on squares behind pawns.
	// Direction is relative.
	// White pawn on e4: 
	//   +-----------------+
	// 8 |                 |
	// 7 |                 |
	// 6 |                 |
	// 5 |                 |
	// 4 |         P       |
	// 3 |         *       |
	// 2 |         *       |
	// 1 |         *       |
	//   +-----------------+
	//     a b c d e f g h
	private final long[] backSquares;
	
	// Contains 1 on squares in front of pawns.
	// Direction is relative.
	// White pawn on e4: 
	//   +-----------------+
	// 8 |         *       |
	// 7 |         *       |
	// 6 |         *       |
	// 5 |         *       |
	// 4 |         P       |
	// 3 |                 |
	// 2 |                 |
	// 1 |                 |
	//   +-----------------+
	//     a b c d e f g h
	private final long[] frontSquares;

	// Mask of squares attackable by pawn of given color.
	// White pawn on e4: 
	//   +-----------------+
	// 8 |       *   *     |
	// 7 |       *   *     |
	// 6 |       *   *     |
	// 5 |       *   *     |
	// 4 |         P       |
	// 3 |                 |
	// 2 |                 |
	// 1 |                 |
	//   +-----------------+
	//     a b c d e f g h
	private final long[] neighborFrontSquares;
	
	// Mask of squares that are protected by own pawns, not occupied by own pawn and not attackable
	// by opponent pawns (there is no opponent pawn on left and right columns in front of the square). 
	private final long[] secureSquares;
	
	// Pawns without opposite pawn in front of them on 3 neighbor files.
	private final long[] passedPawns;
	
	private final long[] connectedPawns;
	private final long[] isolatedPawns;
	private final long[] backwardPawns;
	private final long[] doubledPawns;
	
	private final PawnStructureEvaluatorSettings settings;
	
	public PawnStructureEvaluator(final PawnStructureEvaluatorSettings settings) {
		this.settings = settings;
		
		backSquares = new long[Color.LAST];
		frontSquares = new long[Color.LAST];
		neighborFrontSquares = new long[Color.LAST];
		secureSquares = new long[Color.LAST];
		passedPawns = new long[Color.LAST];
		connectedPawns = new long[Color.LAST];
		isolatedPawns = new long[Color.LAST];
		backwardPawns = new long[Color.LAST];
		doubledPawns = new long[Color.LAST];
	}

	public void clear() {
		Arrays.fill(backSquares, 0);
		Arrays.fill(frontSquares, 0);
		Arrays.fill(neighborFrontSquares, 0);
		Arrays.fill(secureSquares, 0);
	}

	private void fillOpenFileSquares(final Position position) {
		// White
		long whiteNotFileOpenSquares = position.getPiecesMask(Color.WHITE, PieceType.PAWN);
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 8;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 16;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 32;
		
		backSquares[Color.WHITE] = whiteNotFileOpenSquares >>> 8;
		
		// Black
		long blackNotFileOpenSquares = position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 8;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 16;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 32;
		
		backSquares[Color.BLACK] = blackNotFileOpenSquares << 8;
	}

	private void fillOppositeFileAndAttackableSquares(final Position position) {
		// White
		final long whitePawnSquares = position.getPiecesMask(Color.WHITE, PieceType.PAWN);
		long whiteReachableSquares = whitePawnSquares;
		whiteReachableSquares |= whiteReachableSquares << 8;
		whiteReachableSquares |= whiteReachableSquares << 16;
		whiteReachableSquares |= whiteReachableSquares << 32;
		
		frontSquares[Color.WHITE] = whiteReachableSquares << 8;
		neighborFrontSquares[Color.WHITE] = BoardConstants.getPawnsAttackedSquares(Color.WHITE, whiteReachableSquares & ~BoardConstants.RANK_18_MASK);
		
		// Black
		final long blackPawnSquares = position.getPiecesMask(Color.BLACK, PieceType.PAWN);
		long blackReachableSquares = blackPawnSquares;
		blackReachableSquares |= blackReachableSquares >>> 8;
		blackReachableSquares |= blackReachableSquares >>> 16;
		blackReachableSquares |= blackReachableSquares >>> 32;
		
		frontSquares[Color.BLACK] = blackReachableSquares >>> 8;
		neighborFrontSquares[Color.BLACK] = BoardConstants.getPawnsAttackedSquares(Color.BLACK, blackReachableSquares & ~BoardConstants.RANK_18_MASK);
	}
	
	private void fillSecureSquares(final Position position) {
		final long notPawnsSquares = ~position.getBothColorPiecesMask(PieceType.PAWN);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long pawnsMask = position.getPiecesMask(color, PieceType.PAWN);
			final long attackedSquares = BoardConstants.getPawnsAttackedSquares(color, pawnsMask);
			
			secureSquares[color] = attackedSquares & notPawnsSquares & ~neighborFrontSquares[oppositeColor];
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
				final long unprotectedOpenPawnMask = unprotectedPawnMask & ~frontSquares[oppositeColor];
				
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
	
	public long getBackSquares(final int color) {
		return backSquares[color];
	}

	public long getFrontSquares(final int color) {
		return frontSquares[color];
	}

	public void calculate(final Position position) {
		fillOpenFileSquares(position);
		fillOppositeFileAndAttackableSquares(position);
		fillSecureSquares(position);
		calculatePawnTypes(position);
	}
	
	private void calculatePawnTypes(final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			
			final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
			final long openSquares = ~frontSquares[oppositeColor];
			passedPawns[color] = ownPawnMask & openSquares & ~neighborFrontSquares[oppositeColor];
			
			final long extendedConnectedSquareMask = BoardConstants.getAllConnectedPawnSquareMask(ownPawnMask) | BoardConstants.getPawnsAttackedSquares(Color.WHITE, ownPawnMask) | BoardConstants.getPawnsAttackedSquares(Color.BLACK, ownPawnMask);
			connectedPawns[color] = ownPawnMask & extendedConnectedSquareMask;
			
			final long frontOrBackSquares = frontSquares[color] | backSquares[color];
			final long occupiedFiles = frontOrBackSquares | ownPawnMask;
			isolatedPawns[color] = ownPawnMask & ~BoardConstants.getAllConnectedPawnSquareMask(occupiedFiles);
			
			backwardPawns[color] = ownPawnMask & openSquares & neighborFrontSquares[oppositeColor] & ~neighborFrontSquares[color];
			doubledPawns[color] = ownPawnMask & frontOrBackSquares;
		}
	}

	public int evaluate(final Position position, final AttackCalculator attackCalculator) {
		int evaluation = 0;
		
		evaluation += evaluatePawnStructure(position, attackCalculator);
		evaluation += evaluateUnprotectedOpenFilePawns(position, attackCalculator);
		
		return evaluation;		
	}

	public void writeLog(final PrintWriter writer) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Back squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, backSquares[color]);
			writer.println();			
		}
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			writer.print("Front squares " + Color.getName(color) + ": ");
			BitBoard.write(writer, frontSquares[color]);
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
