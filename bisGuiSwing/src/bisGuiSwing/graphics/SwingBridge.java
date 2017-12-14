package bisGuiSwing.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import bisGui.widgets.Widget;
import math.IVectorRead;
import math.Vector2D;
import bisGui.widgets.MouseButton;

@SuppressWarnings("serial")
public class SwingBridge extends JComponent {
	
	private final Widget widget;
	
	public SwingBridge (final Widget widget) {
		this.widget = widget;
		
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}
	
	protected void paintComponent (final Graphics g) {
		final Graphics2D graphics = (Graphics2D) g.create();
		
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
		widget.paintWidget(new AwtGraphics(graphics));
	}
	
	private IVectorRead getMouseEventPosition(final MouseEvent event) {
		final Point point = event.getPoint();
		final IVectorRead pointVector = Vector2D.fromComponents(point.getX(), point.getY());

		return pointVector;
	}
	
	private MouseButton getMouseButton(final MouseEvent event) {
		switch (event.getButton()) {
			case MouseEvent.BUTTON1:
				return MouseButton.LEFT;
				
			case MouseEvent.BUTTON2:
				return MouseButton.MIDDLE;

			case MouseEvent.BUTTON3:
				return MouseButton.RIGHT;
		}
		
		return null;
	}

	private MouseAdapter mouseListener = new MouseAdapter() {
		public void mousePressed (final MouseEvent event) {
			final MouseButton button = getMouseButton(event);
			final IVectorRead position = getMouseEventPosition(event);
			
			widget.mousePressed(position, button);
		}
		
		public void mouseDragged (final MouseEvent event) {
			final MouseButton button = getMouseButton(event);
			final IVectorRead position = getMouseEventPosition(event);
			
			widget.mouseDragged(position, button);
		}
		
		public void mouseReleased (final MouseEvent event) {
			final MouseButton button = getMouseButton(event);
			final IVectorRead position = getMouseEventPosition(event);
			
			widget.mouseReleased(position, button);
		}
	};



}
