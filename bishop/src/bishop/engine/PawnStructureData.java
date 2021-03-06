package bishop.engine;

import java.util.Arrays;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.File;
import bishop.tables.PawnIslandFileTable;
import utils.IntArrayBuilder;
import utils.IntHolder;

public class PawnStructureData {
	
	private long whitePawnMask;
	private long blackPawnMask;
	private final long[] data;
	
	private static final IntHolder OFFSET = new IntHolder();
	
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
	
	private static final int SINGLE_DISADVANTAGE_ATTACK_PAWNS_OFFSET = OFFSET.getAndAdd(Color.LAST);
	private static final int DOUBLE_DISADVANTAGE_ATTACK_PAWNS_OFFSET = OFFSET.getAndAdd(Color.LAST);
	private static final int BLOCKED_PAWNS_OFFSET = OFFSET.getAndAdd(Color.LAST);
	
	private static final int PAWN_ISLANDS_OFFSET = OFFSET.getAndAdd(1);
	
	private static final int DATA_SIZE = OFFSET.getValue();
	
	
	private static final int PAWN_ISLANDS_ITEM_SHIFT = 8;
	private static final int PAWN_ISLANDS_COUNT_MASK = 0x7F;
	private static final int PAWN_ISLANDS_ALIVE_MASK = 0X80;
	public static final int MAX_PAWN_ISLAND_COUNT = 4;
	
	private static final int[] PAWN_FILES_OFFSETS = new IntArrayBuilder(Color.LAST)
		.put(Color.WHITE, BACK_SQUARES_OFFSET + Color.WHITE)
		.put(Color.BLACK, FRONT_SQUARES_OFFSET + Color.BLACK)
		.build();
	
	public PawnStructureData() {
		data = new long[DATA_SIZE];
	}

	public void clear() {
		Arrays.fill(data, 0);
	}
	
	public void assign(final PawnStructureData orig) {
		System.arraycopy(orig.data, 0, this.data, 0, DATA_SIZE);
	}

	private void fillOpenFileSquares() {
		// White
		final long whiteNotFileOpenSquares = BitBoard.extendBackward(whitePawnMask);
		data[BACK_SQUARES_OFFSET + Color.WHITE] = whiteNotFileOpenSquares >>> File.LAST;
		
		// Black
		long blackNotFileOpenSquares = BitBoard.extendForward(blackPawnMask);
		data[BACK_SQUARES_OFFSET + Color.BLACK] = blackNotFileOpenSquares << File.LAST;
	}
	
	private void fillOppositeFileAndAttackableSquares() {
		// White
		final long whiteReachableSquares = BitBoard.extendForward(whitePawnMask);
		
		data[FRONT_SQUARES_OFFSET + Color.WHITE] = whiteReachableSquares << File.LAST;
		data[NEIGHBOR_FRONT_SQUARES_OFFSET + Color.WHITE] = BoardConstants.getPawnsAttackedSquares(Color.WHITE, whiteReachableSquares & ~BoardConstants.RANK_18_MASK);
		
		// Black
		final long blackReachableSquares = BitBoard.extendBackward(blackPawnMask);
		
		data[FRONT_SQUARES_OFFSET + Color.BLACK] = blackReachableSquares >>> File.LAST;
		data[NEIGHBOR_FRONT_SQUARES_OFFSET + Color.BLACK] = BoardConstants.getPawnsAttackedSquares(Color.BLACK, blackReachableSquares & ~BoardConstants.RANK_18_MASK);
	}

