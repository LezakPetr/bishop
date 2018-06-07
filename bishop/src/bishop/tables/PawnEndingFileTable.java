package bishop.tables;

import bishop.base.*;
import bishop.engine.PawnEndingKey;

import java.util.*;

public class PawnEndingFileTable {

    public static final int INDEX_COUNT = 1 << 12;
    public static final int INDEX_MASK = INDEX_COUNT - 1;

    private static final int FIRST_RANK = Rank.R2;
    private static final int LAST_RANK = Rank.R8;
    private static final int RANK_COUNT = LAST_RANK - FIRST_RANK;
    private static final int COMBINATION_COUNT = 1 << RANK_COUNT;

    private static final long BIT_MASK = BoardConstants.FILE_A_MASK & BoardConstants.PAWN_ALLOWED_SQUARES;
    private static final long BIT_COEFF = 0x40010004001L;   // 1 + 2^14 + 2^28 + 2^42
    private static final int BIT_SHIFT = 44;


    private static final short[] complexityTable = createComplexityTable();

    public static int getFileIndex(final PawnEndingKey key, final int file) {
        // 00000000 0000000w 0000000w 0000000w 0000000w 0000000w 0000000w 00000000
        final long normalizedWhitePawns = (key.getWhitePawns() >>> file) & BIT_MASK;

        // 00000000 0000000b 0000000b 0000000b 0000000b 0000000b 0000000b 00000000
        final long normalizedBlackPawns = (key.getBlackPawns() >>> file) & BIT_MASK;

        // 00000000
        // 000000bw000000
        // bw000000bw0000
        // 00bw000000bw00
        // 0000bw00000000
        final long totalMask = normalizedWhitePawns | (normalizedBlackPawns << 1);

        return ((int) ((totalMask * BIT_COEFF) >>> BIT_SHIFT)) & INDEX_MASK;
    }

    public static int getComplexity(final PawnEndingKey key, final int file) {
        final int index = getFileIndex(key, file);

        return complexityTable[index];
    }

    private static short[] createComplexityTable() {
        final int file = File.FA;
        final List<PawnEndingKey> allCombinations = getAllFileCombinations(file);

        final Map<Integer, Set<Integer>> combinations = new HashMap<>();
        //combinations.put(getFileIndex(PawnEndingKey.EMPTY, file), new HashSet<>(1));

        boolean isChange;

        do {
            isChange = false;

            for (PawnEndingKey key: allCombinations) {
                if (!combinations.containsKey(getFileIndex(key, file))) {
                    final List<PawnEndingKey> subKeys = new ArrayList<>();

                    addCapturesSubKeys(key, subKeys);
                    addMovesSubKeys(key, subKeys);

                    final boolean allSubKeysKnown = subKeys.stream().allMatch(k -> combinations.containsKey(getFileIndex(k, file)));

                    if (allSubKeysKnown) {
                        final Set<Integer> subIndices = new HashSet<>();
                        subIndices.add(getFileIndex(key, file));

                        for (PawnEndingKey subKey: subKeys)
                            subIndices.addAll(combinations.get(getFileIndex(subKey, file)));

                        combinations.put(getFileIndex(key, file), subIndices);
                        isChange = true;
                    }
                }
            }
        } while (isChange);

        // Get sizes of the combinations
        final short[] table = new short[INDEX_COUNT];

        for (Map.Entry<Integer, Set<Integer>> entry : combinations.entrySet())
            table[entry.getKey()] = (short) entry.getValue().size();

        return table;
    }

    private static void addMovesSubKeys(final PawnEndingKey key, final List<PawnEndingKey> subKeys) {
        for (int color = Color.FIRST; color < Color.LAST; color++) {
            for (BitLoop sourceLoop = new BitLoop(key.getPawnMask(color)); sourceLoop.hasNextSquare(); ) {
                final int sourceSquare = sourceLoop.getNextSquare();
                final long targetSquares = PawnMoveTable.getItem(color, sourceSquare) & ~key.getPawnOccupancy() & BoardConstants.PAWN_ALLOWED_SQUARES;

                for (BitLoop tagretLoop = new BitLoop(targetSquares); tagretLoop.hasNextSquare(); ) {
                    final int targetSquare = tagretLoop.getNextSquare();

                    if ((BetweenTable.getItem(sourceSquare, targetSquare) & key.getPawnOccupancy()) == 0)
                        subKeys.add(key.removePawn(sourceSquare).addPawn(color, targetSquare));
                }
            }
        }
    }

    private static void addCapturesSubKeys(final PawnEndingKey key, final List<PawnEndingKey> subKeys) {
        for (BitLoop loop = new BitLoop(key.getPawnOccupancy()); loop.hasNextSquare(); ) {
            final int square = loop.getNextSquare();

            subKeys.add(key.removePawn(square));
        }
    }

    public static List<PawnEndingKey> getAllFileCombinations(final int file) {
        final List<PawnEndingKey> combinations = new ArrayList<>();

        for (int whitePawns = 0; whitePawns < COMBINATION_COUNT; whitePawns++) {
            for (int blackPawns = 0; blackPawns < COMBINATION_COUNT; blackPawns++) {
                if ((whitePawns & blackPawns) == 0) {
                    final long whitePawnMask = getPawnMask(whitePawns, file);
                    final long blackPawnMask = getPawnMask(blackPawns, file);
                    final PawnEndingKey key = new PawnEndingKey(whitePawnMask, blackPawnMask);
                    combinations.add(key);
                }
            }
        }

        return combinations;
    }

    private static long getPawnMask(final int pawnCombination, final int file) {
        long mask = 0;

        for (int rank = 0; rank < RANK_COUNT; rank++) {
            if (((pawnCombination >>> rank) & 0x01) != 0)
                mask |= BitBoard.getSquareMask(Square.onFileRank(file, rank + FIRST_RANK));
        }

        return mask;
    }
}

