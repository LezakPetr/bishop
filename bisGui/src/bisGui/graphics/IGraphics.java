package bisGui.graphics;

import math.IVectorRead;


public interface IGraphics {
	
	public void fillRect (final IVectorRead position, final IVectorRead size, final Color color);
	public void drawImage(final IImage image, final IVectorRead position);
	public GraphicContext getContext();
}
