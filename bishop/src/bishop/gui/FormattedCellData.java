package bishop.gui;

public class FormattedCellData {
	
	private final String text;
	private final java.awt.Color foreground;
	private final java.awt.Color background;
	
	public FormattedCellData(final String text, java.awt.Color foreground, final java.awt.Color background) {
		this.text = text;
		this.foreground = foreground;
		this.background = background;
	}

	public String getText() {
		return text;
	}

	public java.awt.Color getForeground() {
		return foreground;
	}

	public java.awt.Color getBackground() {
		return background;
	}

}
