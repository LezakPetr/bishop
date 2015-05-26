package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class StringMap {

	private final Map<String, String> map;
	
	private static final char KEY_TERMINATION_MARK = '=';
	private static final char VALUE_TERMINATION_MARK = '\n';
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	public StringMap() {
		map = new TreeMap<String, String>();
	}
	
	public void addItem (final String key, final String value) {
		map.put(key, value);
	}
	
	public String getItem (final String key) {
		return map.get(key);
	}
	
	public void removeItem (final String key) {
		if (map.remove(key) == null) {
			throw new RuntimeException("Key '" + key + "' not found");
		}
	}
	
	/**
	 * Returns sorted list of keys in the map.
	 * @return independent (copied) list of keys 
	 */
	public List<String> getSortedKeys() {
		return new ArrayList<String> (map.keySet());
	}
	
	/**
	 * Reads content of the map from given reader.
	 * @param reader reader
	 * @throws IOException
	 */
	public void read (final PushbackReader reader) throws IOException {
		map.clear();
		
		IoUtils.skipWhiteSpace(reader);
		
		while (!IoUtils.isEndOfStream(reader)) {
			final String key = IoUtils.readBackslashedString (reader, "" + KEY_TERMINATION_MARK);
			final char keyTermination = IoUtils.readChar(reader);
			
			if (keyTermination != KEY_TERMINATION_MARK)
				throw new RuntimeException("Expected character " + KEY_TERMINATION_MARK);
			
			final String value = IoUtils.readBackslashedString (reader, "" + VALUE_TERMINATION_MARK);
			
			if (map.put(key, value) != null)
				throw new RuntimeException("Dupplicate key " + key);
			
			IoUtils.skipWhiteSpace(reader);
		}
	}
	
	public void readFromStream(final InputStream stream) throws IOException {
		final InputStreamReader streamReader = new InputStreamReader(stream, CHARSET);
		final PushbackReader pushbackReader = new PushbackReader(streamReader);
		
		read (pushbackReader);
	}
	
	/**
	 * Reads content of the map from given file.
	 * @param file file
	 * @throws IOException
	 */
	public void readFromFile (final File file) throws IOException {
		final FileInputStream stream = new FileInputStream(file);
		
		try {
			readFromStream(stream);
		}
		finally {
			stream.close();
		}
	}

	public void readFromUrl (final URL url) throws IOException {
		final InputStream stream = url.openStream();
		
		try {
			readFromStream(stream);
		}
		finally {
			stream.close();
		}		
	}

	/**
	 * Writes content of the map to given writer.
	 * @param writer writer
	 * @throws IOException
	 */
	public void write (final Writer writer) throws IOException {
		for (Entry<String, String> item: map.entrySet()) {
			IoUtils.writeBackslashedString (writer, item.getKey());			
			writer.write(KEY_TERMINATION_MARK);
			IoUtils.writeBackslashedString (writer, item.getValue());
			writer.write(VALUE_TERMINATION_MARK);
		}
	}
	
	public void writeToStream (final OutputStream stream) throws IOException {
		final OutputStreamWriter writer = new OutputStreamWriter(stream, CHARSET);
		
		write(writer);
		writer.flush();
	}
	
	/**
	 * Writes content of the map to given writer.
	 * @param writer writer
	 * @throws IOException
	 */
	public void writeToFile (final File file) throws IOException {
		final FileOutputStream stream = new FileOutputStream(file);
		
		try {
			writeToStream (stream);
		}
		finally {
			stream.close();
		}		
	}

}
