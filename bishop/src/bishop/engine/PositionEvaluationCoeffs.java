package bishop.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import bishop.base.AdditiveMaterialEvaluator;
import bishop.base.IMaterialEvaluator;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import collections.ImmutableList;
import utils.IoUtils;

public class PositionEvaluationCoeffs {

	private static final int TABLE_EVALUATOR_COEFF_COUNT = 2;

	public static final int FIRST = 0;
	private static final CoeffRegistry registry = new CoeffRegistry();
	
	public static final int RULE_OF_SQUARE_SINGLE_PAWN_BONUS = registry.add("rule_of_square_single_pawn");
	public static final int RULE_OF_SQUARE_PAWN_RACE_BONUS = registry.add("rule_of_square_pawn_race");

	public static final List<GameStageCoeffs> GAME_STAGE_COEFFS = createGameStageCoeffs();
	public static final List<TablePositionCoeffs> TABLE_EVALUATOR_COEFFS = createTableEvaluatorCoeffs();

	public static final int MOBILITY_OFFSET = MobilityPositionEvaluator.registerCoeffs(registry);
	
	public static final int LAST = registry.finish();
	
	private final short[] coeffs = new short[LAST];
	private PieceTypeEvaluations pieceTypeEvaluations;
	
	private static List<GameStageCoeffs> createGameStageCoeffs() {
		final ImmutableList.Builder<GameStageCoeffs> builder = ImmutableList.<GameStageCoeffs>builder().withCapacity(GameStage.LAST);
		builder.addTimes(null, GameStage.FIRST);
		
		for (int i = GameStage.FIRST; i < GameStage.LAST; i++)
			builder.add(new GameStageCoeffs(registry, i));
		
		final List<GameStageCoeffs> coeffList = builder.build();

		return coeffList;
	}

	private static List<TablePositionCoeffs> createTableEvaluatorCoeffs() {
		final ImmutableList.Builder<TablePositionCoeffs> builder = ImmutableList.<TablePositionCoeffs>builder().withCapacity(TABLE_EVALUATOR_COEFF_COUNT);
		builder.addTimes(null, GameStage.FIRST);

		for (int i = 0; i < TABLE_EVALUATOR_COEFF_COUNT; i++) {
			registry.enterCategory("table_position_" + i);
			builder.add(new TablePositionCoeffs(registry));
			registry.leaveCategory();
		}

		return builder.build();
	}
	
	public int getEvaluationCoeff (final int index) {
		return coeffs[index];
	}
	
	public static CoeffRegistry getCoeffRegistry() {
		return registry;
	}
	
	public void setEvaluationCoeff (final int index, final int coeff) {
		final short coeffAsShort = (short) coeff;
		
		if (coeffAsShort != coeff)
			throw new RuntimeException("Coeff " + index + " out of range " + coeff);
		
		coeffs[index] = coeffAsShort;
	}

	public PieceTypeEvaluations getPieceTypeEvaluations() {
		return pieceTypeEvaluations;
	}

	public void setPieceTypeEvaluations(final PieceTypeEvaluations pieceTypeEvaluations) {
		this.pieceTypeEvaluations = pieceTypeEvaluations;
	}

	public void read(final InputStream stream) throws IOException {
		for (int i = 0; i < LAST; i++)
			coeffs[i] = (short) IoUtils.readSignedNumberBinary(stream, IoUtils.SHORT_BYTES);

		pieceTypeEvaluations = PieceTypeEvaluations.read (stream);
	}
	
	public void write(final OutputStream stream) throws IOException {
		for (int i = 0; i < LAST; i++)
			IoUtils.writeNumberBinary(stream, coeffs[i], IoUtils.SHORT_BYTES);

		pieceTypeEvaluations.write (stream);
	}

}
