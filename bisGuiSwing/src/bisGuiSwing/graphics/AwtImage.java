package bisGuiSwing.graphics;

import java.awt.image.BufferedImage;

import bisGui.graphics.IImage;

public class AwtImage implements IImage {
	
	private final BufferedImage image;
	
	public AwtImage (final BufferedImage image) {
		this.image = image;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
}
