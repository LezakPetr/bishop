package bishop.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import zip.ZipReader;
import zip.ZipRecord;
import bishop.base.Color;
import bishop.controller.Utils;
import math.IVectorRead;
import math.Vector2D;
import math.Vectors;

public abstract class BoardBase implements IBoard {
	private String name;
	private IVectorRead minSquareCorner;
	private IVectorRead maxSquareCorner;
	private IVectorRead boardCenterPoint;
	private IVectorRead markCenterPoint;
	

	private static final String MANIFEST_PATH = "manifest.xml";
	private static final String INFO_ELEMENT = "info";
	private static final String INFO_NAME_ELEMENT = "name";
	private static final String BOARD_DIMENSIONS_ELEMENT = "dimensions";
	private static final String BOARD_BACKGROUND_ELEMENT = "background";
	private static final String BOARD_COORDINATES_ELEMENT = "coordinates";
	private static final String BOARD_COORDINATES_ORIENTATION_ATTRIBUTE = "orientation";
	private static final String BOARD_DIMENSIONS_MIN_SQUARE_CORNER_ELEMENT = "min_square_corner";
	private static final String BOARD_DIMENSIONS_MAX_SQUARE_CORNER_ELEMENT = "max_square_corner";
	private static final String BOARD_DIMENSIONS_CENTER_ELEMENT = "center";
	private static final String BOARD_ELEMENT = "board";
	private static final String FILE_TYPE_VALUE = "raster_board";
	private static final String FILE_TYPE_ATTRIBUTE = "file_type";
	private static final String MARK_ELEMENT = "mark";
	private static final String MARK_DIMENSIONS_ELEMENT = "dimensions";
	private static final String MARK_DIMENSIONS_CENTER_ELEMENT = "center";
	private static final String MARK_FILE_ELEMENT = "file";
	
	
	/**
	 * Reads image of the board. 
	 * @param zip zip file with board
	 * @param boardElement board element
	 * @throws IOException if IO fails
	 */
	private void readBoard (final ZipReader zip, final Element boardElement) throws IOException {
		// Read dimensions
		final Element dimensionsElement = Utils.getElementByName(boardElement, BOARD_DIMENSIONS_ELEMENT);
		readBoardDimensions(dimensionsElement);
		
		// Read board image
		final Element backgroundElement = Utils.getElementByName(boardElement, BOARD_BACKGROUND_ELEMENT);
		final String path = backgroundElement.getTextContent();
		
		readBoardImage(zip, path);
		
		readBoardCoordinates(zip, boardElement);
	}

	private void readBoardCoordinates(final ZipReader zip, final Element boardElement) throws IOException {
		final NodeList piecesList = boardElement.getChildNodes();
		final int nodeCount = piecesList.getLength();
		
		for (int i = 0; i < nodeCount; i++) {
			final Node node = piecesList.item(i);
			
			if (node.getNodeName().equals(BOARD_COORDINATES_ELEMENT) && node instanceof Element) {
				final Element element = (Element) node;
				
				final String colorAttr = element.getAttribute(BOARD_COORDINATES_ORIENTATION_ATTRIBUTE);
				final int orientation = Color.read(new StringReader(colorAttr));
				
				readCoordinatesImage(zip, element.getTextContent(), orientation);
			}
		}
	}
	
	private void readBoardDimensions (final Element dimensionsElement) {
		final Element minSquareCornerElement = Utils.getElementByName(dimensionsElement, BOARD_DIMENSIONS_MIN_SQUARE_CORNER_ELEMENT);
		minSquareCorner = Vector2D.fromXmlElement(minSquareCornerElement);
		
		final Element maxSquareCornerElement = Utils.getElementByName(dimensionsElement, BOARD_DIMENSIONS_MAX_SQUARE_CORNER_ELEMENT);
		maxSquareCorner = Vector2D.fromXmlElement(maxSquareCornerElement);
		
		final Element centerElement = Utils.getElementByName(dimensionsElement, BOARD_DIMENSIONS_CENTER_ELEMENT);
		boardCenterPoint = Vector2D.fromXmlElement(centerElement);
	}
	
