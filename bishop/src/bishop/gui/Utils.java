package bishop.gui;

import math.IVectorRead;
import math.Vector2D;

class Utils {

	public static double getIconScale (final IVectorRead srcSize, final int targetWidth, final int targetHeight) {
		final double scaleX = (double) targetWidth / srcSize.getElement(Vector2D.COORDINATE_X);
		final double scaleY = (double) targetHeight / srcSize.getElement(Vector2D.COORDINATE_Y);
		final double scale = Math.min (scaleX, scaleY);
		
		return scale;
	}
}
