package bishop.bookBuilder;

import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class Builder {
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final List<String> pgnList = Arrays.asList(args).subList(1, args.length);
		final BookStatistics statistics = new BookStatistics();
		statistics.addLinearGames(pgnList);
		statistics.storeBookToFile(args[0]);
	}
}
