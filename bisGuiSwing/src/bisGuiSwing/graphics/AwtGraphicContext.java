package bisGuiSwing.graphics;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import bisGui.graphics.GraphicContext;
import bisGui.graphics.IImage;

public class AwtGraphicContext extends GraphicContext {
	@Override
	public IImage readImage (final InputStream stream) throws IOException {
		return new AwtImage(ImageIO.read(stream));
	}
	
	@Override
	public IImage scaleImage (final IImage orig, final double scale) {
		final AwtImage origAwtImage = (AwtImage) orig;
		final BufferedImage origBaseImage = origAwtImage.getImage();
		
		final int scaledWidth = (int) Math.round(scale * origBaseImage.getWidth());
		final int scaledHeight = (int) Math.round(scale * origBaseImage.getHeight());
		final Image scaledImage = origBaseImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_AREA_AVERAGING);
		
		final BufferedImage targetImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB_PRE);
		final Graphics graphics = targetImage.getGraphics();
		
		graphics.drawImage(scaledImage, 0, 0, null);
		
		return new AwtImage(targetImage);
	}
	

}
