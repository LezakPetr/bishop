package bishop.controller;

import org.w3c.dom.Element;

public class SideSettings {

	private SideType sideType;
	private long timeForMove;   // [ms]
	
	private static final String ELEMENT_SIDE_TYPE = "side_type";
	private static final String ELEMENT_TIME_FOR_MOVE = "time_for_move";
	
	
	public SideSettings() {
		sideType = SideType.HUMAN;
		timeForMove = 10000;
	}
	
	public SideType getSideType() {
		return sideType;
	}
	
	public void setSideType(final SideType sideType) {
		this.sideType = sideType;
	}
	
	public long getTimeForMove() {
		return timeForMove;
	}
	
	public void setTimeForMove(final long timeForMove) {
		this.timeForMove = timeForMove;
	}
	
	public void assign (final SideSettings orig) {
		this.sideType = orig.sideType;
		this.timeForMove = orig.timeForMove;		
	}
	
	public SideSettings copy() {
		final SideSettings copy = new SideSettings();
		copy.assign (this);
		
		return copy;
	}
	
	public void readFromXmlElement (final Element parentElement) {
		final Element elementSideType = Utils.getElementByName(parentElement, ELEMENT_SIDE_TYPE);
		sideType = SideType.valueOf(elementSideType.getTextContent());
		
		final Element elementTimeForMove = Utils.getElementByName(parentElement, ELEMENT_TIME_FOR_MOVE);
		timeForMove = Long.parseLong(elementTimeForMove.getTextContent());
	}

	public void writeToXmlElement (final Element parentElement) {
		final Element elementSideType = Utils.addChildElement(parentElement, ELEMENT_SIDE_TYPE);
		elementSideType.setTextContent(sideType.toString());
		
		final Element elementTimeForMove = Utils.addChildElement(parentElement, ELEMENT_TIME_FOR_MOVE);
		elementTimeForMove.setTextContent(Long.toString(timeForMove));
	}
	
}
