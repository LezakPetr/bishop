package bishop.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import zip.ZipReader;
import bisGui.graphics.GraphicContext;
import bisGui.graphics.IImage;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.controller.Utils;


/**
 * RasterPieceSet represents set of raster piece images.
 * @author Ing. Petr Ležák
 */
public class RasterPieceSet extends PieceSetBase {

	private final IImage[][] pieceImages = new IImage[Color.LAST][PieceType.LAST];

	/**
	 * Returns image of requested piece.
	 * @param color color of the piece
	 * @param pieceType type of the piece
	 * @return piece image
	 */
	public IImage getPieceImage (final int color, final int pieceType) {
		return pieceImages[color][pieceType];
	}

	/**
	 * Sets image of requested piece.
	 * @param color color of the piece
	 * @param pieceType type of the piece
	 * @param image piece image
	 */
	public void setPieceImage (final int color, final int pieceType, final IImage image) {
		pieceImages[color][pieceType] = image;
	}

	/**
	 * Returns piece set scaled by given ratio.
	 * @param scale scale ratio
	 * @return scaled board
	 */
	public RasterPieceSet renderScaledPieceSet (final double scale) {
		final RasterPieceSet result = createScaledRasterPieceSet(scale);
		final GraphicContext context = GraphicContext.getInstance();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				result.pieceImages[color][pieceType] = context.scaleImage(this.pieceImages[color][pieceType], scale);
			}
		}

		return result;
	}
	
	protected void readPieceImage (final ZipReader zip, final String path, final int color, final int pieceType) throws IOException {
		pieceImages[color][pieceType] = Utils.readImageFromZip (zip, path);
	}

}
