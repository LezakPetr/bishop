package bishop.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bishop.controller.ILocalization;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {

	public static final String VERSION = "1.0.0";
	
	private ILocalization localization;
	private JPanel panel;
	private JLabel labelProduct;
	private JLabel labelCopyright;
	private JButton buttonOk;
	
	private AboutDialog(final Frame owner, final ILocalization localization, final boolean modal) {
		super (owner);
		
		this.setResizable(false);
		this.localization = localization;
		
		initializeComponents(modal);
	}
	
	private void initializeComponents(final boolean modal) {
		this.setTitle(localization.translateString("AboutDialog.title"));
		
		this.setModal(modal);
		this.setSize(new Dimension(300, 170));
		Utils.centerWindow(this);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		this.getContentPane().add(panel);
		
		panel.add(Box.createVerticalStrut(10));
		
		labelProduct = new JLabel (localization.translateString("AboutDialog.labelProduct.textPrefix") + VERSION);
		labelProduct.setAlignmentX(CENTER_ALIGNMENT);
		panel.add(labelProduct);
		
		labelCopyright = new JLabel (localization.translateString("AboutDialog.labelCopyright.text"));
		labelCopyright.setAlignmentX(CENTER_ALIGNMENT);
		panel.add(labelCopyright);
		
		panel.add(Box.createVerticalStrut(10));
		
		if (modal) {
			panel.add(Box.createVerticalGlue());
			
			buttonOk = new JButton(localization.translateString("Button.ok.text"));
			buttonOk.setAlignmentX(CENTER_ALIGNMENT);
			buttonOk.addActionListener(buttonOk_ActionListener);
			panel.add(buttonOk);
		}
		
		panel.add(Box.createVerticalStrut(10));
	}
	
	private ActionListener buttonOk_ActionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent event) {
			AboutDialog.this.dispose();
		}
	};
	
	public static void showModalDialog(final Frame owner, final ILocalization localization) {
		final AboutDialog dialog = createDialog(owner, localization, true);
		dialog.setVisible(true);
	}
	
	public static AboutDialog createDialog(final Frame owner, final ILocalization localization, final boolean modal) {
		return new AboutDialog(owner, localization, modal);
	}
}
