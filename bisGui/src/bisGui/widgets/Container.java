package bisGui.widgets;

import java.util.LinkedList;
import java.util.List;

import bisGui.graphics.IGraphics;
import math.IVector;

public class Container extends Widget {
	
	private final List<Widget> children;
	private IVector position;
	private IVector size;
	
	
	public Container () {
		this.children = new LinkedList<Widget>();
	}

	@Override
	public IVector getPosition() {
		return position;
	}

	public void setPosition(final IVector size) {
		this.size = size;
	}

	@Override
	public IVector getSize() {
		return size;
	}

	public void setSize(final IVector position) {
		this.position = position;
	}

	@Override
	public void paintWidget(IGraphics graphics) {
		// TODO Auto-generated method stub
		
	}
}
