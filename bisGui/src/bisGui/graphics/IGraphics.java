package bisGui.graphics;

import math.IVector;


public interface IGraphics {
	
	public void fillRect (final IVector position, final IVector size, final Color color);
	public void drawImage(final IImage image, final IVector position);
	public GraphicContext getContext();
}
