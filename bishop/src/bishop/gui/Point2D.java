package bishop.gui;

import org.w3c.dom.Element;

public class Point2D {

	private final double x;
	private final double y;
	
	public Point2D (final double x, final double y) {
		this.x = x;
		this.y = y;
	}
	
	public static Point2D fromXmlElement (final Element element) {
		final double x = Double.parseDouble(element.getAttribute("x"));
		final double y = Double.parseDouble(element.getAttribute("y"));
		
		return new Point2D(x, y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}
