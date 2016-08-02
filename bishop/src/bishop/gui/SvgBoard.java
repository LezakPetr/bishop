package bishop.gui;

import java.io.IOException;

import zip.ZipReader;
import bishop.base.Color;
import bishop.controller.Utils;
import math.IVector;
import math.Vector2D;
import bisGuiSwing.graphics.AwtImage;

import com.kitfox.svg.SVGDiagram;

public class SvgBoard extends BoardBase {
	
	private SVGDiagram boardImage;
	private final SVGDiagram[] coordinatesImages;
	private SVGDiagram markImage;
	
	public SvgBoard() {
		coordinatesImages = new SVGDiagram[Color.LAST];
	}

	public RasterBoard renderScaledBoard(final double scale) {
		try {
			final RasterBoard result = createScaledRasterBoard(scale);
			
			final IVector boardSize = Vector2D.fromComponents(boardImage.getWidth(), boardImage.getHeight());
			result.setBoardImage (new AwtImage(Utils.renderScaledSvg(boardImage, boardSize, scale)));
			
			final IVector markSize = Vector2D.fromComponents(markImage.getWidth(), markImage.getHeight());
			result.setMarkImage (new AwtImage(Utils.renderScaledSvg(markImage, markSize, scale)));
			
			for (int orientation = Color.FIRST; orientation < Color.LAST; orientation++) {
				result.setCoordinatesImage(orientation, new AwtImage(Utils.renderScaledSvg(coordinatesImages[orientation], boardSize, scale)));
			}

			return result;
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot render board", ex);
		}
	}

	@Override
	protected void readMarkImage(final ZipReader zip, final String path) throws IOException {
		markImage = Utils.readSvgFromZip(zip, path);
	}

	@Override
	protected void readBoardImage(final ZipReader zip, final String path) throws IOException {
		boardImage = Utils.readSvgFromZip(zip, path);
	}

	@Override
	protected void readCoordinatesImage (final ZipReader zip, final String path, final int color) throws IOException {
		coordinatesImages[color] = Utils.readSvgFromZip(zip, path);
	}
}
