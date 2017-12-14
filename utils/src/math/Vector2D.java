package math;

import org.w3c.dom.Element;

public class Vector2D {
	
	public static final int COORDINATE_X = 0;
	public static final int COORDINATE_Y = 1;
	
	public static final IVectorRead ZERO = fromComponents (0.0, 0.0);

	public static DenseVector fromComponents(final double x, final double y) {
		final double[] components = {x, y};
		
		return new DenseVector (components);
	}
	
	public static DenseVector fromXmlElement (final Element element) {
		final double x = Double.parseDouble(element.getAttribute("x"));
		final double y = Double.parseDouble(element.getAttribute("y"));
		
		return fromComponents (x, y);
	}
	
	public static int getRoundedX (final IVectorRead vector) {
		return (int) Math.round(vector.getElement(Vector2D.COORDINATE_X));
	}
	
	public static int getRoundedY (final IVectorRead vector) {
		return (int) Math.round(vector.getElement(Vector2D.COORDINATE_Y));
	}

}
