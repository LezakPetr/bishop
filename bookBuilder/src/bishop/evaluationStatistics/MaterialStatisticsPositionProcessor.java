package bishop.evaluationStatistics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import bishop.base.AdditiveMaterialEvaluator;
import bishop.base.Color;
import bishop.base.GameResult;
import bishop.base.IMaterialEvaluator;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.engine.TableMaterialEvaluator;
import math.IMatrix;
import math.IVector;
import math.MatrixImpl;
import math.VectorImpl;

/**
 * Processor that calculates evaluations of material constelation of positions. 
 * @author Ing. Petr Ležák
 */
public class MaterialStatisticsPositionProcessor implements IPositionProcessor {

	private static final int MIN_STABILITY = 4;
	private static final int MIN_TOTAL_COUNT = 10;
	private static final int MAX_PAWN_COUNT = 4;
	private static final int MIN_TOTAL_COUNT_FOR_EVALUATOR = 500;
	
	private final File tableEvaluatorFile;

	// Statistics for every material
	private final Map<MaterialHash, MaterialStatistics> statisticsMap = new HashMap<>();
	
	// Statistics for every normalized material; contains sum of statistics of corresponding unnormalized materials from statisticsMap
	private Map<MaterialHash, MaterialStatistics> symmetricalStatistics;
	
	// Statistics for every normalized material with zero pawns on white or black side;
	// contains sum of statistics of corresponding materials with same difference of pawns 
	private Map<MaterialHash, MaterialStatistics> pawnDifferentialStatistics;
	
	// Statistics for every normalized material with zero pieces on white or black side for every piece type;
	// contains sum of statistics of corresponding materials with same difference of pieces
	private Map<MaterialHash, MaterialStatistics> pieceDifferentialStatistics;

	// Evaluation of pieces (in multiplies of pawn)
	private final double[] pawnComplementForPieces = new double[PieceType.VARIABLE_LAST];
	
	// Evaluation of every material combination
	private final TableMaterialEvaluator tableEvaluator = new TableMaterialEvaluator(null);

	
	private MaterialHash prevHash;
	private int stability;
	private boolean processed;
	private GameResult result;

	
	public MaterialStatisticsPositionProcessor (final File tableEvaluatorFile) {
		this.tableEvaluatorFile = tableEvaluatorFile;
	}
	
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
		
	public void calculate() throws IOException {
		calculateSymmetricalStatistics();
		calculatePawnDifferentialStatistics();
		calculatePieceDifferentialStatistics();
		calculatePawnComplementForPieces();
		calculateAndWriteTableEvaluator();
	}
	
