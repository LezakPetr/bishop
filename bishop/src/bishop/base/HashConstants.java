package bishop.base;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashConstants {
	
	private static final long BLACK_ON_TURN_HASH;
	private static final long[] EP_FILE_HASHES;
	private static final long[] CASTLING_RIGHT_HASHES;
	
	public static final byte TYPE_BLACK_ON_TURN = 1;
	public static final byte TYPE_EP_FILE = 2;
	public static final byte TYPE_CASTLING_RIGHT = 3;
	public static final byte TYPE_PIECES = 4;
	
	private static long calculateHashConstant(final byte[] definition) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] digestBytes = digest.digest(definition);
			final int byteCount = Long.SIZE / Byte.SIZE;
			
			long hash = 0;
			
			for (int i = 0; i < byteCount; i++) {
				final long digit = digestBytes[i] & 0xFFL;
				
				hash = (hash << Byte.SIZE) | digit;
			}
			
			return hash;
		}
		catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException("Cannot calculate hash", ex);
		}
	}
	
	public static long calculateHashConstant(final byte type) {
		final byte[] definition = {type};
		
		return calculateHashConstant(definition);
	}

	public static long calculateHashConstant(final byte type, final int index) {
		final byte[] definition = {type, (byte) index};
		
		return calculateHashConstant(definition);
	}

	public static long calculateHashConstant(final byte type, final int index1, final int index2, final int index3) {
		final byte[] definition = {type, (byte) index1, (byte) index2, (byte) index3};
		
		return calculateHashConstant(definition);
	}

	static {
		// Black on turn
		BLACK_ON_TURN_HASH = calculateHashConstant(TYPE_BLACK_ON_TURN);
		
		// EP files
		EP_FILE_HASHES = new long[File.LAST];
				
		for (int file = File.FIRST; file < File.LAST; file++) {
			EP_FILE_HASHES[file] = calculateHashConstant(TYPE_EP_FILE, file);
		}
		
		// Castling rights
		CASTLING_RIGHT_HASHES = new long[CastlingRights.LAST_INDEX];
		
		for (int index = 0; index < CastlingRights.LAST_INDEX; index++)
			CASTLING_RIGHT_HASHES[index] = calculateHashConstant(TYPE_CASTLING_RIGHT, index);
	}
	
	public static long getEpFileHash (final int file) {
		return (file != File.NONE) ? EP_FILE_HASHES[file] : 0;
	}

	public static long getCastlingRightHash (final int rightIndex) {
		return CASTLING_RIGHT_HASHES[rightIndex];
	}
	
	public static long getOnTurnHash (final int onTurn) {
		return (onTurn == Color.BLACK) ? BLACK_ON_TURN_HASH : 0;
	}
	
	public static long getOnTurnHashDifference() {
		return BLACK_ON_TURN_HASH;
	}

}
