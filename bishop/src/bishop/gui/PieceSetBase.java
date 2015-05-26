package bishop.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import zip.ZipReader;
import zip.ZipRecord;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.controller.Utils;
import bisGui.math.IVector;
import bisGui.math.Vector2D;
import bisGui.math.Vectors;

public abstract class PieceSetBase implements IPieceSet {
	private String name;
	private IVector centerPoint;
	private IVector pieceSize;


	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public IVector getCenterPoint() {
		return centerPoint;
	}
	
	public void setCenterPoint(final IVector point) {
		this.centerPoint = point;
	}

	public IVector getPieceSize() {
		return pieceSize;
	}

	public void setPieceSize(final IVector pieceSize) {
		this.pieceSize = pieceSize;
	}

	private static final String MANIFEST_PATH = "manifest.xml";
	private static final String PIECE_ELEMENT = "piece";
	private static final String PIECE_TYPE_ATTRIBUTE = "piece_type";
	private static final String COLOR_ATTRIBUTE = "color";
	private static final String INFO_ELEMENT = "info";
	private static final String INFO_NAME_ELEMENT = "name";
	private static final String DIMENSIONS_ELEMENT = "dimensions";
	private static final String DIMENSIONS_CENTER_ELEMENT = "center";
	private static final String DIMENSIONS_SIZE_ELEMENT = "size";
	private static final String PIECE_LIST_ELEMENT = "pieces";
	private static final String FILE_TYPE_VALUE = "raster_piece_set";
	private static final String FILE_TYPE_ATTRIBUTE = "file_type";
	
	

	/**
	 * Reads dimensions of pieces.
	 * @param dimensionElement element with dimensions
	 */
	private void readPieceDimensions (final Element dimensionsElement) {
		final Element centerElement = Utils.getElementByName(dimensionsElement, DIMENSIONS_CENTER_ELEMENT);
		centerPoint = Vector2D.fromXmlElement(centerElement);
		
		final Element sizeElement = Utils.getElementByName(dimensionsElement, DIMENSIONS_SIZE_ELEMENT);
		pieceSize = Vector2D.fromXmlElement(sizeElement);
	}
	
	/**
	 * Reads pieces from piece set.
	 * @param zip zip file with piece set
	 * @param piecesElement element with information about pieces
	 * @throws IOException if IO fails
	 */
	private void readPieceList (final ZipReader zip, final Element piecesElement) throws IOException {
		final Element dimensionsElement = Utils.getElementByName(piecesElement, DIMENSIONS_ELEMENT);
		readPieceDimensions(dimensionsElement);
		
		final NodeList piecesList = piecesElement.getChildNodes();
		final int nodeCount = piecesList.getLength();
		
		for (int i = 0; i < nodeCount; i++) {
			final Node node = piecesList.item(i);
			
			if (node.getNodeName().equals(PIECE_ELEMENT) && node instanceof Element) {
				final Element element = (Element) node;
				
				final String colorAttr = element.getAttribute(COLOR_ATTRIBUTE);
				final int color = Color.read(new StringReader(colorAttr));
				
				final String pieceTypeAttr = element.getAttribute(PIECE_TYPE_ATTRIBUTE);
				final int pieceType = PieceType.read(new StringReader(pieceTypeAttr));
				
				readPieceImage(zip, element.getTextContent(), color, pieceType);
			}
		}
	}
	
	/**
	 * Reads information about piece set.
	 * @param infoElement element with information
	 */
	private void readInfo (final Element infoElement) {
		final Element nameElement = Utils.getElementByName(infoElement, INFO_NAME_ELEMENT);
		name = nameElement.getTextContent();
	}
	
	/**
	 * Reads piece set from given file.
	 * @param file path to zip with piece set
	 */
	public void readPieceSet (final InputStream stream) {
		try {
			final ZipReader zip = new ZipReader();
			zip.readFromStream(stream);
			
			final Document document = readManifestDocument(zip);
			final Element rootElement = document.getDocumentElement();
			
			final String fileTypeAttribute = rootElement.getAttribute(FILE_TYPE_ATTRIBUTE);
			
			if (!FILE_TYPE_VALUE.equals(fileTypeAttribute))
				throw new RuntimeException("Selected file is not raster piece set");
			
			final Element infoElement = Utils.getElementByName(rootElement, INFO_ELEMENT);
			readInfo (infoElement);
			
			final Element piecesElement = Utils.getElementByName(rootElement, PIECE_LIST_ELEMENT);
			readPieceList (zip, piecesElement);
		}
		catch (Throwable ex) {
			throw new RuntimeException (ex);
		}
	}

	private Document readManifestDocument(final ZipReader zip) throws Exception {
		final ZipRecord manifestRecord = zip.getRecord(MANIFEST_PATH);
		
		if (manifestRecord == null)
			throw new RuntimeException("Piece set does not contain manifest");

		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final InputStream manifestStream = manifestRecord.getStream();
		
		try {
			return builder.parse(manifestStream);
		}
		finally {
			manifestStream.close();
		}
	}	
	protected RasterPieceSet createScaledRasterPieceSet(final double scale) {
		final RasterPieceSet result = new RasterPieceSet();

		result.setName(this.getName());
		result.setCenterPoint(Vectors.multiply(scale, this.getCenterPoint()));
		result.setPieceSize(Vectors.multiply(scale, this.getPieceSize()));
		return result;
	}
	
	protected abstract void readPieceImage (final ZipReader zip, final String path, final int color, final int pieceType) throws IOException;
}
