package bisGuiSwing.graphics;

import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.AEADBadTagException;
import javax.imageio.ImageIO;

import bisGui.graphics.Color;
import bisGui.graphics.GraphicContext;
import bisGui.graphics.IGraphics;
import bisGui.graphics.IImage;
import bisGui.math.IVector;
import bisGui.math.Vector2D;
import bisGui.math.Vectors;

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
	public void fillRect (final IVector position, final IVector size, final Color color) {
		setColor(color);
		
		final int x = Vector2D.getRoundedX(position);
		final int y = Vector2D.getRoundedY(position);
		final int width = Vector2D.getRoundedX(size);
		final int height = Vector2D.getRoundedY(size);

		graphics.fillRect(x, y, width, height);
	}
	
	@Override
	public void drawImage(final IImage image, final IVector position) {
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
