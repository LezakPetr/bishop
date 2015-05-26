package bishop.tablebaseGenerator;

import utils.IoUtils;
import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.engine.DrawChecker;
import bishop.interpreter.Bytecode;

public class DefaultBytecodeCalculator {

	public static Bytecode calculateBytecode (final MaterialHash materialHash) {
		final String expression = calculateExpression(materialHash);
		System.out.println (expression);
		
		try {
			return Bytecode.parse(IoUtils.getPushbackReader(expression));
		}
		catch (Throwable ex) {
			throw new RuntimeException("Cannot calculate bytecode", ex);
		}
	}
	
	private static String calculateExpression(final MaterialHash materialHash) {
		if (!DrawChecker.hasMatingMaterial(materialHash, Color.WHITE) && !DrawChecker.hasMatingMaterial(materialHash, Color.BLACK))
			return calculateSignleModelExpression();
		
		boolean hasPawn = false;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			hasPawn |= (materialHash.getPieceCount(color, PieceType.PAWN) > 0);
		}
		
		if (hasPawn) {
			return getPawnExpression(materialHash);
		}
		else {
			return getNoPawnExpression(materialHash);
		}
	}

	private static String calculateSignleModelExpression() {
		return "0";
	}
	
	private static String getPawnExpression(final MaterialHash materialHash) {
		final StringBuilder builder = new StringBuilder();
		
		builder.append(Integer.toString(Color.WHITE));
		builder.append(" ");
		builder.append(Integer.toString(PieceType.KING));
		builder.append(" piecesMask minRank ");

		builder.append(Integer.toString(Color.BLACK));
		builder.append(" ");
		builder.append(Integer.toString(PieceType.KING));
		builder.append(" piecesMask minRank 10 * + ");
		
		builder.append(Integer.toString(Color.WHITE));
		builder.append(" ");
		builder.append(Integer.toString(PieceType.PAWN));
		builder.append(" piecesMask maxRank 100 * + ");

		builder.append(Integer.toString(Color.BLACK));
		builder.append(" ");
		builder.append(Integer.toString(PieceType.PAWN));
		builder.append(" piecesMask minRank 1000 * + ");

		return builder.toString();
	}

	private static String getNoPawnExpression(final MaterialHash materialHash) {
		final StringBuilder builder = new StringBuilder();
		final int evaluation = materialHash.getEvaluation();
		final int attacker = (evaluation >= 0) ? Color.WHITE : Color.BLACK;
		final int defender = Color.getOppositeColor(attacker);
		
		builder.append(Integer.toString(defender));
		builder.append(" ");
		builder.append(Integer.toString(PieceType.KING));
		builder.append(" piecesMask minEdgeDist 10 * ");

		builder.append(Integer.toString(defender));
		builder.append(" ");
		builder.append(Integer.toString(PieceType.KING));
		builder.append(" piecesMask ");
		builder.append(Integer.toString(attacker));
		builder.append(" ");
		builder.append(Integer.toString(PieceType.KING));
		builder.append(" piecesMask minDist + ");
		
		return builder.toString();
	}

}
