package bishop.gui;

import utils.Logger;
import bishop.base.GlobalSettings;


public class Main {
		
	public static void main (final String[] args) {
		for (String argument: args) {
			if (argument.equals("-debug")) {
				GlobalSettings.setDebug(true);
				Logger.setStream(System.out);
			}
		}
		
		try {
			final ApplicationCreator creator = new ApplicationCreator();
			creator.createApplication();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
