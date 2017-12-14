package bisGui.widgets;

import bisGui.graphics.IGraphics;
import math.IVectorRead;

public abstract class Widget {
	
	public void mousePressed (final IVectorRead position, final MouseButton button) {
	}
	
	public void mouseReleased (final IVectorRead position, final MouseButton button) {
	}

	public void mouseDragged (final IVectorRead position, final MouseButton button) {
	}
	
	public abstract IVectorRead getPosition();
	public abstract IVectorRead getSize();
	
	public abstract void paintWidget(final IGraphics graphics);
}
