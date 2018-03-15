package bishop.gui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bishop.controller.ILocalization;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {
	
	public static final String VERSION = "1.0.1";
	
	private JLabel labelProduct;
	private JLabel labelCopyright;
	
	public AboutPanel(final ILocalization localization) {
		initializeComponents(localization);
	}

	private void initializeComponents(final ILocalization localization) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(Box.createVerticalStrut(10));
		
		labelProduct = new JLabel (localization.translateString("AboutDialog.labelProduct.textPrefix") + VERSION);
		labelProduct.setAlignmentX(CENTER_ALIGNMENT);
		this.add(labelProduct);
		
		labelCopyright = new JLabel (localization.translateString("AboutDialog.labelCopyright.text"));
		labelCopyright.setAlignmentX(CENTER_ALIGNMENT);
		this.add(labelCopyright);
		
		this.add(Box.createVerticalStrut(10));
	}
	
	
}
