package bishop.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import collections.ImmutableList;
import utils.IoUtils;

public class PositionEvaluationCoeffs {
	
	public static final double LINK_WEIGHT = 1024;
	public static final double GAME_STAGE_LINK_WEIGHT = 65536;
	
	public static final int FIRST = 0;
	
	private static final CoeffRegistry registry = new CoeffRegistry();

	public static final int EVALUATION_COEFF = registry.add("evaluation");
	public static final int RULE_OF_SQUARE_BONUS = registry.add("rule_of_square");

	public static final List<GameStageCoeffs> GAME_STAGE_COEFFS = createGameStageCoeffs();
	
	public static final int MOBILITY_OFFSET = MobilityPositionEvaluator.registerCoeffs(registry);
	
	public static final int LAST = registry.finish();
	
	private final short[] coeffs = new short[LAST];
	
	
	private static List<GameStageCoeffs> createGameStageCoeffs() {
		final ImmutableList.Builder<GameStageCoeffs> builder = ImmutableList.<GameStageCoeffs>builder().withCapacity(GameStage.LAST);
		
		builder.add(new GameStageCoeffs(registry, GameStage.PAWNS_ONLY));
		builder.addTimes(new GameStageCoeffs(registry, GameStage.WITH_FIGURES_FIRST), GameStage.WITH_FIGURES_LAST - GameStage.WITH_FIGURES_FIRST);
		
		List<GameStageCoeffs> coeffList = builder.build();
		
		return coeffList;
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

	public void read(final InputStream stream) throws IOException {
		for (int i = 0; i < LAST; i++)
			coeffs[i] = (short) IoUtils.readSignedNumberBinary(stream, IoUtils.SHORT_BYTES);
	}
	
	public void write(final OutputStream stream) throws IOException {
		for (int i = 0; i < LAST; i++)
			IoUtils.writeNumberBinary(stream, coeffs[i], IoUtils.SHORT_BYTES);
	}

}
