package bishopTests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.Fen;
import bishop.base.Move;
import bishop.base.Position;
import bishopTests.PseudoLegalMoveGeneratorTest.PositionWithMoves;

public class UncompressMoveTest {
	@Test
	public void testAllCombinations() throws IOException {
		final PositionWithMoves[] testCases = PseudoLegalMoveGeneratorTest.getPseudoLegalTestValues();
		
		for (PositionWithMoves testCase: testCases) {
			final Fen fen = new Fen();
			fen.readFenFromString(testCase.getPositionFen());
			
			final Position position = fen.getPosition();
			position.checkIntegrity();
			
			final List<String> uncompressedMoveList = new ArrayList<>();
			
			for (int compressedMove = Move.FIRST_COMPRESSED_MOVE; compressedMove < Move.LAST_COMPRESSED_MOVE; compressedMove++) {
				final Move move = new Move();
				
				if (move.uncompressMove(compressedMove, position)) {
					Assert.assertEquals("Position " + position + ", move: " + move, compressedMove, move.getCompressedMove());
					uncompressedMoveList.add(move.toString());
				}
			}
			
			final Set<String> expectedMoves = testCase.getPseudoLegalMoves();
			final Set<String> givenMoves = new HashSet<>(uncompressedMoveList);
			
			Assert.assertEquals(expectedMoves, givenMoves);
			Assert.assertEquals(expectedMoves.size(), uncompressedMoveList.size());   // Test for duplicates in uncompressedMoveList
		}
	}

}
