package bishop.engine;

public class GameStageCoeffs {
	
	private final int firstCoeff;

	public final int pawnOnSameColorBonus;
	public final int rookOnOpenFileBonus;
	
	public final int kingMainProtectionPawnBonus;
	public final int kingSecondProtectionPawnBonus;
	public final int figureOnSecureSquareBonus;
	public final int queenMoveBonus;
	public final int kingAttackBonus;
	public final int onTurnBonus;
	
	private final int lastCoeff;
	
	
	public GameStageCoeffs(final CoeffRegistry registry, final int gameStage) {
		firstCoeff = registry.enterCategory("game_stage_" + gameStage);
		
		final boolean withFigures = gameStage != GameStage.PAWNS_ONLY;

		pawnOnSameColorBonus = (withFigures) ? registry.add("pawn_on_same_color") : -1;
		rookOnOpenFileBonus = (withFigures) ? registry.add("rook_on_open_file") : -1;
		figureOnSecureSquareBonus = (withFigures) ? registry.add("figure_on_secure_square") : -1;
		queenMoveBonus = (withFigures) ? registry.add("queen_move") : -1;
		
		kingMainProtectionPawnBonus = (withFigures) ? registry.add("king_main_protection_pawn") : -1;
		kingSecondProtectionPawnBonus = (withFigures) ? registry.add("king_second_protection_pawn") : -1;
		kingAttackBonus = (withFigures) ? registry.add("king_attack") : -1;
		
		onTurnBonus = registry.add("onTurn");
		
		lastCoeff = registry.leaveCategory();
	}
	
	public int getFirstCoeff() {
		return firstCoeff;
	}
	
	public int getLastCoeff() {
		return lastCoeff;
	}

	public int getCoeffCount() {
		return lastCoeff - firstCoeff;

	}

}
