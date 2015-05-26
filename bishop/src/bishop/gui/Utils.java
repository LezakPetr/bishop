package bishop.gui;

import bisGui.math.IVector;
import bisGui.math.Vector2D;

class Utils {

	public static double getIconScale (final IVector srcSize, final int targetWidth, final int targetHeight) {
		final double scaleX = (double) targetWidth / srcSize.getElement(Vector2D.COORDINATE_X);
		final double scaleY = (double) targetHeight / srcSize.getElement(Vector2D.COORDINATE_Y);
		final double scale = Math.min (scaleX, scaleY);
		
		return scale;
	}
}
