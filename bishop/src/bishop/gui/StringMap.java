package bishop.gui;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import utils.IoUtils;


public class StringMap {

	private final Map<String, String> map;
	
	private static final char KEY_TERMINATION_MARK = '=';
	private static final char VALUE_TERMINATION_MARK = '\n';
	
	public StringMap() {
		map = new TreeMap<String, String>();
	}
	
	public void addItem (final String key, final String value) {
		map.put(key, value);
	}
	
	public String getItem (final String key) {
		return map.get(key);
	} 
		
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
	
	public void read (final File file) throws IOException {
		final FileReader fileReader = new FileReader(file);
		
		try {
			final PushbackReader pushbackReader = new PushbackReader(fileReader);
			read (pushbackReader);
		}
		finally {
			fileReader.close();
		}
	}
	
	public void write (final Writer writer) throws IOException {
		for (Entry<String, String> item: map.entrySet()) {
			IoUtils.writeBackslashedString (writer, item.getKey());			
			writer.write(KEY_TERMINATION_MARK);
			IoUtils.writeBackslashedString (writer, item.getValue());
			writer.write(VALUE_TERMINATION_MARK);
		}
	}
}
