package bishop.tablebase;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.Piece;
import bishop.base.Position;
import bishop.base.Square;
import bishop.tables.SquareSymmetryTable;

/**
 * Immutable combination of same pieces on given squares.
 * @author Ing. Petr Ležák
 */
public class SquareCombination {

	private final ICombinatorialNumberSystem numberSystem;
	private final SquareCombinationKey squareCombinationKey;
	
	private final byte[] forwardSquareMapping;
	private final byte[] backwardSquareMapping;
	
	public SquareCombination(final SquareCombinationKey squareCombinationKey) {
		this.squareCombinationKey = squareCombinationKey;
		
		final long allowedSquares = squareCombinationKey.getAllowedSquares();
		final int allowedSquareCount = BitBoard.getSquareCount(allowedSquares);
		final int pieceCount = squareCombinationKey.getDefinition().getCount();

		final CombinatorialNumberSystemDefinition numberSystemDefinition = new CombinatorialNumberSystemDefinition(allowedSquareCount, pieceCount);
		this.numberSystem = CombinatorialNumberSystemRegistrar.getInstance().getDefinition(numberSystemDefinition);
		
		this.forwardSquareMapping = new byte[Square.LAST];
		this.backwardSquareMapping = new byte[allowedSquareCount];
		
		fillSquareMapping(allowedSquares);
	}

	public void fillSquareMapping(final long allowedSquares) {
		final Piece piece = squareCombinationKey.getDefinition().getPiece();
		byte index = 0;
		
		for (int sequenceIndex = SquareSequence.FIRST_INDEX; sequenceIndex < SquareSequence.LAST_INDEX; sequenceIndex++) {
			final int square = SquareSequence.getSquareOnIndex(piece.getColor(), piece.getPieceType(), sequenceIndex);
			
			if ((allowedSquares & BitBoard.getSquareMask(square)) != 0) {
				forwardSquareMapping[square] = index;
				backwardSquareMapping[index] = (byte) square;
				index++;
			}
			else
				forwardSquareMapping[square] = -1;
		}
	}
	
	public int getCount() {
		return numberSystem.getCombinationCount();
	}
	
	private int squareArrayToIndex(final int[] squareArray) {
		final int[] transformedSquares = new int[squareArray.length];
		
		for (int i = 0; i < squareArray.length; i++) {
			final int transformedSquare = forwardSquareMapping[squareArray[i]];
			
			if (transformedSquare < 0)
				return -1;
			
			transformedSquares[i] = transformedSquare;
		}
		
		return numberSystem.getCombinationIndex(transformedSquares);
	}
	

	public int calculateIndex(final long piecesMask, final int symmetry) {
		final int[] squareArray = new int[numberSystem.getK()];
		
		int index = 0;
		
		for (BitLoop loop = new BitLoop(piecesMask); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			final int transformedSquare = SquareSymmetryTable.getItem(symmetry, square);
			
			if (index >= squareArray.length)
				throw new RuntimeException();
			
			squareArray[index] = transformedSquare;
			index++;
		}
		
		return squareArrayToIndex(squareArray);
	}

	public void setToPosition(final Position position, final int combinationIndex) {
		final Piece piece = squareCombinationKey.getDefinition().getPiece();
		final long combinationMask = numberSystem.getCombinationMask(combinationIndex);
		
		for (BitLoop loop = new BitLoop(combinationMask); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			final int transformedSquare = backwardSquareMapping[square];
			
			position.setSquareContent(transformedSquare, piece);
		}
	}
	
	public SquareCombinationKey getKey() {
		return squareCombinationKey;
	}

}
