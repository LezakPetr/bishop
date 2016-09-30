package bishop.evaluationStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import bishop.base.Color;
import bishop.base.GameResult;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.engine.TableMaterialEvaluator;
import math.IMatrix;
import math.IVector;
import math.MatrixImpl;
import math.VectorImpl;

public class MaterialStatisticsPositionProcessor implements IPositionProcessor {

	private static final int MAX_PIECE_COUNT = 8;
	private static final int MIN_STABILITY = 4;
	private static final int MIN_TOTAL_COUNT = 10;
	private static final int MAX_PAWN_COUNT = 4;
	
	private final Map<MaterialHash, MaterialStatistics> statisticsMap = new HashMap<>();   // Statistics for every material
	private Map<MaterialHash, MaterialStatistics> symmetricalStatistics;
	private Map<MaterialHash, MaterialStatistics> pawnDifferentialStatistics;
	private Map<MaterialHash, MaterialStatistics> pieceDifferentialStatistics;
	
	private final TableMaterialEvaluator tableEvaluator = new TableMaterialEvaluator(null);
	
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
		
	public void calculate() {
		calculateSymmetricalStatistics();
		calculatePawnDifferentialStatistics();
		calculatePieceDifferentialStatistics();
		calculatePawnComplement();
	}
	
	private void calculateSymmetricalStatistics() {
		for (Map.Entry<MaterialHash, MaterialStatistics> entry: statisticsMap.entrySet()) {
			final MaterialHash hash = entry.getKey().copy();
			
			final MaterialStatistics statistics = entry.getValue().copy();
			normalizeHashAndStatistics (hash, statistics);
			
			MaterialStatistics statisticsInMap = symmetricalStatistics.get(hash);
			
			if (statisticsInMap == null) {
				statisticsInMap = new MaterialStatistics();
				symmetricalStatistics.put(hash, statisticsInMap);
			}
			
			statisticsInMap.add(statistics);
		}
	}
	
	private void calculatePawnDifferentialStatistics() {
		pawnDifferentialStatistics = mergeMapKeys(symmetricalStatistics, (h) -> {
			final MaterialHash result = h.copy();
			result.reducePieceToDifference(PieceType.PAWN);
			
			return result;
		});
	}

	private void calculatePieceDifferentialStatistics() {
		pieceDifferentialStatistics = mergeMapKeys(pawnDifferentialStatistics, (h) -> {
			final MaterialHash result = h.copy();
			result.reduceToDifference();
			
			return result;
		});
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
		
	private void normalizeHashAndStatistics(final MaterialHash hash, final MaterialStatistics statistics) {
		final MaterialHash oppositeHash = hash.getOpposite();
		
		if (hash.compareTo(oppositeHash) < 0) {
			hash.assign(oppositeHash);
			statistics.negate();
		}
	}

	public void calculatePawnComplement() {
		final List<PawnComplement> pawnComplementList = calculatePawnComplementList();
		
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

	public List<PawnComplement> calculatePawnComplementList() {
		final List<PawnComplement> pawnComplementList = new ArrayList<>();
		
		for (MaterialHash zeroPawnHash: pieceDifferentialStatistics.keySet()) {
			if (zeroPawnHash.getPieceCount(Color.WHITE, PieceType.PAWN) == 0 && zeroPawnHash.getPieceCount(Color.BLACK, PieceType.PAWN) == 0) {
				final MaterialStatistics zeroPawnStatistics = pieceDifferentialStatistics.get(zeroPawnHash);
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
					
					final MaterialStatistics nextStatistics = pieceDifferentialStatistics.get(nextHash);
					
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
		return pawnComplementList;
	}
		
	/**
	 * Creates new statistic map from given one. The keys (material hashes) are mapped by keyMapper. If there will be collision of returned keys the
	 * statistics are summed together.
	 * @param origMap original map
	 * @param keyMapper mapper of the material hashes
	 * @return transformed map
	 */
	public Map<MaterialHash, MaterialStatistics> mergeMapKeys(final Map<MaterialHash, MaterialStatistics> origMap, final UnaryOperator<MaterialHash> keyMapper)
	{
		return origMap.entrySet().stream().collect(Collectors.toMap(e -> keyMapper.apply(e.getKey()), e -> e.getValue(), MaterialStatistics::sum));
	}
}
