package bishop.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import utils.IoUtils;

public class PositionEvaluationCoeffs {
	
	public static final double LINK_WEIGHT = 256;
	
	public static final int FIRST = 0;
	private static final CoeffRegistry registry = new CoeffRegistry();
	
	public static final int RULE_OF_SQUARE_BONUS = registry.add("rule_of_square");
	public static final int PAWN_ON_SAME_COLOR_BONUS = registry.add("pawn_on_same_color");
	public static final int ROOK_ON_OPEN_FILE_BONUS = registry.add("rook_on_open_file");
	
	public static final int KING_MAIN_PROTECTION_PAWN_BONUS = registry.add("king_main_protection_pawn");
	public static final int KING_SECOND_PROTECTION_PAWN_BONUS = registry.add("king_second_protection_pawn");
	public static final int FIGURE_ON_SECURE_SQUARE_BONUS = registry.add("figure_on_secure_square");
	public static final int QUEEN_MOVE_BONUS = registry.add("queen_move");
	
	public static final int BISHOP_ATTACK_COEFF = registry.add("bishop_attack");
	public static final int ROOK_ATTACK_COEFF = registry.add("rook_attack");
	public static final int QUEEN_ATTACK_COEFF = registry.add("queen_attack");
	
	public static final TablePositionCoeffs MIDDLE_GAME_TABLE_EVALUATOR_COEFFS = new TablePositionCoeffs(registry, "middle_game_table_evaluator");
	public static final TablePositionCoeffs ENDING_TABLE_EVALUATOR_COEFFS = new TablePositionCoeffs(registry, "ending_table_evaluator");
	
	public static final int MOBILITY_OFFSET = MobilityPositionEvaluator.registerCoeffs(registry);
	
	public static final PawnStructureCoeffs MIDDLE_GAME_PAWN_STRUCTURE_COEFFS = new PawnStructureCoeffs(registry, "middle_game_pawn_structure", true);
	public static final PawnStructureCoeffs ENDING_WITH_FIGURES_PAWN_STRUCTURE_COEFFS = new PawnStructureCoeffs(registry, "ending_with_figures_pawn_structure", true);
	public static final PawnStructureCoeffs ENDING_PAWNS_ONLY_PAWN_STRUCTURE_COEFFS = new PawnStructureCoeffs(registry, "ending_pawns_only_pawn_structure", false);
	
	public static final int LAST = registry.finish();
	
	private final short[] coeffs = new short[LAST];
	
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
