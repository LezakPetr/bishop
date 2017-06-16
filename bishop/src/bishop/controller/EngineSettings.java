package bishop.controller;

import org.w3c.dom.Element;

import bishop.base.Copyable;

public class EngineSettings implements Copyable<EngineSettings> {

	private int threadCount;
	private int hashTableExponent;
	private String tablebaseDirectory;
	
	
	private static final String ELEMENT_THREAD_COUNT = "thread_count";
	private static final String ELEMENT_HASH_TABLE_EXPONENT = "hashTableExponent";
	private static final String ELEMENT_TABLEBASE_DIRECTORY = "tablebaseDirectory";
	
	public static final int MIN_HASH_TABLE_EXPONENT = 10;
	public static final int MAX_HASH_TABLE_EXPONENT = 30;
	public static final int HASH_TABLE_EXPONENT_OFFSET = 3;   // 2^3 = 8 Bytes per hash table item
	
	
	public int getThreadCount() {
		return threadCount;
	}
	
	public void setThreadCount(final int threadCount) {
		this.threadCount = threadCount;
	}

	public int getHashTableExponent() {
		return hashTableExponent;
	}

	public void setHashTableExponent(int hashTableExponent) {
		this.hashTableExponent = hashTableExponent;
	}
	
	public String getTablebaseDirectory() {
		return tablebaseDirectory;
	}

	public void setTablebaseDirectory(final String tablebaseDirectory) {
		this.tablebaseDirectory = tablebaseDirectory;
	}

	public void readFromXmlElement (final Element parentElement) {
		final Element elementThreadCount = Utils.getElementByName(parentElement, ELEMENT_THREAD_COUNT);
		threadCount = Integer.parseInt(elementThreadCount.getTextContent());
		
		final Element elementHashTableExponent = Utils.getElementByName(parentElement, ELEMENT_HASH_TABLE_EXPONENT);
		hashTableExponent = Integer.parseInt(elementHashTableExponent.getTextContent());
		
		final Element elementTablebaseDirectory = Utils.getElementByName(parentElement, ELEMENT_TABLEBASE_DIRECTORY);
		tablebaseDirectory = elementTablebaseDirectory.getTextContent();
	}

	public void writeToXmlElement (final Element parentElement) {
		final Element elementThreadCount = Utils.addChildElement(parentElement, ELEMENT_THREAD_COUNT);
		elementThreadCount.setTextContent(Integer.toString(threadCount));
		
		final Element elementHashTableExponent = Utils.addChildElement(parentElement, ELEMENT_HASH_TABLE_EXPONENT);
		elementHashTableExponent.setTextContent(Integer.toString(hashTableExponent));
		
		final Element elementTablebaseDirectory = Utils.addChildElement(parentElement, ELEMENT_TABLEBASE_DIRECTORY);
		elementTablebaseDirectory.setTextContent(tablebaseDirectory);
	}

	public void setDefaults() {
		final Runtime runtime = Runtime.getRuntime();
		
		threadCount = runtime.availableProcessors();
		hashTableExponent = 20;
		tablebaseDirectory = "";
	}
	
	public void assign (final EngineSettings orig) {
		this.threadCount = orig.threadCount;
		this.hashTableExponent = orig.hashTableExponent;
		this.tablebaseDirectory = orig.tablebaseDirectory;
	}

	public EngineSettings copy() {
		final EngineSettings copyOfSettings = new EngineSettings();
		copyOfSettings.assign(this);
		
		return copyOfSettings;
	}

}
