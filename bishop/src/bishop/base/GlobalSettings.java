package bishop.base;

public class GlobalSettings {
	
	private static boolean debug = false;

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(final boolean debug) {
		GlobalSettings.debug = debug;
	}
	
}
