package bishop.interpreter;

import bishop.base.Color;
import bishop.base.PieceType;

public class Operators {
	
	public static final byte OPCODE_NEG = 16;
	public static final byte OPCODE_ADD = 17;
	public static final byte OPCODE_SUBTRACT = 18;
	public static final byte OPCODE_MULTIPLY = 19;
	public static final byte OPCODE_DIVIDE = 20;
	public static final byte OPCODE_NOT = 21;
	public static final byte OPCODE_AND = 22;
	public static final byte OPCODE_OR = 23;
	public static final byte OPCODE_XOR = 24;
	public static final byte OPCODE_SHL = 25;
	public static final byte OPCODE_SHR = 26;
	public static final byte OPCODE_IF = 27;
	public static final byte OPCODE_GREATER = 32;
	public static final byte OPCODE_GREATER_OR_EQUAL = 33;
	public static final byte OPCODE_LESS = 34;
	public static final byte OPCODE_LESS_OR_EQUAL = 35;
	public static final byte OPCODE_EQUAL = 36;
	public static final byte OPCODE_NOT_EQUAL = 37;
	public static final byte OPCODE_ON_TURN = 64;
	public static final byte OPCODE_PIECES_MASK = 65;
	public static final byte OPCODE_MIN_RANK = 66;
	public static final byte OPCODE_MAX_RANK = 67;
	public static final byte OPCODE_MIN_EDGE_DIST = 68;
	public static final byte OPCODE_MIN_DIST = 69;
	public static final byte OPCODE_FIRST_SQUARE = 70;
	public static final byte OPCODE_LAST_SQUARE = 71;
	public static final byte OPCODE_WHITE = 100;
	public static final byte OPCODE_BLACK = 101;
	public static final byte OPCODE_KING = 102;
	public static final byte OPCODE_QUEEN = 103;
	public static final byte OPCODE_ROOK = 104;
	public static final byte OPCODE_BISHOP = 105;
	public static final byte OPCODE_KNIGHT = 106;
	public static final byte OPCODE_PAWN = 107;
	
	
	private static OperatorRecord[] records = {
		// Arithmetic and logic
		new OperatorRecord(OPCODE_NEG, "neg", 1, OperatorNegate.class),
		new OperatorRecord(OPCODE_ADD, "+", 2, OperatorAdd.class),
		new OperatorRecord(OPCODE_SUBTRACT, "-", 2, OperatorSubtract.class),
		new OperatorRecord(OPCODE_MULTIPLY, "*", 2, OperatorMultiply.class),
		new OperatorRecord(OPCODE_DIVIDE, "/", 2, OperatorDivide.class),
		new OperatorRecord(OPCODE_NOT, "~", 1, OperatorNot.class),
		new OperatorRecord(OPCODE_AND, "&", 2, OperatorAnd.class),
		new OperatorRecord(OPCODE_OR, "|", 2, OperatorOr.class),
		new OperatorRecord(OPCODE_XOR, "^", 2, OperatorXor.class),
		new OperatorRecord(OPCODE_SHL, "<<", 2, OperatorShiftLeft.class),
		new OperatorRecord(OPCODE_SHR, ">>", 2, OperatorShiftRight.class),
		new OperatorRecord(OPCODE_IF, "?", 3, OperatorIf.class),
		
		// Comparison
		new OperatorRecord(OPCODE_GREATER, ">", 2, OperatorGreater.class),
		new OperatorRecord(OPCODE_GREATER_OR_EQUAL, ">=", 2, OperatorGreaterOrEqual.class),
		new OperatorRecord(OPCODE_LESS, "<", 2, OperatorLess.class),
		new OperatorRecord(OPCODE_LESS_OR_EQUAL, "<=", 2, OperatorLessOrEqual.class),
		new OperatorRecord(OPCODE_EQUAL, "==", 2, OperatorEqual.class),
		new OperatorRecord(OPCODE_NOT_EQUAL, "!=", 2, OperatorNotEqual.class),
		
		// Position
		new OperatorRecord(OPCODE_ON_TURN, "onTurn", 0, OperatorOnTurn.class),
		new OperatorRecord(OPCODE_PIECES_MASK, "piecesMask", 2, OperatorPiecesMask.class),
		new OperatorRecord(OPCODE_MIN_RANK, "minRank", 1, OperatorMinRank.class),
		new OperatorRecord(OPCODE_MAX_RANK, "maxRank", 1, OperatorMaxRank.class),
		new OperatorRecord(OPCODE_MIN_EDGE_DIST, "minEdgeDist", 1, OperatorMinEdgeDist.class),
		new OperatorRecord(OPCODE_MIN_DIST, "minDist", 2, OperatorMinDist.class),
		new OperatorRecord(OPCODE_FIRST_SQUARE, "firstSquare", 1, OperatorFirstSquare.class),
		new OperatorRecord(OPCODE_LAST_SQUARE, "lastSquare", 1, OperatorLastSquare.class),
		
		// Colors
		new OperatorRecord(OPCODE_WHITE, Color.NAME_WHITE, 0, OperatorWhite.class),
		new OperatorRecord(OPCODE_BLACK, Color.NAME_BLACK, 0, OperatorBlack.class),
		
		// Piece types
		new OperatorRecord(OPCODE_KING, PieceType.NAME_KING, 0, OperatorKing.class),
		new OperatorRecord(OPCODE_QUEEN, PieceType.NAME_QUEEN, 0, OperatorQueen.class),
		new OperatorRecord(OPCODE_ROOK, PieceType.NAME_ROOK, 0, OperatorRook.class),
		new OperatorRecord(OPCODE_BISHOP, PieceType.NAME_BISHOP, 0, OperatorBishop.class),
		new OperatorRecord(OPCODE_KNIGHT, PieceType.NAME_KNIGHT, 0, OperatorKnight.class),
		new OperatorRecord(OPCODE_PAWN, PieceType.NAME_PAWN, 0, OperatorPawn.class)
	};
	
	public static OperatorRecord getRecordForOpcode (final byte opcode) {
		for (OperatorRecord record: records) {
			if (record.getOpcode() == opcode)
				return record;
		}
		
		return null;
	}
	
	public static OperatorRecord getRecordForToken (final String token) {
		for (OperatorRecord record: records) {
			if (record.getToken().equals(token))
				return record;
		}
		
		return null;
	}

}
