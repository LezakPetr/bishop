package bishop.base;

import java.util.LinkedList;
import java.util.List;

import utils.ICharacterFilter;

public abstract class Pgn {
	
	protected static final char BEGIN_TAG_CHAR = '[';
	protected static final char END_TAG_CHAR = ']';

	protected static final char BEGIN_COMMENTARY_CHAR = '{';
	protected static final char END_COMMENTARY_CHAR = '}';

	protected static final String TAG_EVENT = "Event";
	protected static final String TAG_SITE = "Site";
	protected static final String TAG_DATE = "Date";
	protected static final String TAG_ROUND = "Round";
	protected static final String TAG_WHITE = "White";
	protected static final String TAG_BLACK = "Black";
	protected static final String TAG_RESULT = "Result";
	protected static final String TAG_FEN = "FEN";
	
	protected static final char ANNOTATION_MARK = '$';
	protected static final String PGN_ENCODING = "UTF-8";

	
	protected static final ICharacterFilter nonMoveTokenFilter = ch -> {
		if (Character.isDigit(ch) || ch == '.')
			return true;

		for (GameResult result: GameResult.values()) {
			if (result.toString().indexOf(ch) >= 0)
				return true;
		}

		return false;
	};
		
	public Pgn() {
		gameList = new LinkedList<>();
	}
	
	protected final List<Game> gameList;
	
	public List<Game> getGameList() {
		return gameList;
	}

}
