package bishop.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bishop.controller.GuiSettings;
import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public class GuiSettingPanel extends JPanel implements ILocalizedComponent {

	private final IApplication application;
	private JLabel labelLanguage;
	private JComboBox<String> comboLanguage;
	
	public GuiSettingPanel(final IApplication application) {
		this.application = application;
		
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
	}
	
	private void initializeComponents() {
		this.setLayout(new GridBagLayout());
		
		// Thread count
		labelLanguage = new JLabel();
		
		this.add (labelLanguage,
			new GridBagConstraints(
				0, 0, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 5, 5),
				0, 0
			)
		);
		
		comboLanguage = new JComboBox<String>();
		
		for (String language: application.getLocalization().getAvailableLanguages())
			comboLanguage.addItem(language);
		
		this.add (comboLanguage,
			new GridBagConstraints(
				1, 0, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 0),
				0, 0
			)
		);
	}

	public void loadSettings(final GuiSettings guiSettings) {
		comboLanguage.setSelectedItem(guiSettings.getLanguage());
	}

	public void saveSettings(final GuiSettings guiSettings) {
		guiSettings.setLanguage((String) comboLanguage.getSelectedItem());
	}

	public void updateLanguage(final ILocalization localization) {
		labelLanguage.setText(localization.translateString("GuiSettingPanel.labelLanguage.text"));
	}


}
