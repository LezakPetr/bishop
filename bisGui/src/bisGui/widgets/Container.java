package bisGui.widgets;

import java.util.LinkedList;
import java.util.List;

import bisGui.graphics.IGraphics;
import math.IVectorRead;

public class Container extends Widget {
	
	private final List<Widget> children;
	private IVectorRead position;
	private IVectorRead size;
	
	
	public Container () {
		this.children = new LinkedList<Widget>();
	}

	@Override
	public IVectorRead getPosition() {
		return position;
	}

	public void setPosition(final IVectorRead size) {
		this.size = size;
	}

	@Override
	public IVectorRead getSize() {
		return size;
	}

	public void setSize(final IVectorRead position) {
		this.position = position;
	}

	@Override
	public void paintWidget(IGraphics graphics) {
		// TODO Auto-generated method stub
		
	}

	public List<Widget> getChildren() {
		return children;
	}
}
