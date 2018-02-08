package bishop.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import collections.ImmutableList;
import utils.IoUtils;

public class PositionEvaluationFeatures {
	
	public static final double LINK_WEIGHT = 1024;
	public static final double GAME_STAGE_LINK_WEIGHT = 65536;
	
	public static final int FIRST = 0;
	
	private static final FeatureRegistry registry = new FeatureRegistry();

	public static final int EVALUATION_FEATURE = registry.add("evaluation");
	public static final int RULE_OF_SQUARE_BONUS = registry.add("rule_of_square");

	public static final List<GameStageFeatures> GAME_STAGE_FEATURES = createGameStageFeatures();
	
	public static final int MOBILITY_OFFSET = MobilityPositionEvaluator.registerFeatures(registry);
	
	public static final int LAST = registry.finish();
	
	private final short[] features = new short[LAST];
	
	
	private static List<GameStageFeatures> createGameStageFeatures() {
		final ImmutableList.Builder<GameStageFeatures> builder = ImmutableList.<GameStageFeatures>builder().withCapacity(GameStage.LAST);
		
		builder.add(new GameStageFeatures(registry, GameStage.PAWNS_ONLY));
		builder.addTimes(new GameStageFeatures(registry, GameStage.WITH_FIGURES_FIRST), GameStage.WITH_FIGURES_LAST - GameStage.WITH_FIGURES_FIRST);
		
		List<GameStageFeatures> featureList = builder.build();
		
		return featureList;
	}
	
	public int getEvaluationFeature (final int index) {
		return features[index];
	}
	
	public static FeatureRegistry getFeatureRegistry() {
		return registry;
	}
	
	public void setEvaluationFeatureCoeff (final int feature, final int coeff) {
		final short coeffAsShort = (short) coeff;
		
		if (coeffAsShort != coeff)
			throw new RuntimeException("Coeff " + feature + " out of range " + coeff);
		
		features[feature] = coeffAsShort;
	}

	public void read(final InputStream stream) throws IOException {
		for (int i = 0; i < LAST; i++)
			features[i] = (short) IoUtils.readSignedNumberBinary(stream, IoUtils.SHORT_BYTES);
	}
	
	public void write(final OutputStream stream) throws IOException {
		for (int i = 0; i < LAST; i++)
			IoUtils.writeNumberBinary(stream, features[i], IoUtils.SHORT_BYTES);
	}

}
