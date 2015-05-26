package bishop.controller;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import zip.ZipReader;
import zip.ZipRecord;
import bisGui.graphics.GraphicContext;
import bisGui.graphics.IImage;
import bisGui.math.IVector;
import bisGui.math.Vector2D;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;


public final class Utils {
	
	private static final String PGN_EXTENSION = "pgn";
	private static final String PROBLEM_EXTENSION = "prb";
	
	
	public static javax.swing.filechooser.FileFilter addProblemFileFilter(final JFileChooser chooser, final ILocalization localization) {
		final String description = localization.translateString ("ProblemFileFilter.description");
		final FileNameExtensionFilter filter = new FileNameExtensionFilter(description, PROBLEM_EXTENSION);
		
		chooser.addChoosableFileFilter(filter);
		
		return filter;
	}

	public static javax.swing.filechooser.FileFilter addPgnFileFilter(final JFileChooser chooser, final ILocalization localization) {
		final String description = localization.translateString ("PgnFileFilter.description");
		final FileNameExtensionFilter filter = new FileNameExtensionFilter(description, PGN_EXTENSION);
		
		chooser.addChoosableFileFilter(filter);
		
		return filter;
	}

	public static Element getElementByName (final Element element, final String name) {
		for (Node childNode = element.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
			if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(name))
				return (Element) childNode;
		}
		
		throw new RuntimeException ("Element " + name + " not found");
	}
	
	public static Element addChildElement (final Element parent, final String name) {
		final Document document = parent.getOwnerDocument();
		final Element child = document.createElement(name);
		
		parent.appendChild(child);
		
		return child;
	}

	public static IImage readImageFromZip (final ZipReader zip, final String path) throws IOException {
		final ZipRecord record = zip.getRecord(path);
		
		if (record == null)
			throw new RuntimeException("File " + path + " does not exist");
		
		final InputStream stream = record.getStream();
		
		try {
			return GraphicContext.getInstance().readImage(stream);
		}
		finally {
			stream.close();
		}
	}

	public static SVGDiagram readSvgFromZip (final ZipReader zip, final String path) throws IOException {
		final ZipRecord record = zip.getRecord(path);
		
		if (record == null)
			throw new RuntimeException("File " + path + " does not exist");
		
		final InputStream stream = record.getStream();
		
		try {
			final SVGUniverse universe = new SVGUniverse();
			final URI uri = universe.loadSVG(stream, path);
			
			return universe.getDiagram(uri);
		}
		finally {
			stream.close();
		}
	}

	public static final void centerWindow (final Window window) {
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final Dimension windowSize = window.getSize();
		final Dimension screenSize = toolkit.getScreenSize();
		
		window.setLocation((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2);
	}
	
	public static String timeToString (final long time) {
		return Long.toString(time / 1000);
	}
	
	public static long stringToTime (final String str) {
		return 1000 * Long.parseLong(str);
	}
	
	public static BufferedImage undersampleImage (final BufferedImage orig, final int multisamplingExponent) {
		final int totalExponent = 2 * multisamplingExponent;
		final int multisampling = 1 << multisamplingExponent;
		final int targetWidth = orig.getWidth() >> multisamplingExponent;
		final int targetHeight = orig.getHeight() >> multisamplingExponent;
		final BufferedImage target = new BufferedImage (targetWidth, targetHeight, orig.getType());
		final Raster origRaster = orig.getRaster();
		final WritableRaster targetRaster = target.getRaster();
		final int numBands = origRaster.getNumBands();
		final int[] origPixel = new int[numBands];
		final int[] targetPixel = new int[numBands];
		
		for (int x = 0; x < targetWidth; x++) {
			final int targetXBegin = multisampling * x;
			final int targetXEnd = targetXBegin + multisampling;

			for (int y = 0; y < targetHeight; y++) {
				Arrays.fill(targetPixel, 0);
				
				final int targetYBegin = multisampling * y;
				final int targetYEnd = targetYBegin + multisampling;
				
				for (int targetX = targetXBegin; targetX < targetXEnd; targetX++) {
					for (int targetY = targetYBegin; targetY < targetYEnd; targetY++) {
						origRaster.getPixel(targetX, targetY, origPixel);
						
						for (int band = 0; band < numBands; band++) {
							targetPixel[band] += origPixel[band];
						}
					}
				}

				for (int band = 0; band < numBands; band++) {
					targetPixel[band] >>= totalExponent;
				}

				targetRaster.setPixel(x, y, targetPixel);
			}
		}
		
		return target;
	}
	
	public static BufferedImage renderScaledSvg(final SVGDiagram svg, final IVector origSize, final double scale) throws SVGException {
		final int width = (int) Math.round(scale * origSize.getElement(Vector2D.COORDINATE_X));
		final int height = (int) Math.round(scale * origSize.getElement(Vector2D.COORDINATE_Y));
		
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.scale(scale, scale);
		
		svg.render(graphics);
		
		return image;
	}
}
