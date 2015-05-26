package bishop.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import bishop.controller.EngineSettings;
import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public class EngineSettingPanel extends JPanel implements ILocalizedComponent {

	private final IApplication application;
	
	private JLabel labelThreadCount;
	private JTextField fieldThreadCount;
	
	private JLabel labelHashTableExponent;
	private JSpinner spinnerHashTableExponent;
	private BinarySizeSpinnerModel hashTableSpinnerModel;

	private JLabel labelTablebaseDirectory;
	private JTextField fieldTablebaseDirectory;

	
	public EngineSettingPanel(final IApplication application) {
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
		labelThreadCount = new JLabel();
		
		this.add (labelThreadCount,
			new GridBagConstraints(
				0, 0, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 5, 5),
				0, 0
			)
		);
		
		fieldThreadCount = new JTextField();
		
		this.add (fieldThreadCount,
			new GridBagConstraints(
				1, 0, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 0),
				0, 0
			)
		);

		// Hash table exponent
		labelHashTableExponent = new JLabel();
		
		this.add (
			labelHashTableExponent,
			new GridBagConstraints(
				0, 1, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 5),
				0, 0
			)
		);
		
		spinnerHashTableExponent = new JSpinner();
			
		this.add(
			spinnerHashTableExponent,
			new GridBagConstraints(
				1, 1, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
		
		hashTableSpinnerModel = new BinarySizeSpinnerModel(EngineSettings.MIN_HASH_TABLE_EXPONENT, EngineSettings.HASH_TABLE_EXPONENT_OFFSET, EngineSettings.MIN_HASH_TABLE_EXPONENT, EngineSettings.MAX_HASH_TABLE_EXPONENT);
		spinnerHashTableExponent.setModel(hashTableSpinnerModel);
		
		// Tablebase directory
		labelTablebaseDirectory = new JLabel();
		
		this.add (labelTablebaseDirectory,
			new GridBagConstraints(
				0, 2, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 5, 5),
				0, 0
			)
		);
		
		fieldTablebaseDirectory = new JTextField();
		
		this.add (fieldTablebaseDirectory,
			new GridBagConstraints(
				1, 2, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 0),
				0, 0
			)
		);
	}

	public void loadSettings(final EngineSettings engineSettings) {
		fieldThreadCount.setText(Integer.toString(engineSettings.getThreadCount()));
		hashTableSpinnerModel.setExponent(engineSettings.getHashTableExponent());
		fieldTablebaseDirectory.setText(engineSettings.getTablebaseDirectory());
	}

	public void saveSettings(final EngineSettings engineSettings) {
		engineSettings.setThreadCount(Integer.parseInt(fieldThreadCount.getText()));
		engineSettings.setHashTableExponent(hashTableSpinnerModel.getExponent());
		engineSettings.setTablebaseDirectory(fieldTablebaseDirectory.getText());
	}

	public void updateLanguage(final ILocalization localization) {
		labelThreadCount.setText(localization.translateString("EngineSettingPanel.labelThreadCount.text"));
		labelHashTableExponent.setText(localization.translateString("EngineSettingPanel.labelHashTableExponent.text"));
		labelTablebaseDirectory.setText(localization.translateString("EngineSettingPanel.labelTablebaseDirectory.text"));
	}

}
