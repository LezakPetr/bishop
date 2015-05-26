package bishop.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import zip.ZipReader;
import bisGui.graphics.IImage;
import bisGuiSwing.graphics.AwtImage;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.controller.Utils;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class SvgPieceSet extends PieceSetBase {

	private final SVGDiagram[][] pieceImages = new SVGDiagram[Color.LAST][PieceType.LAST];
	
	/**
	 * Returns piece set scaled by given ratio.
	 * @param scale scale ratio
	 * @return scaled board
	 */
	public RasterPieceSet renderScaledPieceSet (final double scale) {
		try {
			final RasterPieceSet result = createScaledRasterPieceSet(scale);
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
					final IImage image = new AwtImage(Utils.renderScaledSvg(this.pieceImages[color][pieceType], getPieceSize(), scale));
					result.setPieceImage(color, pieceType, image);
				}
			}
	
			return result;
		}
		catch (SVGException ex) {
			throw new RuntimeException("Cannot render SVG", ex);
		}
	}

	protected void readPieceImage (final ZipReader zip, final String path, final int color, final int pieceType) throws IOException {
		pieceImages[color][pieceType] = Utils.readSvgFromZip (zip, path);
	}
}
