package bishop.engine;

public interface IPositionEvaluation {
	
	public int getEvaluation();
	public void clear(final int onTurn);
	public void addCoeffWithCount (final int index, final int count);
	public void addCoeff (final int index, final int color);
	public void addCoeff(final int index, final int color, final int count);
	public void shiftRight (final int shift);
}
