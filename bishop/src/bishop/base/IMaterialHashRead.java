package bishop.base;

import utils.ICopyable;

public interface IMaterialHashRead extends IPieceCounts, ICopyable<MaterialHash>, Comparable<MaterialHash> {
	public int getOnTurn();
	public MaterialHash getOpposite();
	public String getMaterialString();
	public int getTotalPieceCount();
	
	/**
	 * Method defines order between material hashes.
	 * @param cmpHash compared material hash
	 * @return true if this hash is greater than cmpHash
	 */
	public boolean isGreater(final MaterialHash cmpHash);
	
	/**
	 * Checks if the material hash contains same number of corresponding white and black pieces except for given piece type.
	 * @param exceptPieceType piece type with allowed different count
	 */
	public boolean isBalancedExceptFor(final int exceptPieceType);
	
	public long getHash();
	
	public boolean isAloneKing(final int color);
	public boolean hasQueenRookOrPawn();
	public boolean hasQueenRookOrPawnOnSide(final int color);
}
