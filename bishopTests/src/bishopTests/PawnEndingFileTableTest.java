package bishopTests;

import bishop.base.*;
import bishop.engine.PawnEndingKey;
import bishop.tables.PawnEndingFileTable;
import org.junit.Assert;
import org.junit.Test;


public class PawnEndingFileTableTest {

    @Test
    public void fileIndexUniqueTest() {
        for (int file = File.FIRST; file < File.LAST; file++) {
            final boolean[] existingIndices = new boolean[PawnEndingFileTable.INDEX_COUNT];

            for (PawnEndingKey key : PawnEndingFileTable.getAllFileCombinations(file)) {
                final int index = PawnEndingFileTable.getFileIndex(key, file);

                Assert.assertFalse(existingIndices[index]);
                existingIndices[index] = true;
            }
        }
    }

    private static int getComplexity (final String whitePawns, final String blackPawns, final int file) {
        final PawnEndingKey key = new PawnEndingKey(BitBoard.fromString(whitePawns), BitBoard.fromString(blackPawns));

        return PawnEndingFileTable.getComplexity(key, file);
    }

    @Test
    public void complexityTest() {
        Assert.assertEquals(2, getComplexity("b7", "", File.FB));
        Assert.assertEquals(7, getComplexity("b2", "", File.FB));
        Assert.assertEquals(7, getComplexity("b2, d1, d2", "", File.FB));
        Assert.assertEquals(6 + 6 + 4 + 1, getComplexity("h4", "h7", File.FH));
    }
}
