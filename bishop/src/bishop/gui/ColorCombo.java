package bishop.gui;

import javax.swing.JComboBox;

import bishop.base.Color;
import bishop.controller.ILocalization;

@SuppressWarnings("serial")
public class ColorCombo extends JComboBox<String> {
	
	public void updateLanguage (final ILocalization localization) {
		final int selectedIndex = this.getSelectedIndex();
		this.removeAllItems();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			this.addItem(LocalizedStrings.translateColor(localization, color));
		}
		
		this.setSelectedIndex(selectedIndex);
	}
	
	public int getSelectedColor() {
		return getSelectedIndex() + Color.FIRST;
	}
	
	public void setSelectedColor (final int color) {
		this.setSelectedIndex(color - Color.FIRST);
	}
	
}
