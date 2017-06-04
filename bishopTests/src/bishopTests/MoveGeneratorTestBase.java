package bishopTests;

import java.io.IOException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
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
		public final Set<String> minExpectedMoves;
		public final Set<String> maxExpectedMoves;

		public TestValue(final String positionFen, final Set<String> minExpectedMoves, final Set<String> maxExpectedMoves) {
			this.positionFen = positionFen;
			this.minExpectedMoves = minExpectedMoves;
			this.maxExpectedMoves = maxExpectedMoves;
		}

		public TestValue(final String positionFen, final Set<String> expectedMoves) {
			this (positionFen, expectedMoves, expectedMoves);
		}

		public TestValue(final String positionFen, final String minExpectedMoveStr, final String maxExpectedMoveStr) {
			this.positionFen = positionFen;
			this.minExpectedMoves = splitMoveList(minExpectedMoveStr);
			this.maxExpectedMoves = splitMoveList(maxExpectedMoveStr);
		}

		public TestValue(final String positionFen, final String expectedMoveArray) {
			this (positionFen, expectedMoveArray, expectedMoveArray);
		}
		
		public static Set<String> splitMoveList(final String moveList) {
			final Set<String> result = new HashSet<String>();
			int index = 0;
			
			while (index < moveList.length()) {
				int nextIndex = moveList.indexOf(',', index);
				
				if (nextIndex < 0)
					nextIndex = moveList.length();
				
				result.add(moveList.substring(index, nextIndex).trim());
				index = nextIndex + 1;
			}
			
			return result;
		}
	}

	protected abstract TestValue[] getTestValues();
	protected abstract IMoveGenerator getMoveGenerator();
	
	protected List<Object> parameters;
	
	protected List<List<Object>> getParameterCombinations() {
		return Collections.singletonList(Collections.emptyList());
	}
	
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
		for (List<Object> parameters: getParameterCombinations()) {
			this.parameters = parameters;
			
			final TestValue[] testValueArray = getTestValues();
			final Fen fen = new Fen();
	
			for (TestValue testValue: testValueArray) {
				fen.readFenFromString(testValue.positionFen);
				
				if (!testValue.maxExpectedMoves.containsAll(testValue.minExpectedMoves))
					throw new RuntimeException("Wrong test " + testValue.positionFen + " - minExpectedMoves is not subset of maxExpectedMoves!!!");
				
				final Position position = fen.getPosition();
				final Set<String> moveNotationSet = getMoveNotationSet(position);
	
				final Set<String> redundantMoves = new HashSet<String>(moveNotationSet);
				redundantMoves.removeAll(testValue.maxExpectedMoves);
	
				final Set<String> missingMoves = new HashSet<String>(testValue.minExpectedMoves);
				missingMoves.removeAll(moveNotationSet);
	
				if (!redundantMoves.isEmpty() || !missingMoves.isEmpty()) {
					String msg = testValue.positionFen + " " + parameters + " Redundant moves: " + redundantMoves + ", missing moves: " + missingMoves;
	
					throw new AssertionFailedError(msg);
				}
			}
		}
	}
	
	@Test
	public void makeUndoCompressMoveTest() throws IOException {
		for (List<Object> parameters: getParameterCombinations()) {
			this.parameters = parameters;

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
							copyPosition.checkIntegrity();
							
							final int compressedMove = move.getCompressedMove();
							final Move uncompressedMove = new Move();
							uncompressedMove.uncompressMove(compressedMove, copyPosition);
							
							Assert.assertEquals(move, uncompressedMove);

							break;
							
						case REVERSE:
							copyPosition.undoMove(move);
							copyPosition.checkIntegrity();
							copyPosition.makeMove(move);
							copyPosition.checkIntegrity();
							break;
					}
					
					if (!position.equals(copyPosition)) {
						Assert.fail();
					}
				}
			}	
		}
	}

	@Test
	public void speedTest() throws IOException {
		for (List<Object> parameters: getParameterCombinations()) {
			this.parameters = parameters;

			final String testPosFen = "r2q1rk1/1b2p1bp/p2pp1p1/1p2B3/1P2P3/2P2B2/P2QNPPP/3R1RK1 w - - 0 1";
			final int iterationCount = 2000000;
			final Move generatedMove = new Move();
	
			final Fen fen = new Fen();
			fen.readFenFromString(testPosFen);
	
			final Position position = fen.getPosition();
	
			IMoveWalker walker = new IMoveWalker() {
				public boolean processMove(final Move move) {
					generatedMove.assign(move);
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
	
			System.out.println("Iterations per second " + parameters + ": " + iterPerSec);
		}
	}
}
