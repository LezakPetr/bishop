package bishop.bookBuilder;

import java.util.Arrays;
import java.util.List;

public class Builder {
	public static void main(final String[] args) throws Exception {
		final List<String> pgnList = Arrays.asList(args).subList(1, args.length);
		final BookStatistics statistics = new BookStatistics();
		statistics.addLinearGames(pgnList);
		statistics.storeBookToFile(args[0]);
	}
}
