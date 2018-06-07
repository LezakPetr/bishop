package bishop.engine;

public interface IPawnEndingTable {
    public int getClassification (final int kingOnTurnSquare, final int kingNotOnTurnSquare, final int onTurn);
}
