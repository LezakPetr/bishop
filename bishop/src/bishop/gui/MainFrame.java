package bishop.gui;

import java.awt.Container;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ILocalizedComponent {
		
	private static final String ICON_SUFFIX = ".png";
	private static final String ICON_DIRECTORY = "graphics/icon";

	private final ApplicationViewImpl applicationView;
	private ILocalization localization;
	
	public MainFrame(final ApplicationViewImpl applicationView) throws IOException {
		this.applicationView = applicationView;
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		setApplicationIcons();
	}
	
	public void setAboutPanel() {
		final Container contentPane = this.getContentPane();
		contentPane.removeAll();
		contentPane.add(new AboutPanel(localization));
		
		this.setSize(300, 80);
		Utils.centerWindow(this);
	}

	public void initialize() {
		this.addWindowListener(windowListener);
		
		applicationView.setMainFrame(this);

		final Container contentPane = this.getContentPane();
		contentPane.removeAll();
		contentPane.add(applicationView);

		this.setJMenuBar(applicationView.getApplicationMenuBar());
		applicationView.getApplication().getLocalizedComponentRegister().addComponent(this);
		
		this.setSize(800, 700);
		Utils.centerWindow(this);
	}

	public void updateLanguage(final ILocalization localization) {
		this.localization = localization;
		
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
	
	private void setApplicationIcons() throws IOException {
		final File currentDirectory = new File(".").getAbsoluteFile();
		final File directory = new File (currentDirectory, ICON_DIRECTORY);
		final List<Image> icons = new ArrayList<>();
		
		for (File file: directory.listFiles()) {
			if (file.isFile() && file.getName().endsWith(ICON_SUFFIX)) {
				final Image image = ImageIO.read(file);
				icons.add(image);
			}
		}
		
		this.setIconImages(icons);
	}


}
