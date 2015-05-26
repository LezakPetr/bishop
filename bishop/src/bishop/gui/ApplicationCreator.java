package bishop.gui;

import java.io.File;
import java.net.URL;

import javax.swing.SwingUtilities;

import bishop.controller.ApplicationImpl;
import bishop.controller.SearchResources;

public class ApplicationCreator {
	
	private AboutDialog aboutDialog;
	private SearchResources searchResources;
	private ApplicationImpl application;
	private ApplicationViewImpl applicationView;
	private MainFrame mainFrame;
	
	private void initializationWithDialog(final ApplicationImpl application) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				aboutDialog = AboutDialog.createDialog(null, application.getLocalization(), false);
				aboutDialog.setVisible(true);
			}
		});	

		searchResources = new SearchResources(application);
		applicationView.loadGraphics();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				aboutDialog.dispose();
			}
		});
		
		aboutDialog = null;
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
			
			mainFrame = new MainFrame(applicationView);
			
			application.initialize();
			applicationView.initialize();
			
			mainFrame.setVisible(true);
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot create application view", ex);
		}
	}

}
