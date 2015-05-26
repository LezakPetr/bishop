package bishop.gui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import bishop.base.Copyable;
import bishop.controller.ILocalization;

@SuppressWarnings("serial")
public abstract class SettingDialogBase<SETTINGS extends Copyable<SETTINGS>> extends OkCancelDialog {
	
	private SETTINGS settings;
	private boolean confirmed;

	
	protected SettingDialogBase(final IApplicationView application, final SETTINGS settings) {
		super (application);
		
		this.settings = settings.copy();
		this.confirmed = false;
	}
	
	protected void initialize() {
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
		
		loadSettings();
	}
	
	@Override
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
		
		super.destroy();
	}

	protected void initializeComponents() {
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	@Override
	protected void onOk() {
		try {
			saveSettings();
			confirmed = true;
			
			SettingDialogBase.this.dispose();
		}
		catch (Throwable th) {
			JOptionPane.showMessageDialog(SettingDialogBase.this, th.getMessage());
		}
	}
	
	@Override
	protected void onCancel() {
		confirmed = false;
		SettingDialogBase.this.dispose();
	}
	
	protected abstract void loadSettings();
	protected abstract void saveSettings();
	
	public SETTINGS getSettings() {
		return settings;
	}
	
	public JPanel getContentPanel() {
		return contentPanel;
	}
	
	public void updateLanguage(final ILocalization localization) {
	}
}