	private void fillSecureSquares() {
		final long notPawnsSquares = ~(whitePawnMask | blackPawnMask);

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long pawnsMask = getPawnMask(color);
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

	public void precalculate(final long whitePawnMask, final long blackPawnMask) {
		this.whitePawnMask = whitePawnMask;
		this.blackPawnMask = blackPawnMask;

		fillOpenFileSquares();
		fillOppositeFileAndAttackableSquares();
		fillSecureSquares();
	}

	public void calculate() {
		calculatePawnTypes();
		calculatePawnDynamic();
		calculatePawnIslands();
	}

	public long getPawnMask(final int color) {
		return (color == Color.WHITE) ? whitePawnMask : blackPawnMask;
	}
	
	private void calculatePawnTypes() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			
			final long ownPawnMask = getPawnMask(color);
			final long openSquares = ~data[FRONT_SQUARES_OFFSET + oppositeColor];
			data[PASSED_PAWNS_OFFSET + color] = ownPawnMask & openSquares & ~data[NEIGHBOR_FRONT_SQUARES_OFFSET + oppositeColor];
		}
	}
	
	private void calculatePawnIslands() {
		final int files = getPawnFiles(Color.WHITE) | getPawnFiles(Color.BLACK);
		final long[] islandFiles = PawnIslandFileTable.getIslandsFiles(files);
		final long whiteActivePawns = getActivePawns(Color.WHITE);
		final long blackActivePawns = getActivePawns(Color.BLACK);
		
		long result = 0;
		
		for (long islandMask: islandFiles) {
			final long whiteIslandPawns = whitePawnMask & islandMask;
			final long blackIslandPawns = blackPawnMask & islandMask;
			
			int whiteItem = BitBoard.getSquareCount(whiteIslandPawns);
			int blackItem = BitBoard.getSquareCount(blackIslandPawns);
			
			if ((whiteIslandPawns & whiteActivePawns) != 0)
				whiteItem |= PAWN_ISLANDS_ALIVE_MASK;

			if ((blackIslandPawns & blackActivePawns) != 0)
				blackItem |= PAWN_ISLANDS_ALIVE_MASK;

			result = (result << PAWN_ISLANDS_ITEM_SHIFT) | blackItem;
			result = (result << PAWN_ISLANDS_ITEM_SHIFT) | whiteItem;
		}
		
		data[PAWN_ISLANDS_OFFSET] = result;
	}
	
	private long getActivePawns(final int color) {
		return getPawnMask(color) & (
						getPassedPawnMask(color) | ~(
								getBlockedPawnMask(color) |
								getSingleDisadvantageAttackPawnMask(color) |
								getDoubleDisadvantageAttackPawnMask(color)
						)
				);
	}

	private void calculatePawnDynamic() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long ownPawnMask = getPawnMask(color);
			final long oppositePawnMask = getPawnMask(oppositeColor);
			
			final long blockers = oppositePawnMask | BoardConstants.getPawnsAttackedSquares (oppositeColor, oppositePawnMask);
			final long nonAttackReachableSquares = makeSteps (ownPawnMask, blockers, color);
			
			final long squaresAttackedFromLeft = BoardConstants.getPawnsAttackedSquaresFromLeft(oppositeColor, oppositePawnMask);
			final long squaresAttackedFromRight = BoardConstants.getPawnsAttackedSquaresFromRight(oppositeColor, oppositePawnMask);
			
			final long squaresProtectableFromLeft = BoardConstants.getPawnsAttackedSquaresFromLeft(color, nonAttackReachableSquares);
			final long squaresProtectableFromRight = BoardConstants.getPawnsAttackedSquaresFromRight(color, nonAttackReachableSquares);
			
			final long singleAttackedSquares = squaresAttackedFromLeft ^ squaresAttackedFromRight;
			final long doubleAttackedSquares = squaresAttackedFromLeft & squaresAttackedFromRight;
			
			final long nonProtectableSquares = ~(squaresProtectableFromLeft | squaresProtectableFromRight);
			final long singleProtectableSquares = squaresProtectableFromLeft ^ squaresProtectableFromRight;
			
			final long singleDisadvantageSquares =
					(singleAttackedSquares & nonProtectableSquares) |
					(doubleAttackedSquares & singleProtectableSquares);
			final long extendedSingleDisadvantageSquares = makeSteps(singleDisadvantageSquares & ~oppositePawnMask, blockers, oppositeColor);
			data[SINGLE_DISADVANTAGE_ATTACK_PAWNS_OFFSET + color] = extendedSingleDisadvantageSquares;
			
			final long doubleDisadvantageSquares = doubleAttackedSquares & nonProtectableSquares;
			final long extendedDoubleDisadvantageSquares = makeSteps(doubleDisadvantageSquares & ~oppositePawnMask, blockers, oppositeColor);
			data[DOUBLE_DISADVANTAGE_ATTACK_PAWNS_OFFSET + color] = extendedDoubleDisadvantageSquares;
			
			final long blockedSquares = makeSteps(oppositePawnMask, blockers, oppositeColor);
			data[BLOCKED_PAWNS_OFFSET + color] = blockedSquares;
		}
	}

	private long makeSteps(final long initialMask, final long blockers, final int color) {
		long resultMask = initialMask;
		
		while (true) {
			final long prevMask = resultMask;
			final long advanced = (color == Color.WHITE) ? prevMask << File.COUNT : prevMask >>> File.COUNT;
			resultMask |= advanced & ~blockers;
			
			if (resultMask == prevMask)
				return resultMask;
		}
	}

	public long getSecureSquares (final int color) {
		return data[SECURE_SQUARES_OFFSET + color];
	}

	public long getPassedPawnMask(final int color) {
		return data[PASSED_PAWNS_OFFSET + color];
	}

	public long getSingleDisadvantageAttackPawnMask(final int color) {
		return data[SINGLE_DISADVANTAGE_ATTACK_PAWNS_OFFSET + color];
	}

	public long getDoubleDisadvantageAttackPawnMask(final int color) {
		return data[DOUBLE_DISADVANTAGE_ATTACK_PAWNS_OFFSET + color];
	}

	public long getBlockedPawnMask(final int color) {
		return data[BLOCKED_PAWNS_OFFSET + color];
	}
	
	private int getPawnIslandItem(final int index, final int color) {
		final int offset = ((index << Color.BIT_COUNT) + color) * PAWN_ISLANDS_ITEM_SHIFT;
		
		return (int) (data[PAWN_ISLANDS_OFFSET] >>> offset);
	}

	public int getIslandPawnCount(final int index, final int color) {
		final int item = getPawnIslandItem(index, color);
		
		return item & PAWN_ISLANDS_COUNT_MASK;
	}

	public boolean isIslandAlive(final int index, final int color) {
		final int item = getPawnIslandItem(index, color);
		
		return (item & PAWN_ISLANDS_ALIVE_MASK) != 0;
	}

	public int getPawnFiles(final int color) {
		return (int) data[PAWN_FILES_OFFSETS[color]] & 0xFF;
	}

}
