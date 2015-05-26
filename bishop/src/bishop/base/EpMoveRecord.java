package bishop.base;

public class EpMoveRecord {
	
	private final int beginSquare;
	private final int targetSquare;

	public EpMoveRecord(final int beginSquare, final int targetSquare) {
		this.beginSquare = beginSquare;
		this.targetSquare = targetSquare;
	}

	/**
	 * Returns begin square of EP move.
	 * @return begin square of EP move
	 */
	public int getBeginSquare() {
		return beginSquare;
	}

	/**
	 * Returns target square of EP move.
	 * @return target square of EP move
	 */
	public int getTargetSquare() {
		return targetSquare;
	}
	
	


}
