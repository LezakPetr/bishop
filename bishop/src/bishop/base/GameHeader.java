package bishop.base;

public class GameHeader implements Copyable<GameHeader> {

	private String event;
	private String site;
	private String date;
	private String round;
	private String white;
	private String black;
	private GameResult result;
	
	public GameHeader() {
		event = "";
		site = "";
		date = "";
		round = "";
		white = "";
		black = "";
		result = GameResult.GAME_IN_PROGRESS;
	}
	
	public String getEvent() {
		return event;
	}
	
	public void setEvent(final String event) {
		this.event = event;
	}
	
	public String getSite() {
		return site;
	}
	
	public void setSite(final String site) {
		this.site = site;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(final String date) {
		this.date = date;
	}
	
	public String getRound() {
		return round;
	}
	
	public void setRound(final String round) {
		this.round = round;
	}
	
	public String getWhite() {
		return white;
	}
	
	public void setWhite(final String white) {
		this.white = white;
	}
	
	public String getBlack() {
		return black;
	}
	
	public void setBlack(final String black) {
		this.black = black;
	}
	
	public GameResult getResult() {
		return result;
	}
	
	public void setResult(final GameResult result) {
		this.result = result;
	}
	
	public String toString() {
		return white + " x " + black + " - " + event;
	}
	
	public void assign (final GameHeader orig) {
		this.event = orig.event;
		this.site = orig.site;
		this.date = orig.date;
		this.round = orig.round;
		this.white = orig.white;
		this.black = orig.black;
		this.result = orig.result;
	}

	public GameHeader copy() {
		final GameHeader copyHeader = new GameHeader();
		copyHeader.assign (this);
		
		return copyHeader;
	}
	
	
}
