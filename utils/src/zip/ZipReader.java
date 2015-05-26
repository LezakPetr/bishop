package zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import utils.IoUtils;

public class ZipReader {
	
	private final Map<String, ZipRecord> recordMap;
	
	public ZipReader() {
		recordMap = new HashMap<String, ZipRecord>();
	}
	
	public void clear() {
		recordMap.clear();
	}
	
	public void readFromStream(final InputStream stream) throws IOException {
		final ZipInputStream zipStream = new ZipInputStream(stream);
		
		try {
			while (readEntry(zipStream))
				;
			
		}
		finally {
			zipStream.close();
		}
	}
	
	private boolean readEntry(final ZipInputStream zipStream) throws IOException {
		final ZipEntry entry = zipStream.getNextEntry();
		
		if (entry == null)
			return false;
		
		final ByteArrayOutputStream entryStream = new ByteArrayOutputStream();
		IoUtils.copyStream(zipStream, entryStream);
		
		zipStream.closeEntry();
		
		recordMap.put(entry.getName(), new ZipRecord(entry, entryStream));
		
		return true;
	}
	
	public ZipRecord getRecord(final String name) {
		return recordMap.get(name);
	}
}