	private void readMarkDimensions (final Element dimensionsElement) {
		final Element centerElement = Utils.getElementByName(dimensionsElement, MARK_DIMENSIONS_CENTER_ELEMENT);
		markCenterPoint = Vector2D.fromXmlElement(centerElement);
	}
	
	private void readMark(final ZipReader zip, final Element markElement) throws IOException {
		// Read dimensions
		final Element dimensionsElement = Utils.getElementByName(markElement, MARK_DIMENSIONS_ELEMENT);
		readMarkDimensions(dimensionsElement);
		
		// Read image
		final Element fileElement = Utils.getElementByName(markElement, MARK_FILE_ELEMENT);
		final String path = fileElement.getTextContent();
		
		readMarkImage(zip, path);
	}
	
	/**
	 * Reads information about piece set.
	 * @param infoElement element with information
	 */
	private void readInfo (final Element infoElement) {
		final Element nameElement = Utils.getElementByName(infoElement, INFO_NAME_ELEMENT);
		name = nameElement.getTextContent();
	}
	
	public void readBoard (final InputStream stream) {
		try {
			final ZipReader zip = new ZipReader();
			zip.readFromStream(stream);
			
			final Document document = readManifestDocument(zip);
			final Element rootElement = document.getDocumentElement();
			
			final String fileTypeAttribute = rootElement.getAttribute(FILE_TYPE_ATTRIBUTE);
			
			if (!FILE_TYPE_VALUE.equals(fileTypeAttribute))
				throw new RuntimeException("Selected file is not raster board");
			
			final Element infoElement = Utils.getElementByName(rootElement, INFO_ELEMENT);
			readInfo (infoElement);
			
			final Element boardElement = Utils.getElementByName(rootElement, BOARD_ELEMENT);
			readBoard (zip, boardElement);
			
			final Element markElement = Utils.getElementByName(rootElement, MARK_ELEMENT);
			readMark (zip, markElement);
		}
		catch (Throwable ex) {
			throw new RuntimeException (ex);
		}
	}
	
	private Document readManifestDocument(final ZipReader zip) throws Exception {
		final ZipRecord manifestRecord = zip.getRecord(MANIFEST_PATH);
		
		if (manifestRecord == null)
			throw new RuntimeException("Board does not contain manifest");

		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final InputStream manifestStream = manifestRecord.getStream();
		
		try {
			return builder.parse(manifestStream);
		}
		finally {
			manifestStream.close();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public IVectorRead getMinSquareCorner() {
		return minSquareCorner;
	}

	public void setMinSquareCorner(final IVectorRead position) {
		this.minSquareCorner = position;
	}

	public IVectorRead getMaxSquareCorner() {
		return maxSquareCorner;
	}

	public void setMaxSquareCorner(final IVectorRead position) {
		this.maxSquareCorner = position;
	}

	public IVectorRead getBoardCenterPoint() {
		return boardCenterPoint;
	}

	public void setBoardCenterPoint(final IVectorRead position) {
		this.boardCenterPoint = position;
	}

	public IVectorRead getMarkCenterPoint() {
		return markCenterPoint;
	}

	public void setMarkCenterPoint(final IVectorRead position) {
		this.markCenterPoint = position;
	}
	
	protected RasterBoard createScaledRasterBoard(final double scale) {
		final RasterBoard result = new RasterBoard();
		
		result.setName (this.getName());
		result.setMinSquareCorner (this.getMinSquareCorner().multiply(scale));
		result.setMaxSquareCorner (this.getMaxSquareCorner().multiply(scale));
		result.setBoardCenterPoint (this.getBoardCenterPoint().multiply(scale));
		result.setMarkCenterPoint (this.getMarkCenterPoint().multiply(scale));
		
		return result;
	}

	protected abstract void readMarkImage (final ZipReader zip, final String path) throws IOException;
	protected abstract void readBoardImage (final ZipReader zip, final String path) throws IOException;
	protected abstract void readCoordinatesImage (final ZipReader zip, final String path, final int color) throws IOException;
}
