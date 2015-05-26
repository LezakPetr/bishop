package bishop.base;

import java.util.Arrays;


/**
 * Representation of castling rights for both white and black player.
 * @author Ing. Petr Ležák
 */
public final class CastlingRights {
	
	// Rights for castling. Each right is represented by one bit of this number.
	private byte rights;
	

	// Full rights of both sides.
	private static final byte FULL_RIGHTS = 0x0F;

	// Table contains mask of castling rights that are preserved when given square changes its content.
	private static final byte[] TABLE_SQUARE_RIGHT_MASK = initializeTableSquareRightMask();

	// Table contains mask of castling rights for given color.
	private static final byte[] TABLE_COLOR_RIGHT_MASK = initializeTableColorRightMask();
	
	// Mask of all squares that affects castling rights
	public static final long AFFECTED_SQUARES = initializeAffectedSquares();
	
	
	public static final int FIRST_INDEX = 0;
	public static final int LAST_INDEX = FULL_RIGHTS + 1;
	
	
	/**
	 * Initializes TABLE_SQUARE_RIGHT_MASK.
	 * @return value of the table
	 */
	private static byte[] initializeTableSquareRightMask() {
		final byte[] table = new byte[Square.LAST];
		Arrays.fill(table, FULL_RIGHTS);

		table[Square.E1] &= ~getMaskOfRight (Color.WHITE, CastlingType.SHORT);
		table[Square.H1] &= ~getMaskOfRight (Color.WHITE, CastlingType.SHORT);

		table[Square.E1] &= ~getMaskOfRight (Color.WHITE, CastlingType.LONG);
		table[Square.A1] &= ~getMaskOfRight (Color.WHITE, CastlingType.LONG);

		table[Square.E8] &= ~getMaskOfRight (Color.BLACK, CastlingType.SHORT);
		table[Square.H8] &= ~getMaskOfRight (Color.BLACK, CastlingType.SHORT);

		table[Square.E8] &= ~getMaskOfRight (Color.BLACK, CastlingType.LONG);
		table[Square.A8] &= ~getMaskOfRight (Color.BLACK, CastlingType.LONG);
		
		return table;
	}
	
	/**
	 * Initializes TABLE_COLOR_RIGHT_MASK.
	 * @return value of the table
	 */
	private static byte[] initializeTableColorRightMask() {
		final byte[] table = new byte[Color.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			table[color] = (byte) (getMaskOfRight (color, CastlingType.SHORT) | getMaskOfRight (color, CastlingType.LONG));
		
		return table;
	}
	
	private static long initializeAffectedSquares() {
		long mask = 0;
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			if (TABLE_SQUARE_RIGHT_MASK[square] != FULL_RIGHTS)
				mask |= BitBoard.getSquareMask(square);
		}
		
		return mask;
	}


	/**
	 * Returns index of given castling right.
	 * @param color color of player
	 * @param type type of castling
	 * @return mask with one bit set corresponding to required right
	 */
	private static byte getMaskOfRight (final int color, final int type) {
		return (byte) (1 << (2*color + type));
	}

	/**
	 * Default constructor - creates cleared rights.
	 */
	public CastlingRights() {
		clearRights();
	}
      
	/**
	 * Initializes rights to full castling rights.
	 */
	public void setFullRights() {
    	rights = FULL_RIGHTS;
	}

    /**
     * Clears castling rights.
     */
    public void clearRights() {
    	rights = 0;
	}

    /**
     * Returns right to make castling with given color and type.
     * @param color color of player
     * @param type type of right
     * @return true if given player has right for given castling, false if not
     */
    public boolean isRight (final int color, final int type) {
    	return (rights & getMaskOfRight (color, type)) != 0;
    }
    
    /**
     * Checks if there is some right for castling of player of given color.
     * @param color color of player
     * @return true if given player has right for some castling, false if not
     */
    public boolean isRightForColor (final int color) {
    	return (rights & TABLE_COLOR_RIGHT_MASK[color]) != 0;
    }

	/**
	 * Checks if set of rights is empty e.g. if there is no right for castling.
	 * @return true if set of rights is empty, false if not
	 */
    public boolean isEmpty() {
        return rights == 0;
    }

    /**
     * Sets right for castling to given value.
     * @param color color of player
     * @param type type of castling
     * @param isRight true if required right should be set, false if it should be cleared
     */
    public void setRight (final int color, final int type, final boolean isRight) {
    	final byte mask = getMaskOfRight (color, type);

    	if (isRight)
    		rights |= mask;
    	else
    		rights &= ~mask;
    }

    /**
     * Updates (drops) right caused by changing content of given square.
     * @param square square that was changed
     */
    public void updateAfterSquareChange (final int square) {
        rights &= TABLE_SQUARE_RIGHT_MASK[square];
    }

    /**
     * Drops rights for castling of player with given color.
     * @param color color of player
     */
    public void dropRightsForColor (final int color) {
    	rights &= ~TABLE_COLOR_RIGHT_MASK[color];
    }
    
    /**
     * Assign given original rights into this.
     * @param orig original rights
     */
    public void assign (final CastlingRights orig) {
    	this.rights = orig.rights;
    }
    
    /**
     * Assigns right with swapped white and black side.
     * @param orig original rights
     */
	public void assignMirror(final CastlingRights orig) {
		this.rights = (byte) (((orig.rights & TABLE_COLOR_RIGHT_MASK[Color.WHITE]) << 2) | ((orig.rights & TABLE_COLOR_RIGHT_MASK[Color.BLACK]) >>> 2));
	}
    
	@Override
    public boolean equals (final Object obj) {
    	if (!(obj instanceof CastlingRights))
    		return false;
    	
    	final CastlingRights castlingRights = (CastlingRights) obj;
    	
    	return this.rights == castlingRights.rights;
    }
	
	@Override
	public int hashCode() {
		return rights;
	}
    
    /**
     * Returns index of this rights.
     * @return rights index
     */
    public int getIndex() {
    	return rights;
    }

    /**
     * Sets index of this rights.
     * @param index right index
     */
	public void setIndex(final int index) {
		rights = (byte) index;
	}

}
