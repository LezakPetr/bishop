package bisGui.graphics;

import java.io.IOException;
import java.io.InputStream;

public abstract class GraphicContext {
	
	private static GraphicContext instance;
	
	public static GraphicContext getInstance() {
		return instance;
	}

	public static void setInstance(final GraphicContext instance) {
		GraphicContext.instance = instance;
	}
	
	
	public abstract IImage readImage (final InputStream stream) throws IOException;
	public abstract IImage scaleImage (final IImage orig, final double scale);
}
