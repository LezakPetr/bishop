package bishop.tablebase;

import java.util.Arrays;
import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Position;

public class ClassificationProbabilityModelSelector implements IProbabilityModelSelector {
	
	public static final int DEFAULT_CLASSIFICATION_HISTORY_LENGTH = 2;

	private final int symbolCount;
	private final int classificationHistoryLength;
	private final int[] previousSymbols;
	private final int[] classificationHistory;
	private final byte[] symbolClassificationIndices;
	private final int classificationModulus;
	private final int previousSymbolClassification;
	private int[][] bishopPositionIndices;
	
	public ClassificationProbabilityModelSelector(final ISymbolToResultMap symbolToResultMap, final int classificationHistoryLength, final boolean previousWin, final MaterialHash materialHash) {
		this.symbolCount = symbolToResultMap.getSymbolCount();
		this.classificationHistoryLength = classificationHistoryLength;
		
		final int positionIndexCount = initializeBishopPositionIndices (materialHash);
		this.previousSymbols = new int[positionIndexCount];
		this.classificationHistory = new int[positionIndexCount];
		
		this.previousSymbolClassification = (previousWin) ? Classification.WIN : Classification.LOSE;
		this.symbolClassificationIndices = createSymbolClassificationIndices (symbolToResultMap);;
		this.classificationModulus = Utils.intPower (Classification.COUNT_LEGAL, classificationHistoryLength);
		
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
		
		classificationHistory[positionIndex] = (Classification.COUNT_LEGAL * classificationHistory[positionIndex] + classificationIndex) % classificationModulus;
	}
	
	@Override
	public void resetSymbols() {
		Arrays.fill(previousSymbols, symbolCount);
		Arrays.fill(classificationHistory, Classification.DRAW);
	}
	
	@Override
	public int getModelCount() {
		return classificationModulus * previousSymbols.length * (symbolCount + 1);
	}
	
	private static byte[] createSymbolClassificationIndices (final ISymbolToResultMap symbolToResultMap) {
		final int symbolCount = symbolToResultMap.getSymbolCount();
		final byte[] classificationIndices = new byte[symbolCount];
		
		for (int i = 0; i < symbolCount; i++) {
			final int result = symbolToResultMap.symbolToResult(i);
			final byte classificationIndex;
			
			if (TableResult.isWin(result))
				classificationIndex = Classification.WIN;
			else {
				if (TableResult.isLose(result))
					classificationIndex = Classification.LOSE;
				else {
					if (result == TableResult.DRAW)
						classificationIndex = Classification.DRAW;
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
		return previousSymbolClassification == Classification.WIN;
	}

}
