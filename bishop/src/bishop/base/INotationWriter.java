package bishop.base;

import java.io.PrintWriter;

public interface INotationWriter {
	/**
	 * Writes move into given writer.
	 * @param writer target writer
	 * @param position begin position of move
	 * @param move move to write
	 */
	public void writeMove (final PrintWriter writer, final Position position, final Move move);
}
