package bisGui.math;

import org.w3c.dom.Element;

public class Vector2D {
	
	public static final int COORDINATE_X = 0;
	public static final int COORDINATE_Y = 1;
	
	public static final IVector ZERO = fromComponents (0.0, 0.0);

	public static VectorImpl fromComponents(final double x, final double y) {
		final double[] components = {x, y};
		
		return new VectorImpl (components);
	}
	
	public static VectorImpl fromXmlElement (final Element element) {
		final double x = Double.parseDouble(element.getAttribute("x"));
		final double y = Double.parseDouble(element.getAttribute("y"));
		
		return fromComponents (x, y);
	}
	
	public static int getRoundedX (final IVector vector) {
		return (int) Math.round(vector.getElement(Vector2D.COORDINATE_X));
	}
	
	public static int getRoundedY (final IVector vector) {
		return (int) Math.round(vector.getElement(Vector2D.COORDINATE_Y));
	}

}
