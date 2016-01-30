package bishop.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import bishop.controller.ILocalization;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {

	private ILocalization localization;
	private AboutPanel panel;
	private JButton buttonOk;
	
	private AboutDialog(final Frame owner, final ILocalization localization) {
		super (owner);
		
		this.setResizable(false);
		this.localization = localization;
		
		initializeComponents();
	}
	
	private void initializeComponents() {
		this.setTitle(localization.translateString("AboutDialog.title"));
		
		this.setModal(true);
		this.setSize(new Dimension(300, 120));
		Utils.centerWindow(this);
		
		panel = new AboutPanel(localization);
		this.getContentPane().add(panel);
	
		panel.add(Box.createVerticalStrut(10));
		panel.add(Box.createVerticalGlue());
		
		buttonOk = new JButton(localization.translateString("Button.ok.text"));
		buttonOk.setAlignmentX(CENTER_ALIGNMENT);
		buttonOk.addActionListener(buttonOk_ActionListener);
		panel.add(buttonOk);
		
		panel.add(Box.createVerticalStrut(10));
	}
	
	private ActionListener buttonOk_ActionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent event) {
			AboutDialog.this.dispose();
		}
	};
	
	public static void showModalDialog(final Frame owner, final ILocalization localization) {
		final AboutDialog dialog = createDialog(owner, localization);
		dialog.setVisible(true);
	}
	
	public static AboutDialog createDialog(final Frame owner, final ILocalization localization) {
		return new AboutDialog(owner, localization);
	}
}
