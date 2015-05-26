package bishop.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import bishop.controller.ApplicationSettings;
import bishop.controller.ILocalization;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class ApplicationSettingDialog extends SettingDialogBase<ApplicationSettings> {
	
	private JTabbedPane tabbedPane;
	private EngineSettingPanel engineSettingPanel;
	private GuiSettingPanel guiSettingPanel;
	
	private static final int TAB_ENGINE_SETTINGS = 0;
	private static final int TAB_GUI_SETTINGS = 1;
	
	
	private ApplicationSettingDialog(final IApplicationView applicationView, final ApplicationSettings settings) {
		super (applicationView, settings);
		
		initialize();
	}
	
	public void destroy() {
		super.destroy();
		
		engineSettingPanel.destroy();
		guiSettingPanel.destroy();
	}
	
	protected void initializeComponents() {
		super.initializeComponents();
		
		this.setModal(true);
		this.setSize(500, 150);
		
		Utils.centerWindow(this);
		
		final JPanel contentPanel = getContentPanel();
		contentPanel.setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		contentPanel.add(tabbedPane);
		
		engineSettingPanel = new EngineSettingPanel(application);
		tabbedPane.add(engineSettingPanel);
		
		guiSettingPanel = new GuiSettingPanel(application);
		tabbedPane.add(guiSettingPanel);
	}
	
	public void loadSettings() {
		engineSettingPanel.loadSettings(getSettings().getEngineSettings());
		guiSettingPanel.loadSettings(getSettings().getGuiSettings());
	}

	public void saveSettings() {
		engineSettingPanel.saveSettings(getSettings().getEngineSettings());
		guiSettingPanel.saveSettings(getSettings().getGuiSettings());
	}
	
	public static boolean showDialog(final IApplicationView applicationView, final ApplicationSettings applicationSettings) {
		final ApplicationSettingDialog dialog = new ApplicationSettingDialog(applicationView, applicationSettings);
		
		dialog.setVisible(true);
		dialog.destroy();
			
		if (dialog.isConfirmed()) {
			applicationSettings.assign (dialog.getSettings());
			
			return true;
		}
		else
			return false;
	}

	public void updateLanguage(final ILocalization localization) {
		super.updateLanguage (localization);
		
		this.setTitle(localization.translateString("ApplicationSettingDialog.title"));
		
		tabbedPane.setTitleAt(TAB_ENGINE_SETTINGS, localization.translateString("ApplicationSettingDialog.tabEngineSettings.title"));
		tabbedPane.setTitleAt(TAB_GUI_SETTINGS, localization.translateString("ApplicationSettingDialog.tabGuiSettings.title"));
	}
}
