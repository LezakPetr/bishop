package bishop.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.SideSettings;
import bishop.controller.SideType;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class SideSettingPanel extends JPanel implements ILocalizedComponent {

	private final IApplication application;
	private JLabel labelSideType;
	private JComboBox<String> comboSideType;
	
	private JLabel labelTimeForMove;
	private JTextField fieldTimeForMove;
	
	public SideSettingPanel(final IApplication application) {
		this.application = application;
		
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
	}
	
	
	private void initializeComponents() {
		this.setLayout(new GridBagLayout());
		
		// Side type
		labelSideType = new JLabel();
		
		this.add (labelSideType,
			new GridBagConstraints(
				0, 0, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 5, 5),
				0, 0
			)
		);
		
		comboSideType = new JComboBox<String>();
		
		this.add (comboSideType,
			new GridBagConstraints(
				1, 0, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 0),
				0, 0
			)
		);

		
		// Time for move
		labelTimeForMove = new JLabel();
		this.add (
			labelTimeForMove,
			new GridBagConstraints(
				0, 1, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 5),
				0, 0
			)
		);
		
		fieldTimeForMove = new JTextField();
			
		this.add(
			fieldTimeForMove,
			new GridBagConstraints(
				1, 1, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
	}


	public void loadSettings(final SideSettings sideSettings) {
		comboSideType.setSelectedIndex(sideSettings.getSideType().ordinal());
		
		final String timeString = Utils.timeToString (sideSettings.getTimeForMove());
		fieldTimeForMove.setText(timeString);
	}


	public void saveSettings(final SideSettings sideSettings) {
		sideSettings.setSideType(SideType.values()[comboSideType.getSelectedIndex()]);
		
		final long timeForMove = Utils.stringToTime (fieldTimeForMove.getText());
		sideSettings.setTimeForMove(timeForMove);
	}

	public void updateLanguage(final ILocalization localization) {
		labelSideType.setText(localization.translateString("SideSettingPanel.labelSideType.text"));
		labelTimeForMove.setText(localization.translateString("SideSettingPanel.labelTimeForMove.text"));
		
		localizeComboSideType(localization);
	}
	
	private void localizeComboSideType(final ILocalization localization) {
		final int selectedIndex = comboSideType.getSelectedIndex();
		comboSideType.removeAllItems();
		
		for (SideType type: SideType.values())
			comboSideType.addItem(type.getName(localization));
	
		comboSideType.setSelectedIndex(selectedIndex);
	}
	
	public void setEnabled (final boolean enabled) {
		comboSideType.setEnabled(enabled);
		fieldTimeForMove.setEnabled(enabled);
	}
	
}