	private void calculateSymmetricalStatistics() {
		symmetricalStatistics = new HashMap<>();
		
		for (Map.Entry<MaterialHash, MaterialStatistics> entry: statisticsMap.entrySet()) {
			final MaterialHash hash = entry.getKey().copy();
			final MaterialStatistics statistics;
			
			if (hash.isBalancedExceptFor(PieceType.NONE))
				statistics = new MaterialStatistics(Integer.MAX_VALUE, 0.0);   // Fictional zero balance for balanced positions 
			else	
				statistics = entry.getValue().copy();
			
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
			
	private void normalizeHashAndStatistics(final MaterialHash hash, final MaterialStatistics statistics) {
		final MaterialHash oppositeHash = hash.getOpposite();
		oppositeHash.setOnTurn(Color.WHITE);
		
		if (hash.compareTo(oppositeHash) < 0) {
			hash.assign(oppositeHash);
			statistics.negate();
		}
	}

	// Calculates evaluation of every piece type by least squares method
	private void calculatePawnComplementForPieces() {
		final Map<MaterialHash, PawnComplement> pawnComplementMap = calculatePawnComplementMap(pieceDifferentialStatistics);
		
		final int equationCount = pawnComplementMap.size();
		final double[][] aElements = new double[equationCount][PieceType.PROMOTION_FIGURE_COUNT];
		final double[] bElements = new double[equationCount];
		final double[] weightsElements = new double[equationCount];
		
		int equationIndex = 0;
		
		for (PawnComplement complement: pawnComplementMap.values()) {
			final MaterialHash materialHash = complement.getMaterialHash();
			
			for (int j = 0; j < PieceType.PROMOTION_FIGURE_COUNT; j++) {
				final int pieceType = PieceType.PROMOTION_FIGURE_FIRST + j;
				aElements[equationIndex][j] = materialHash.getPieceCount(Color.WHITE, pieceType) - materialHash.getPieceCount(Color.BLACK, pieceType);
			}
			
			bElements[equationIndex] = complement.getComplement();
			weightsElements[equationIndex] = complement.getTotalCount();
			equationIndex++;
		}
		
		final IMatrix a = new MatrixImpl(aElements);
		final IVector b = new VectorImpl(bElements);
		final IVector weights = new VectorImpl(weightsElements);
		
		final IVector result = math.Utils.solveEquationsLeastSquare (a, b, weights);
		
		pawnComplementForPieces[PieceType.PAWN] = PieceTypeEvaluations.PAWN_EVALUATION;
		pawnComplementForPieces[PieceType.KING] = PieceTypeEvaluations.KING_EVALUATION;
		
		for (int j = 0; j < PieceType.PROMOTION_FIGURE_COUNT; j++) {
			final int pieceType = PieceType.PROMOTION_FIGURE_FIRST + j;
			final double complement = result.getElement(j);
			pawnComplementForPieces[pieceType] = result.getElement(j);
			
			System.out.println("Pawn complement for " + PieceType.getName(pieceType) + " is " + complement);
		}
	}

	// Calculates pawn complements from given differential statistics map. At least it must be differentiated by pawns.
	// Algorithm works this way. For every material with zero pawn on both sides it finds if it is win for white or black.
	// Then it adds pawns to the lost side until it finds material with opposite evaluation in the statistics map.
	// This way it finds two numbers of pawns (with difference 1) with opposite evaluations. The complement is then interpolated. 
	private static Map<MaterialHash, PawnComplement> calculatePawnComplementMap(final Map<MaterialHash, MaterialStatistics> differentialStatistics) {
		final Map<MaterialHash, PawnComplement> pawnComplementList = new HashMap<>();
		
		for (MaterialHash zeroPawnHash: differentialStatistics.keySet()) {
			if (zeroPawnHash.getPieceCount(Color.WHITE, PieceType.PAWN) == 0 && zeroPawnHash.getPieceCount(Color.BLACK, PieceType.PAWN) == 0) {
				final MaterialStatistics zeroPawnStatistics = differentialStatistics.get(zeroPawnHash);
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
					
					final MaterialStatistics nextStatistics = differentialStatistics.get(nextHash);
					
					if (nextStatistics == null)
						break;
					
					final double nextBalance = nextStatistics.getBalance();
					
					if (nextBalance * coeff >= 0) {
						final long totalCount = Math.min(prevStatistics.getTotalCount(), nextStatistics.getTotalCount());
						
						if (totalCount >= MIN_TOTAL_COUNT) {
							final double pawnCount = math.Utils.linearInterpolation(prevBalance, nextBalance, i - 1, i, 0);							
							final PawnComplement complement = new PawnComplement(zeroPawnHash, -pawnCount * coeff, totalCount);
							pawnComplementList.put(zeroPawnHash, complement);
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
	
	private void calculateAndWriteTableEvaluator() throws IOException {
		final Map<MaterialHash, PawnComplement> pawnComplementMap = calculatePawnComplementMap(pawnDifferentialStatistics);
		
		final IMaterialEvaluator additiveEvaluator = new AdditiveMaterialEvaluator() {
			@Override
			public int getPieceEvaluation(final int color, final int pieceType) {
				final double colorMultiplier = (color == Color.WHITE) ? +1 : -1;
				
				return PieceTypeEvaluations.getPawnMultiply(colorMultiplier * pawnComplementForPieces[pieceType]);
			}
		};
		
		for (int i = 0; i < TableMaterialEvaluator.TABLE_SIZE; i++) {
			final MaterialHash materialHash = TableMaterialEvaluator.getMaterialHashForIndex(i);
			final PawnComplement complement = pawnComplementMap.get(materialHash);
			
			int evaluation;
			
			if (complement != null && complement.getTotalCount() >= MIN_TOTAL_COUNT_FOR_EVALUATOR)
				evaluation = PieceTypeEvaluations.getPawnMultiply(complement.getComplement());
			else
				evaluation = additiveEvaluator.evaluateMaterial(materialHash);
			
			tableEvaluator.setEvaluationForIndex(i, evaluation);
			
			// Just a check
			if (tableEvaluator.evaluateMaterial(materialHash) != evaluation)
				throw new RuntimeException("Internal error - wrong evaluation");
		}
		
		tableEvaluator.write(tableEvaluatorFile);
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
