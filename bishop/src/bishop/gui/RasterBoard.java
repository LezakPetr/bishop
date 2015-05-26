package bishop.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import zip.ZipReader;
import bisGui.graphics.GraphicContext;
import bisGui.graphics.IImage;
import bishop.base.Color;
import bishop.controller.Utils;


public class RasterBoard extends BoardBase {

	private IImage boardImage;
	private final IImage[] coordinatesImages;
	private IImage markImage;

	
	public RasterBoard() {
		coordinatesImages = new IImage[Color.LAST];
	}
	
	public IImage getBoardImage() {
		return boardImage;
	}

	public void setBoardImage(final IImage image) {
		this.boardImage = image;
	}

	public IImage getMarkImage() {
		return markImage;
	}

	public void setMarkImage(final IImage image) {
		this.markImage = image;
	}
	
	public IImage getCoordinatesImage (final int orientation) {
		return coordinatesImages[orientation];
	}

	public void setCoordinatesImage (final int orientation, final IImage image) {
		coordinatesImages[orientation] = image;
	}

	/**
	 * Returns board scaled by given ratio.
	 * @param scale scale ratio
	 * @return scaled board
	 */
	public RasterBoard renderScaledBoard (final double scale) {
		final RasterBoard result = createScaledRasterBoard(scale);
		final GraphicContext context = GraphicContext.getInstance();
		
		result.setBoardImage (context.scaleImage(this.getBoardImage(), scale));
		result.setMarkImage (context.scaleImage(this.getMarkImage(), scale));
		
		for (int orientation = Color.FIRST; orientation < Color.LAST; orientation++) {
			result.setCoordinatesImage(orientation, context.scaleImage(this.getCoordinatesImage(orientation), scale));
		}

		return result;
	}
	
	protected void readMarkImage (final ZipReader zip, final String path) throws IOException {
		markImage = Utils.readImageFromZip(zip, path);
	}
	
	protected void readBoardImage (final ZipReader zip, final String path) throws IOException {
		boardImage = Utils.readImageFromZip(zip, path);
	}
	
	@Override
	protected void readCoordinatesImage (final ZipReader zip, final String path, final int color) throws IOException {
		coordinatesImages[color] = Utils.readImageFromZip(zip, path);
	}

}
