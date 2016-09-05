package bishop.evaluationStatistics;

import java.util.HashMap;
import java.util.Map;

import bishop.base.Color;
import bishop.base.GameResult;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Position;

public class MaterialStatisticsPositionProcessor implements IPositionProcessor {

	private static final int MAX_PIECE_COUNT = 8;
	private static final int MIN_STABILITY = 4;
	
	private final Map<MaterialHash, MaterialStatistics> statisticsMap = new HashMap<>();
	private final MaterialStatistics[][] pieceStatistics = new MaterialStatistics[PieceType.VARIABLE_LAST][];
	
	private MaterialHash prevHash;
	private int stability;
	private boolean processed;
	private GameResult result;

	
	public MaterialStatistics getStatistics() {
		MaterialStatistics statistics = statisticsMap.get(prevHash);
		
		if (statistics == null) {
			statistics = new MaterialStatistics();
			statisticsMap.put(prevHash, statistics);
		}
		
		return statistics;
	}
	
	@Override
	public void newGame(final GameResult result) {
		this.result = result;
		this.stability = 0;
		this.prevHash = null;
	}
	
	@Override
	public void processPosition (final Position position) {
		final MaterialHash currentHash = position.getMaterialHash();
		currentHash.setOnTurn(Color.WHITE);
		
		if (currentHash.equals(prevHash))
			stability++;
		else {
			processStatistics();
			
			prevHash = currentHash;
			stability = 1;
			processed = false;
		}
	}
	
	@Override
	public void endGame() {
		processStatistics();
	}
	
	private void processStatistics() {
		if (stability >= MIN_STABILITY && !processed) {
			final MaterialStatistics statictics = getStatistics();
			statictics.addResult(result);
			
			processed = true;
		}
	}
	
	private void calculatePieceStatistics() {
		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
			pieceStatistics[pieceType] = calculatePieceStatistics(pieceType);
		}
	}
	
	public void calculate() {
		calculatePieceStatistics();
	}
	
	private MaterialStatistics[] calculatePieceStatistics(final int pieceType) {
		final MaterialStatistics[] pieceStatistics = new MaterialStatistics[MAX_PIECE_COUNT + 1];
		
		for (int pieceCount = 0; pieceCount <= MAX_PIECE_COUNT; pieceCount++)
			pieceStatistics[pieceCount] = new MaterialStatistics();
		
		for (MaterialHash material: statisticsMap.keySet()) {
			if (material.isBalancedExceptFor (pieceType)) {
				final int whiteCount = material.getPieceCount(Color.WHITE, pieceType);
				final int blackCount = material.getPieceCount(Color.BLACK, pieceType);
				final int countDiff = whiteCount - blackCount;
				final int absCountDiff = Math.abs(countDiff);
				
				if (absCountDiff <= MAX_PIECE_COUNT) {
					if (countDiff >= 0)
						pieceStatistics[absCountDiff].add(statisticsMap.get(material));
					else
						pieceStatistics[absCountDiff].addOpposite(statisticsMap.get(material));
				}
			}
		}
		
		return pieceStatistics;
	
	}
	
	public void printPieceStatistics() {
		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
			System.out.println(PieceType.getName(pieceType));
			
			for (int pieceCount = 0; pieceCount <= MAX_PIECE_COUNT; pieceCount++) {
				final MaterialStatistics statistics = pieceStatistics[pieceType][pieceCount];
				System.out.println(pieceCount + ": " + statistics.getBalance() + " (" + statistics.getTotalCount() + ")");
			}
			
			System.out.println();
		}
	}
}
