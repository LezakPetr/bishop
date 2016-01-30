package bishop.gui;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import bishop.controller.ApplicationImpl;
import bishop.controller.SearchResources;

public class ApplicationCreator {
	
	private SearchResources searchResources;
	private ApplicationImpl application;
	private ApplicationViewImpl applicationView;
	private MainFrame mainFrame;
	
	private void initializationWithDialog(final ApplicationImpl application) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				createMainFrame();
			}
		});	

		searchResources = new SearchResources(application);
		applicationView.loadGraphics();
	}
	
	private void createMainFrame() {
		try {
			mainFrame = new MainFrame(applicationView);
			mainFrame.updateLanguage(application.getLocalization());
			mainFrame.setAboutPanel();
			mainFrame.setVisible(true);
		}
		catch (Exception ex) {
			throw new RuntimeException (ex);
		}
	}
	
	public void createApplication() throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				createApplicationObject();
			}
		});	
		
		initializationWithDialog(application);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				finishInitialization();
			}
		});	
	}

	private void createApplicationObject() {
		try {
			final File currentDirectory = new File(".");
			final URL rootUrl = currentDirectory.toURI().toURL();
	
			application = new ApplicationImpl(rootUrl);
			applicationView = new ApplicationViewImpl(application);
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot create application", ex);
		}
	}
	
	private void finishInitialization() {
		try {
			application.setSearchResources(searchResources);
						
			application.initialize();
			applicationView.initialize();
			
			mainFrame.initialize();
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot create application view", ex);
		}
	}
}
