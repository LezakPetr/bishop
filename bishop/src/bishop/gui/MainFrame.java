package bishop.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ILocalizedComponent {
	
	private final ApplicationViewImpl applicationView;
	
	public MainFrame(final ApplicationViewImpl applicationView) throws IOException {
		this.setSize(800, 700);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		Utils.centerWindow(this);
		
		this.addWindowListener(windowListener);
		this.applicationView = applicationView;
		
		applicationView.setMainFrame(this);
		
		getContentPane().add(applicationView);

		this.setJMenuBar(applicationView.getApplicationMenuBar());
		
		applicationView.getApplication().getLocalizedComponentRegister().addComponent(this);
	}
	
	public void updateLanguage(final ILocalization localization) {
		this.setTitle(localization.translateString("MainFrame.title"));
	}

	private WindowListener windowListener = new WindowAdapter() {
		public void windowClosing (final WindowEvent e) {
			final IApplication application = applicationView.getApplication();
			final ILocalization localization = application.getLocalization();
			final String title = localization.translateString("ConfirmCloseApplicationDialog.title");
			final String message = localization.translateString("ConfirmCloseApplicationDialog.message");
			
			final int answer = JOptionPane.showConfirmDialog(MainFrame.this, message, title, JOptionPane.OK_CANCEL_OPTION);
			
			if (answer == JOptionPane.OK_OPTION) {
				application.getLocalizedComponentRegister().removeComponent(MainFrame.this);
				applicationView.onClose();
				
				dispose();
				System.exit(0);
			}
		}
	};
	

}
