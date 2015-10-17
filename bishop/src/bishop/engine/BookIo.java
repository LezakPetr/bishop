package bishop.engine;

public class BookIo {
	protected static final int MOVE_SIZE = 2;
	protected static final int GOOD_MOVE_MASK = 0x8000;

	protected final int EVALUATION_BYTES = 3;
	
	protected static final byte[] HEADER_MAGIC = { (byte) 'B', (byte) 'O', (byte) 'O', (byte) 'K' };
	
	protected static final int HEADER_SIZE = 7;
}
