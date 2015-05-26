package bisGui.graphics;

public class Color {
	
	private final float red;
	private final float green;
	private final float blue;
	private final float alpha;
	
	public Color (final float red, final float green, final float blue) {
		this (red, green, blue, 1.0f);
	}
	
	public Color (final float red, final float green, final float blue, final float alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}
	
	public float getRed() {
		return red;
	}

	public float getGreen() {
		return green;
	}

	public float getBlue() {
		return blue;
	}

	public float getAlpha() {
		return alpha;
	} 
	
	public static final Color BLACK = new Color (0.0f, 0.0f, 0.0f);
	public static final Color WHITE = new Color (1.0f, 1.0f, 1.0f);

}
