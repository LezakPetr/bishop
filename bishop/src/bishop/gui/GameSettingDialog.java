package bishop.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import bishop.base.Color;
import bishop.controller.GameSettings;
import bishop.controller.GameType;
import bishop.controller.ILocalization;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class GameSettingDialog extends SettingDialogBase<GameSettings> {
	
	private SideSettingPanel[] sideSettingPanelArray;
	private JPanel panelSideSettings;
	private JPanel panelGameType;
	private JLabel labelGameType;
	private JComboBox<String> comboGameType;
	
	
	private static final Dimension DIALOG_SIZE = new Dimension(500, 170);
	private static final Dimension COMBO_GAME_TYPE_SIZE = new Dimension(140, 25);
	
	
	private GameSettingDialog(final IApplicationView applicationView, final GameSettings gameSettings) {
		super (applicationView, gameSettings);
		
		initialize();
	}
	
	public void destroy() {
		super.destroy();
		
		for (SideSettingPanel panel: sideSettingPanelArray)
			panel.destroy();
	}

	protected void initializeComponents() {
		super.initializeComponents();
		
		this.setModal(true);
		this.setSize(DIALOG_SIZE);
		
		Utils.centerWindow(this);
		
		final JPanel contentPanel = getContentPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		contentPanel.add(Box.createVerticalStrut(5));
		
		panelGameType = new JPanel();
		panelGameType.setLayout(new BoxLayout(panelGameType, BoxLayout.X_AXIS));
		
		panelGameType.add(Box.createHorizontalStrut(5));
		
		labelGameType = new JLabel();
		panelGameType.add(labelGameType);
		
		panelGameType.add(Box.createHorizontalStrut(5));
		
		comboGameType = new JComboBox<String>();
		comboGameType.setMinimumSize(COMBO_GAME_TYPE_SIZE);
		comboGameType.setPreferredSize(COMBO_GAME_TYPE_SIZE);
		comboGameType.setMaximumSize(COMBO_GAME_TYPE_SIZE);
		comboGameType.addActionListener(comboGameType_ActionListener);
		panelGameType.add(comboGameType);

		panelGameType.add(Box.createHorizontalGlue());
		
		contentPanel.add(panelGameType);
		contentPanel.add(Box.createVerticalStrut(5));
		
		panelSideSettings = new JPanel();
		panelSideSettings.setLayout(new BoxLayout(panelSideSettings, BoxLayout.X_AXIS));
		
		contentPanel.add(panelSideSettings);
		
		// Settings
		sideSettingPanelArray = new SideSettingPanel[Color.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final SideSettingPanel currentPanel = new SideSettingPanel(application);
			sideSettingPanelArray[color] = currentPanel;
			
			currentPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
			panelSideSettings.add(currentPanel);
		}
	}
	
	protected void loadSettings() {
		final GameSettings settings = getSettings();
				
		comboGameType.setSelectedIndex(settings.getGameType().ordinal());
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			sideSettingPanelArray[color].loadSettings(settings.getSideSettings(color));
	}

	protected void saveSettings() {
		final GameSettings settings = getSettings();
		
		settings.setGameType(getSelectedGameType());
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			sideSettingPanelArray[color].saveSettings(settings.getSideSettings(color));
	}

	private GameType getSelectedGameType() {
		final int selectedIndex = comboGameType.getSelectedIndex();
		
		if (selectedIndex >= 0)
			return GameType.values()[selectedIndex];
		else
			return null;
	}
	
	public static boolean showDialog(final IApplicationView applicationView, final GameSettings gameSettings) {
		final GameSettingDialog dialog = new GameSettingDialog(applicationView, gameSettings);
		
		dialog.setVisible(true);
		dialog.destroy();
			
		if (dialog.isConfirmed()) {
			gameSettings.assign (dialog.getSettings());
			
			return true;
		}
		else
			return false;
	}

	public void updateLanguage(final ILocalization localization) {
		super.updateLanguage (localization);
		
		this.setTitle(localization.translateString("GameSettingDialog.title"));
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final TitledBorder border = (TitledBorder) sideSettingPanelArray[color].getBorder();
			border.setTitle(LocalizedStrings.translateColor(localization, color));
		}
		
		labelGameType.setText(localization.translateString("GameSettingDialog.labelGameType.text"));
		localizeGameTypeCombo(localization);
	}

	private void localizeGameTypeCombo(final ILocalization localization) {
		final int selectedIndex = comboGameType.getSelectedIndex();
		comboGameType.removeAllItems();
		
		for (GameType type: GameType.values())
			comboGameType.addItem(type.getName(localization));
	
		comboGameType.setSelectedIndex(selectedIndex);
	}
	
	private void updateEnabled() {
		final boolean sideSettingEnabled = getSelectedGameType() == GameType.PLAY;
		
		for (SideSettingPanel panel: sideSettingPanelArray)
			panel.setEnabled(sideSettingEnabled);
	}
	
	private ActionListener comboGameType_ActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			updateEnabled();
		}
	};
}
