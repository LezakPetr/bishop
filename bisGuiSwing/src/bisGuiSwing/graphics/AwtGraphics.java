package bisGuiSwing.graphics;

import java.awt.Graphics2D;
import bisGui.graphics.Color;
import bisGui.graphics.GraphicContext;
import bisGui.graphics.IGraphics;
import bisGui.graphics.IImage;
import math.IVectorRead;
import math.Vector2D;

public class AwtGraphics implements IGraphics {

	private static final AwtGraphicContext CONTEXT = new AwtGraphicContext();
	private final Graphics2D graphics;
	
	public AwtGraphics (final Graphics2D graphics) {
		this.graphics = graphics;
	}
	
	private void setColor (final Color color) {
		final java.awt.Color awtColor = new java.awt.Color (color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		
		graphics.setColor(awtColor);
	}

	@Override
	public void fillRect (final IVectorRead position, final IVectorRead size, final Color color) {
		setColor(color);
		
		final int x = Vector2D.getRoundedX(position);
		final int y = Vector2D.getRoundedY(position);
		final int width = Vector2D.getRoundedX(size);
		final int height = Vector2D.getRoundedY(size);

		graphics.fillRect(x, y, width, height);
	}
	
	@Override
	public void drawImage(final IImage image, final IVectorRead position) {
		final int x = Vector2D.getRoundedX(position);
		final int y = Vector2D.getRoundedY(position);
		
		final AwtImage awtImage = (AwtImage) image;

		graphics.drawImage(awtImage.getImage(), x, y, null);
	}

	@Override
	public GraphicContext getContext() {
		return CONTEXT;
	}
	
	
}
