package bishopTests;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import bishop.base.Fen;
import bishop.base.IMoveGenerator;
import bishop.base.IMoveWalker;
import bishop.base.Move;
import bishop.base.Position;

public abstract class MoveGeneratorTestBase {

	protected static class TestValue {
		public final String positionFen;
		public final Set<String> expectedMoves;

		public TestValue(final String positionFen, final String[] expectedMoveArray) {
			this.positionFen = positionFen;
			this.expectedMoves = new HashSet<String>(Arrays.asList(expectedMoveArray));
		}
	}

	protected abstract TestValue[] getTestValues();
	protected abstract IMoveGenerator getMoveGenerator();
	
	protected String moveToString(final Move move) {
		return move.toString();
	}
	
	private Set<Move> getMoveSet(final Position position) throws IOException {
		final Set<Move> moveSet = new HashSet<Move>();

		final IMoveWalker walker = new IMoveWalker() {
			public boolean processMove(final Move move) {
				moveSet.add(move.copy());
				return true;
			}
		};

		final IMoveGenerator generator = getMoveGenerator();
		generator.setWalker(walker);
		
		generator.setPosition(position);
		generator.generateMoves();

		return moveSet;
	}
	
	private Set<String> getMoveNotationSet(final Position position) throws IOException {
		final Set<Move> moveSet = getMoveSet(position);		
		final Set<String> moveNotationSet = new HashSet<String>();
		
		for (Move move: moveSet) {
			moveNotationSet.add(moveToString(move));
		}
		
		return moveNotationSet;
	}

	@Test
	public void testGenerator() throws IOException {
		final TestValue[] testValueArray = getTestValues();
		final Fen fen = new Fen();

		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			
			final Position position = fen.getPosition();
			final Set<String> moveNotationSet = getMoveNotationSet(position);

			final Set<String> redundantMoves = new HashSet<String>(moveNotationSet);
			redundantMoves.removeAll(testValue.expectedMoves);

			final Set<String> missingMoves = new HashSet<String>(testValue.expectedMoves);
			missingMoves.removeAll(moveNotationSet);

			if (!redundantMoves.isEmpty() || !missingMoves.isEmpty()) {
				String msg = testValue.positionFen + " Redundant moves: " + redundantMoves + ", missing moves: " + missingMoves;

				throw new AssertionFailedError(msg);
			}
		}
	}
	
	@Test
	public void makeUndoMoveTest() throws IOException {
		final TestValue[] testValueArray = getTestValues();
		final Fen fen = new Fen();
		final IMoveGenerator generator = getMoveGenerator();

		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);
			
			final Position position = fen.getPosition();
			position.checkIntegrity();
			
			final Set<Move> moveSet = getMoveSet(position);
			
			for (Move move: moveSet) {
				final Position copyPosition = position.copy();
				copyPosition.checkIntegrity();
				
				switch (generator.getGeneratorType()) {
					case DIRECT:
						copyPosition.makeMove(move);
						copyPosition.checkIntegrity();
						copyPosition.undoMove(move);
						break;
						
					case REVERSE:
						copyPosition.undoMove(move);
						copyPosition.checkIntegrity();
						copyPosition.makeMove(move);
						break;
				}
				
				if (!position.equals(copyPosition)) {
					Assert.fail();
				}
			}
		}	
	}

	@Test
	public void speedTest() throws IOException {
		final String testPosFen = "r2q1rk1/1b2p1bp/p2pp1p1/1p2B3/1P2P3/2P2B2/P2QNPPP/3R1RK1 w - - 0 1";
		final int iterationCount = 2000000;
		final Move generatedMove = new Move();

		final Fen fen = new Fen();
		fen.readFenFromString(testPosFen);

		final Position position = fen.getPosition();

		IMoveWalker walker = new IMoveWalker() {
			public boolean processMove(final Move move) {
				generatedMove.assign(move);
				position.makeMove(generatedMove);
				position.undoMove(generatedMove);
				return true;
			}
		};

		final IMoveGenerator generator = getMoveGenerator();
		generator.setPosition(position);
		generator.setWalker(walker);

		for (int i = 0; i < iterationCount; i++)
			generator.generateMoves();

		final long t1 = System.currentTimeMillis();

		for (int i = 0; i < iterationCount; i++)
			generator.generateMoves();

		final long t2 = System.currentTimeMillis();
		final double iterPerSec = (double) iterationCount * 1000 / (t2 - t1);

		System.out.println("Iterations per second: " + iterPerSec);
	}
}
