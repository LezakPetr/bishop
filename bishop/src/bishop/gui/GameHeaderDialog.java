package bishop.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import bishop.base.GameHeader;
import bishop.controller.ILocalization;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class GameHeaderDialog extends SettingDialogBase<GameHeader> {
	
	private GameHeaderPanel headerPanel;
	
	protected GameHeaderDialog(final IApplicationView applicationView, final GameHeader settings) {
		super(applicationView, settings);
		
		initialize();
	}
	
	public void destroy() {
		super.destroy();
		
		headerPanel.destroy();
	}
	
	protected void initializeComponents() {
		super.initializeComponents();
		
		this.setModal(true);
		this.setSize(300, 300);
		
		Utils.centerWindow(this);
		
		final JPanel contentPanel = getContentPanel();
		contentPanel.setLayout(new BorderLayout());
		
		headerPanel = new GameHeaderPanel(application);
		contentPanel.add(headerPanel);
	}
	
	public void loadSettings() {
		headerPanel.loadHeader(getSettings());
	}

	public void saveSettings() {
		headerPanel.saveHeader(getSettings());
	}
	
	public static boolean showDialog(final IApplicationView applicationView, final GameHeader header) {
		final GameHeaderDialog dialog = new GameHeaderDialog(applicationView, header);
		
		dialog.setVisible(true);
		dialog.destroy();
			
		if (dialog.isConfirmed()) {
			header.assign (dialog.getSettings());
			
			return true;
		}
		else
			return false;
	}

	public void updateLanguage(final ILocalization localization) {
		super.updateLanguage (localization);
		
		this.setTitle(localization.translateString("GameHeaderDialog.title"));
	}

}
