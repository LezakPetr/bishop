package bishop.engine;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import bishop.base.BoardConstants;
import bishop.base.Color;

public class PawnStructureData {
	
	private PawnStructure structure;
	private final long[] data;
	
	private static final AtomicInteger OFFSET = new AtomicInteger();
	
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
	private static final int BACK_SQUARES_OFFSET = OFFSET.getAndAdd(Color.LAST);
	
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
	private static final int FRONT_SQUARES_OFFSET = OFFSET.getAndAdd(Color.LAST);

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
	private static final int NEIGHBOR_FRONT_SQUARES_OFFSET = OFFSET.getAndAdd(Color.LAST);
	
	// Mask of squares that are protected by own pawns, not occupied by own pawn and not attackable
	// by opponent pawns (there is no opponent pawn on left and right columns in front of the square).
	private static final int SECURE_SQUARES_OFFSET = OFFSET.getAndAdd(Color.LAST);
	
	// Pawns without opposite pawn in front of them on 3 neighbor files.
	private static final int PASSED_PAWNS_OFFSET = OFFSET.getAndAdd(Color.LAST);
	
	private static final int CONNECTED_PAWNS_OFFSET = OFFSET.getAndAdd(Color.LAST);
	private static final int ISOLATED_PAWNS_OFFSET = OFFSET.getAndAdd(Color.LAST);
	private static final int BACKWARDS_PAWNS_OFFSET = OFFSET.getAndAdd(Color.LAST);
	private static final int DOUBLED_PAWNS_OFFSET = OFFSET.getAndAdd(Color.LAST);
	
	private static final int DATA_SIZE = OFFSET.get();
	
	public PawnStructureData() {
		data = new long[DATA_SIZE];
	}

	public void clear() {
		Arrays.fill(data, 0);
	}
	
	public void assign(final PawnStructureData orig) {
		System.arraycopy(orig.data, 0, this.data, 0, DATA_SIZE);
	}

	private void fillOpenFileSquares(final PawnStructure structure) {
		this.structure = structure;
		
		// White
		long whiteNotFileOpenSquares = structure.getWhitePawnMask();
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 8;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 16;
		whiteNotFileOpenSquares |= whiteNotFileOpenSquares >>> 32;
		
		data[BACK_SQUARES_OFFSET + Color.WHITE] = whiteNotFileOpenSquares >>> 8;
		
		// Black
		long blackNotFileOpenSquares = structure.getBlackPawnMask();
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 8;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 16;
		blackNotFileOpenSquares |= blackNotFileOpenSquares << 32;
		
		data[BACK_SQUARES_OFFSET + Color.BLACK] = blackNotFileOpenSquares << 8;
	}

	private void fillOppositeFileAndAttackableSquares(final PawnStructure structure) {
		// White
		final long whitePawnSquares = structure.getWhitePawnMask();
		long whiteReachableSquares = whitePawnSquares;
		whiteReachableSquares |= whiteReachableSquares << 8;
		whiteReachableSquares |= whiteReachableSquares << 16;
		whiteReachableSquares |= whiteReachableSquares << 32;
		
		data[FRONT_SQUARES_OFFSET + Color.WHITE] = whiteReachableSquares << 8;
		data[NEIGHBOR_FRONT_SQUARES_OFFSET + Color.WHITE] = BoardConstants.getPawnsAttackedSquares(Color.WHITE, whiteReachableSquares & ~BoardConstants.RANK_18_MASK);
		
		// Black
		final long blackPawnSquares = structure.getBlackPawnMask();
		long blackReachableSquares = blackPawnSquares;
		blackReachableSquares |= blackReachableSquares >>> 8;
		blackReachableSquares |= blackReachableSquares >>> 16;
		blackReachableSquares |= blackReachableSquares >>> 32;
		
		data[FRONT_SQUARES_OFFSET + Color.BLACK] = blackReachableSquares >>> 8;
		data[NEIGHBOR_FRONT_SQUARES_OFFSET + Color.BLACK] = BoardConstants.getPawnsAttackedSquares(Color.BLACK, blackReachableSquares & ~BoardConstants.RANK_18_MASK);
	}
	
	private void fillSecureSquares(final PawnStructure structure) {
		final long notPawnsSquares = ~structure.getBothColorPawnMask();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long pawnsMask = structure.getPawnMask(color);
			final long attackedSquares = BoardConstants.getPawnsAttackedSquares(color, pawnsMask);
			
			data[SECURE_SQUARES_OFFSET + color] = attackedSquares & notPawnsSquares & ~data[NEIGHBOR_FRONT_SQUARES_OFFSET + oppositeColor];
		}
	}	
	
	public long getBackSquares(final int color) {
		return data[BACK_SQUARES_OFFSET + color];
	}

	public long getFrontSquares(final int color) {
		return data[FRONT_SQUARES_OFFSET + color];
	}

	public void calculate(final PawnStructure structure) {
		this.structure = structure;
		
		fillOpenFileSquares(structure);
		fillOppositeFileAndAttackableSquares(structure);
		fillSecureSquares(structure);
		calculatePawnTypes(structure);
	}
	
	private void calculatePawnTypes(final PawnStructure structure) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			
			final long ownPawnMask = structure.getPawnMask(color);
			final long openSquares = ~data[FRONT_SQUARES_OFFSET + oppositeColor];
			data[PASSED_PAWNS_OFFSET + color] = ownPawnMask & openSquares & ~data[NEIGHBOR_FRONT_SQUARES_OFFSET + oppositeColor];
			
			final long extendedConnectedSquareMask = BoardConstants.getAllConnectedPawnSquareMask(ownPawnMask) | BoardConstants.getPawnsAttackedSquares(Color.WHITE, ownPawnMask) | BoardConstants.getPawnsAttackedSquares(Color.BLACK, ownPawnMask);
			data[CONNECTED_PAWNS_OFFSET + color] = ownPawnMask & extendedConnectedSquareMask;
			
			final long frontOrBackSquares = data[FRONT_SQUARES_OFFSET + color] | data[BACK_SQUARES_OFFSET + color];
			final long occupiedFiles = frontOrBackSquares | ownPawnMask;
			data[ISOLATED_PAWNS_OFFSET + color] = ownPawnMask & ~BoardConstants.getAllConnectedPawnSquareMask(occupiedFiles);
			
			data[BACKWARDS_PAWNS_OFFSET + color] = ownPawnMask & openSquares & data[NEIGHBOR_FRONT_SQUARES_OFFSET + oppositeColor] & ~data[NEIGHBOR_FRONT_SQUARES_OFFSET + color];
			data[DOUBLED_PAWNS_OFFSET + color] = ownPawnMask & frontOrBackSquares;
		}
	}

	public long getSecureSquares (final int color) {
		return data[SECURE_SQUARES_OFFSET + color];
	}

	public PawnStructure getStructure() {
		return structure;
	}


}
