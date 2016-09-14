package bishop.evaluationStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bishop.base.Color;
import bishop.base.GameResult;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Position;
import math.IMatrix;
import math.IVector;
import math.MatrixImpl;
import math.VectorImpl;

public class MaterialStatisticsPositionProcessor implements IPositionProcessor {

	private static final int MAX_PIECE_COUNT = 8;
	private static final int MIN_STABILITY = 4;
	private static final int MIN_TOTAL_COUNT = 10;
	private static final int MAX_PAWN_COUNT = 4;
	
	private final Map<MaterialHash, MaterialStatistics> statisticsMap = new HashMap<>();
	private final Map<MaterialHash, MaterialStatistics> pawnDifferentialStatistics = new HashMap<>();
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
		calculateDifferentialStatistics();
		calculatePawnComplement();
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
	
	private void calculateDifferentialStatistics() {
		for (Map.Entry<MaterialHash, MaterialStatistics> entry: statisticsMap.entrySet()) {
			final MaterialHash differentialHash = entry.getKey().copy();
			differentialHash.reduceToDifference();
			
			final MaterialStatistics statistics = entry.getValue().copy();
			normalizeHashAndStatistics (differentialHash, statistics);
			
			MaterialStatistics differentialStatistics = pawnDifferentialStatistics.get(differentialHash);
			
			if (differentialStatistics == null) {
				differentialStatistics = new MaterialStatistics();
				pawnDifferentialStatistics.put(differentialHash, differentialStatistics);
			}
			
			differentialStatistics.add(statistics);
		}
	}
	
	private void normalizeHashAndStatistics(final MaterialHash hash, final MaterialStatistics statistics) {
		final MaterialHash oppositeHash = hash.getOpposite();
		
		if (hash.compareTo(oppositeHash) < 0) {
			hash.assign(oppositeHash);
			statistics.negate();
		}
	}

	public void calculatePawnComplement() {
		final List<PawnComplement> pawnComplementList = new ArrayList<>();
		
		for (MaterialHash zeroPawnHash: pawnDifferentialStatistics.keySet()) {
			if (zeroPawnHash.getPieceCount(Color.WHITE, PieceType.PAWN) == 0 && zeroPawnHash.getPieceCount(Color.BLACK, PieceType.PAWN) == 0) {
				final MaterialStatistics zeroPawnStatistics = pawnDifferentialStatistics.get(zeroPawnHash);
				final double zeroPawnBalance = zeroPawnStatistics.getBalance();
				final double coeff;
				final int color;
				
				if (zeroPawnBalance >= 0) {
					color = Color.BLACK;
					coeff = -1;
				}
				else {
					color = Color.WHITE;
					coeff = +1;
				}
				
				MaterialHash prevHash = zeroPawnHash;
				MaterialStatistics prevStatistics = zeroPawnStatistics;
				double prevBalance = zeroPawnBalance;
				
				for (int i = 1; i <= MAX_PAWN_COUNT; i++) {
					final MaterialHash nextHash = prevHash.copy();
					nextHash.addPiece(color, PieceType.PAWN);
					
					final MaterialStatistics nextStatistics = pawnDifferentialStatistics.get(nextHash);
					
					if (nextStatistics == null)
						break;
					
					final double nextBalance = nextStatistics.getBalance();
					
					if (nextBalance * coeff >= 0) {
						final long totalCount = Math.min(prevStatistics.getTotalCount(), nextStatistics.getTotalCount());
						
						if (totalCount >= MIN_TOTAL_COUNT) {
							final double pawnCount = math.Utils.linearInterpolation(prevBalance, nextBalance, i - 1, i, 0);
							System.out.println(prevHash + " " + prevBalance);
							System.out.println(nextHash + " " + nextBalance);
							System.out.println(pawnCount);
							
							final PawnComplement complement = new PawnComplement(zeroPawnHash, -pawnCount * coeff, totalCount);
							pawnComplementList.add(complement);
						}
						
						break;
					}
					
					prevHash = nextHash;
					prevStatistics = nextStatistics;
					prevBalance = nextBalance;
				}
			}
		}
		
		final int equationCount = pawnComplementList.size();
		final double[][] aElements = new double[equationCount][PieceType.PROMOTION_FIGURE_COUNT];
		final double[] bElements = new double[equationCount];
		final double[] weightsElements = new double[equationCount];
		
		for (int i = 0; i < equationCount; i++) {
			final PawnComplement complement = pawnComplementList.get(i);
			final MaterialHash materialHash = complement.getMaterialHash();
			
			for (int j = 0; j < PieceType.PROMOTION_FIGURE_COUNT; j++) {
				final int pieceType = PieceType.PROMOTION_FIGURE_FIRST + j;
				aElements[i][j] = materialHash.getPieceCount(Color.WHITE, pieceType) - materialHash.getPieceCount(Color.BLACK, pieceType);
			}
			
			bElements[i] = complement.getComplement();
			weightsElements[i] = complement.getTotalCount();
		}
		
		final IMatrix a = new MatrixImpl(aElements);
		final IVector b = new VectorImpl(bElements);
		final IVector weights = new VectorImpl(weightsElements);
		
		final IVector result = math.Utils.solveEquationsLeastSquare (a, b, weights);
		
		for (int j = 0; j < PieceType.PROMOTION_FIGURE_COUNT; j++) {
			final int pieceType = PieceType.PROMOTION_FIGURE_FIRST + j;
			System.out.println(PieceType.getName(pieceType) + " " + result.getElement(j));
		}
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
