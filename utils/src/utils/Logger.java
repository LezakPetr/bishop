package utils;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Logger {
	
	private static final LinkedList<String> messageList = new LinkedList<String>();
	private static final int MESSAGE_LIST_MAX_SIZE = 100;
	
	private static PrintStream stream;
	private static final long beginTime = System.currentTimeMillis();
	private static final Object monitor = new Object();
	
	public static void logMessage (final String message) {
		synchronized (monitor) {
			final long diffTime = System.currentTimeMillis() - beginTime;
			final String wholeMessage = "" + diffTime + ": " + message;
			
			addMessageToList (wholeMessage);
			
			if (stream != null) {
				stream.println (wholeMessage);				
				stream.flush();
			}
		}
	}
	
	private static void addMessageToList (final String message) {
		messageList.add(message);
		
		while (messageList.size() > MESSAGE_LIST_MAX_SIZE) {
			messageList.removeFirst();
		}
	}
	
	public static void setStream (final PrintStream stream) {
		Logger.stream = stream;
	}
	
	public static void logException(final Throwable th) {
		th.printStackTrace();
		
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		th.printStackTrace(printWriter);
		printWriter.flush();
		
		logMessage(stringWriter.toString());
	}
	
	public static void showLog(final Frame owner) {
		final StringBuilder builder = new StringBuilder();
		
		for (String message: messageList) {
			builder.append(message);
			builder.append('\n');
		}
		
		final JTextArea area = new JTextArea();
		area.setText(builder.toString());
		
		final JScrollPane scrollPane = new JScrollPane(area);
		scrollPane.setPreferredSize(new Dimension(500, 200));
		
		JOptionPane.showMessageDialog(owner, scrollPane, "Log", JOptionPane.INFORMATION_MESSAGE);
	}
}
