package bishop.base;

import java.io.IOException;
import java.io.PushbackReader;


public interface INotationReader {
	/**
	 * Reads move from given reader.
	 * @param reader source reader
	 * @param position begin position of the move
	 * @param move read move will be assigned here
	 */
	public void readMove (final PushbackReader reader, final Position position, final Move move) throws IOException;

}
