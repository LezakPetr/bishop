package bisGui.widgets;

import bisGui.graphics.IGraphics;
import bisGui.math.IVector;

public abstract class Widget {
	
	public void mousePressed (final IVector position, final MouseButton button) {
	}
	
	public void mouseReleased (final IVector position, final MouseButton button) {
	}

	public void mouseDragged (final IVector position, final MouseButton button) {
	}
	
	public abstract IVector getPosition();
	public abstract IVector getSize();
	
	public abstract void paintWidget(final IGraphics graphics);
}
