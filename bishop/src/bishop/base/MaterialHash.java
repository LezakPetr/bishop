package bishop.base;

import java.io.PrintWriter;
import java.io.StringWriter;

import bishop.tablebase.FileNameCalculator;

public final class MaterialHash implements IMaterialHashRead {
	
	private static final long HASH_CODE_COEFF = 0xb2c9ae1182d84b7bL;
	private static final int HASH_CODE_SHIFT = 32;
	
	private long hash;
	
	public MaterialHash() {
		clear();
	}

	public MaterialHash(final IPieceCounts pieceCounts, final int onTurn) {
		clear();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
				final int count = pieceCounts.getPieceCount (color, pieceType);
				
				addPiece(color, pieceType, count);
			}
		}
		
		setOnTurn(onTurn);
	}

	public MaterialHash (final String definition, final int onTurn) {
		clear();
		
		int index = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
				final int count = Character.getNumericValue(definition.charAt(index));
				this.addPiece(color, pieceType, count);
								
				index++;
			}
			
			index = definition.indexOf(FileNameCalculator.MATERIAL_SEPARATOR) + 1;
		}
		
		this.setOnTurn(onTurn);
	}

	public void changeToOpposite () {
		final long whiteHash = hash & MaterialHashConstants.WHITE_COLOR_MASK;
		final long blackHash = hash & MaterialHashConstants.BLACK_COLOR_MASK;
		final long onTurn = hash & MaterialHashConstants.ON_TURN_MASK;
		
		hash = (whiteHash << MaterialHashConstants.BITS_PER_ITEM) | (blackHash >>> MaterialHashConstants.BITS_PER_ITEM) | (onTurn ^ MaterialHashConstants.ON_TURN_MASK);
	}
	
	@Override
	public int getPieceCount (final int color, final int pieceType) {
		if (pieceType == PieceType.KING) {
			return 1;
		}
		else {
			final int offset = MaterialHashConstants.getOffset(color, pieceType);
			
			return (int) ((hash >>> offset) & MaterialHashConstants.ITEM_MASK);
		}
	}

	public void addPiece(final int color, final int pieceType, final int count) {
		final int offset = MaterialHashConstants.getOffset(color, pieceType);
		
		hash += (long) count << offset;
	}

	public void addPiece(final int color, final int pieceType) {
		hash += MaterialHashConstants.getPieceIncrement(color, pieceType);
	}

	public void removePiece(final int color, final int pieceType, final int count) {
		addPiece(color, pieceType, -count);
	}
	
	public void removePiece(final int color, final int pieceType) {
		hash -= MaterialHashConstants.getPieceIncrement(color, pieceType);
	}

	public int getOnTurn() {
		return (int) ((hash & MaterialHashConstants.ON_TURN_MASK) >>> MaterialHashConstants.ON_TURN_OFFSET);
	}

	public void swapOnTurn() {
		hash ^= MaterialHashConstants.ON_TURN_MASK;
	}
	
	public void setOnTurn(final int onTurn) {
		hash = (hash & ~MaterialHashConstants.ON_TURN_MASK) | ((long) onTurn << MaterialHashConstants.ON_TURN_OFFSET);
	}

	public MaterialHash copy() {
		final MaterialHash copyObj = new MaterialHash();
		copyObj.assign (this);
		
		return copyObj;
	}

	public void assign(final MaterialHash orig) {
		this.hash = orig.hash;
	}

	public void clear() {
		this.hash = MaterialHashConstants.HASH_ALONE_KINGS;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof MaterialHash))
			return false;
		
		final MaterialHash cmp = (MaterialHash) obj;
		
		return this.hash == cmp.hash;
	}
	
	@Override
	public int hashCode() {
		return (int) ((hash * HASH_CODE_COEFF) >>> HASH_CODE_SHIFT);
	}

	@Override
	public MaterialHash getOpposite() {
		final MaterialHash opposite = this.copy();
		opposite.changeToOpposite();
		
		return opposite;
	}

	@Override
	public String getMaterialString() {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			if (color != Color.FIRST)
				printWriter.append('-');
			
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final int count = getPieceCount(color, pieceType);
				
				for (int i = 0; i < count; i++) {
					PieceType.write(printWriter, pieceType, false);
				}
			}
		}
		
		printWriter.flush();
		
		return stringWriter.toString();
	}
	
	@Override
	public String toString() {
		return getMaterialString() + '-' + Color.getNotation(getOnTurn());
	}
	
	@Override
	public int getTotalPieceCount() {
		int count = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				count += getPieceCount(color, pieceType);
			}
		}
		
		return count;
	}

	@Override
	public int compareTo(final MaterialHash cmp) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final int thisCount = this.getPieceCount(color, pieceType);
				final int cmpCount = cmp.getPieceCount(color, pieceType);
				
				if (thisCount < cmpCount)
					return -1;
				
				if (thisCount > cmpCount)
					return +1;
			}
		}
		
		return this.getOnTurn() - cmp.getOnTurn();
	}

	public MaterialHash[] getBothSideHashes() {
		final MaterialHash[] materialHashArray = new MaterialHash[Color.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			materialHashArray[color] = this.copy();
			materialHashArray[color].setOnTurn(color);
		}

		return materialHashArray;
	}

	@Override
	public boolean isGreater(final MaterialHash cmpHash) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
				final int thisCount = this.getPieceCount(color, pieceType);
				final int cmpCount = cmpHash.getPieceCount(color, pieceType);
				
				if (thisCount > cmpCount)
					return true;
				
				if (thisCount < cmpCount)
					return false;
			}
		}
		
		return false;
	}

	@Override
	public boolean isBalancedExceptFor(final int exceptPieceType) {
		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
			if (pieceType != exceptPieceType && getPieceCount(Color.WHITE, pieceType) != getPieceCount(Color.BLACK, pieceType))
				return false;
		}
		
		return true;
	}

	public void reduceToDifference() {
		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
			reducePieceToDifference(pieceType);
		}
	}

	public void reducePieceToDifference(final int pieceType) {
		final int whiteCount = getPieceCount(Color.WHITE, pieceType);
		final int blackCount = getPieceCount(Color.BLACK, pieceType);
		final int toRemove = Math.min(whiteCount, blackCount);
		
		removePiece(Color.WHITE, pieceType, toRemove);
		removePiece(Color.BLACK, pieceType, toRemove);
	}
	
	@Override
	public long getHash() {
		return hash;
	}
	
	@Override
	public boolean isAloneKing(final int color) {
		return (hash & MaterialHashConstants.getColorMask(color)) == 0;
	}

	@Override
	public boolean hasQueenRookOrPawn() {
		return (hash & MaterialHashConstants.QUEEN_ROOK_OR_PAWN_BOTH_COLOR_MASK) != 0;
	}
	
	@Override
	public boolean hasQueenRookOrPawnOnSide(final int color) {
		return (hash & MaterialHashConstants.getQueenRookOrPawnOnSide(color)) != 0;
	}
}
