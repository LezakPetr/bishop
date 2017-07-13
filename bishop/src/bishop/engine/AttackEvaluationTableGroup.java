package bishop.engine;

public class AttackEvaluationTableGroup {

	private final LineAttackEvaluationTable[] lineAttackTables;
	private final ShortAttackEvaluationTable[] knightAttackTables;
	private final ShortAttackEvaluationTable[] pawnAttackTables;
	
	
	public AttackEvaluationTableGroup (final LineAttackEvaluationTable whiteLineTable, final LineAttackEvaluationTable blackLineTable, final ShortAttackEvaluationTable whiteKnightTable, final ShortAttackEvaluationTable blackKnightTable, final ShortAttackEvaluationTable whitePawnTable, final ShortAttackEvaluationTable blackPawnTable) {
		this.lineAttackTables = new LineAttackEvaluationTable[] { whiteLineTable, blackLineTable };
		this.knightAttackTables = new ShortAttackEvaluationTable[] { whiteKnightTable, blackKnightTable };
		this.pawnAttackTables = new ShortAttackEvaluationTable[] { whitePawnTable, blackPawnTable };
	}
	
	public LineAttackEvaluationTable getLineAttackTable(final int kingColor) {
		return lineAttackTables[kingColor];
	}


	public ShortAttackEvaluationTable getKnightTable(final int kingColor) {
		return knightAttackTables[kingColor];
	}
	
	public ShortAttackEvaluationTable getPawnTable(final int kingColor) {
		return pawnAttackTables[kingColor];
	}

	public static final AttackEvaluationTableGroup ZERO_GROUP = new AttackEvaluationTableGroup(LineAttackEvaluationTable.ZERO_TABLE, LineAttackEvaluationTable.ZERO_TABLE, ShortAttackEvaluationTable.ZERO_TABLE, ShortAttackEvaluationTable.ZERO_TABLE, ShortAttackEvaluationTable.ZERO_TABLE, ShortAttackEvaluationTable.ZERO_TABLE);
}

