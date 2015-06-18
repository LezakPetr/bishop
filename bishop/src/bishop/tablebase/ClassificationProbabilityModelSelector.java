package bishop.tablebase;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Position;

public class ClassificationProbabilityModelSelector implements IProbabilityModelSelector {
	
	public static final int DEFAULT_CLASSIFICATION_HISTORY_LENGTH = 2;
	
	private static final int CLASSIFICATION_INDEX_DRAW = 0;
	private static final int CLASSIFICATION_INDEX_WIN = 1;
	private static final int CLASSIFICATION_INDEX_LOSE = 2;
	private static final int CLASSIFICATION_INDEX_COUNT = 3;
	
	private final int symbolCount;
	private final int classificationHistoryLength;
	private final int[] previousSymbols;
	private final int[] classificationHistory;
	private final byte[] symbolClassificationIndices;
	private final int classificationModulus;
	private final int previousSymbolClassification;
	private int[][] bishopPositionIndices;
	
	public ClassificationProbabilityModelSelector(final int[] symbolToResultMap, final int classificationHistoryLength, final boolean previousWin, final MaterialHash materialHash) {
		this.symbolCount = symbolToResultMap.length;
		this.classificationHistoryLength = classificationHistoryLength;
		
		final int positionIndexCount = initializeBishopPositionIndices (materialHash);
		this.previousSymbols = new int[positionIndexCount];
		this.classificationHistory = new int[positionIndexCount];
		
		this.previousSymbolClassification = (previousWin) ? CLASSIFICATION_INDEX_WIN : CLASSIFICATION_INDEX_LOSE;
		this.symbolClassificationIndices = createSymbolClassificationIndices (symbolToResultMap);;
		this.classificationModulus = Utils.intPower (CLASSIFICATION_INDEX_COUNT, classificationHistoryLength);
		
		resetSymbols();
	}
	
	private int initializeBishopPositionIndices(final MaterialHash materialHash) {
		final int whiteBishopCount = materialHash.getPieceCount(Color.WHITE, PieceType.BISHOP);
		final int blackBishopCount = materialHash.getPieceCount(Color.BLACK, PieceType.BISHOP);
		
		bishopPositionIndices = new int[whiteBishopCount+1][blackBishopCount+1];
		Utils.fillArray2D (bishopPositionIndices, -1);
		
		int positionIndex = 0;
		
		for (int whiteIndex = 0; whiteIndex <= whiteBishopCount; whiteIndex++) {
			for (int blackIndex = 0; blackIndex <= blackBishopCount; blackIndex++) {
				if (bishopPositionIndices[whiteIndex][blackIndex] < 0) {
					bishopPositionIndices[whiteIndex][blackIndex] = positionIndex;
					bishopPositionIndices[whiteBishopCount-whiteIndex][blackBishopCount-blackIndex] = positionIndex;
					positionIndex++;
				}
			}
		}
		
		return positionIndex;
	}

	private int getPositionIndex(final Position position) {
		final long whiteBishopMask = position.getPiecesMask(Color.WHITE, PieceType.BISHOP);
		final int whiteSquaredWhiteBishopCount = BitBoard.getSquareCount(whiteBishopMask & BoardConstants.WHITE_SQUARE_MASK);

		final long blackBishopMask = position.getPiecesMask(Color.BLACK, PieceType.BISHOP);
		final int whiteSquaredBlackBishopCount = BitBoard.getSquareCount(blackBishopMask & BoardConstants.WHITE_SQUARE_MASK);
		
		return bishopPositionIndices[whiteSquaredWhiteBishopCount][whiteSquaredBlackBishopCount];
	}
	
	@Override
	public int getModelIndex(final Position position) {
		final int positionIndex = getPositionIndex(position);
		final int previousSymbol = previousSymbols[positionIndex];
		final int history = classificationHistory[positionIndex];
		
		int result = positionIndex;
		
		result *= (symbolCount + 1);
		result += previousSymbol;
		
		result *= classificationModulus;
		result += history;
		
		return result; 
	}
	
	@Override
	public void addSymbol (final Position position, final int symbol) {
		final int positionIndex = getPositionIndex(position);
		final int classificationIndex = symbolClassificationIndices[symbol];
		
		if (classificationIndex == previousSymbolClassification)
			previousSymbols[positionIndex] = symbol;
		
		classificationHistory[positionIndex] = (CLASSIFICATION_INDEX_COUNT * classificationHistory[positionIndex] + classificationIndex) % classificationModulus;
	}
	
	@Override
	public void resetSymbols() {
		Arrays.fill(previousSymbols, symbolCount);
		Arrays.fill(classificationHistory, CLASSIFICATION_INDEX_DRAW);
	}
	
	@Override
	public int getModelCount() {
		return classificationModulus * previousSymbols.length * (symbolCount + 1);
	}
	
	private static byte[] createSymbolClassificationIndices (final int[] symbolToResultMap) {
		final byte[] classificationIndices = new byte[symbolToResultMap.length];
		
		for (int i = 0; i < symbolToResultMap.length; i++) {
			final int result = symbolToResultMap[i];
			final byte classificationIndex;
			
			if (TableResult.isWin(result))
				classificationIndex = CLASSIFICATION_INDEX_WIN;
			else {
				if (TableResult.isLose(result))
					classificationIndex = CLASSIFICATION_INDEX_LOSE;
				else {
					if (result == TableResult.DRAW)
						classificationIndex = CLASSIFICATION_INDEX_DRAW;
					else
						throw new RuntimeException("Unknown result");
				}
			}
			
			classificationIndices[i] = classificationIndex;
		}
		
		return classificationIndices;
	}

	public int getClassificationHistoryLength() {
		return classificationHistoryLength;
	}

	public boolean isPreviousWin() {
		return previousSymbolClassification == CLASSIFICATION_INDEX_WIN;
	}

}
