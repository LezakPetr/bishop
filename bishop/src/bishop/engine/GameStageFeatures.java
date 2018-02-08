package bishop.engine;

public class GameStageFeatures {
	
	private final int firstFeature;
	
	public final TablePositionFeatures tableEvaluatorFeatures;
	public final PawnStructureFeatures pawnStructureFeatures; 
	public final int pawnOnSameColorBonus;
	public final int rookOnOpenFileBonus;
	
	public final int kingMainProtectionPawnBonus;
	public final int kingSecondProtectionPawnBonus;
	public final int figureOnSecureSquareBonus;
	public final int queenMoveBonus;
	public final int kingAttackBonus;
	
	private final int lastFeature;
	
	
	public GameStageFeatures(final FeatureRegistry registry, final int gameStage) {
		firstFeature = registry.enterCategory("game_stage_" + gameStage);
		
		final boolean withFigures = gameStage != GameStage.PAWNS_ONLY;
		
		tableEvaluatorFeatures = new TablePositionFeatures(registry);
		pawnStructureFeatures = new PawnStructureFeatures(registry, withFigures);
		
		pawnOnSameColorBonus = (withFigures) ? registry.add("pawn_on_same_color") : -1;
		rookOnOpenFileBonus = (withFigures) ? registry.add("rook_on_open_file") : -1;
		figureOnSecureSquareBonus = (withFigures) ? registry.add("figure_on_secure_square") : -1;
		queenMoveBonus = (withFigures) ? registry.add("queen_move") : -1;
		
		kingMainProtectionPawnBonus = (withFigures) ? registry.add("king_main_protection_pawn") : -1;
		kingSecondProtectionPawnBonus = (withFigures) ? registry.add("king_second_protection_pawn") : -1;
		kingAttackBonus = (withFigures) ? registry.add("king_attack") : -1;
		
		lastFeature = registry.leaveCategory();
	}
	
	public int getFirstCoeff() {
		return firstFeature;
	}
	
	public int getLastCoeff() {
		return lastFeature;
	}

}
