package bishop.gui;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public class OkCancelPanel extends JPanel implements ILocalizedComponent {
	
	private final IApplication application;
	private final Action okActionListener;
	private final Action cancelActionListener;
	private JButton buttonOk;
	private JButton buttonCancel;

	
	public OkCancelPanel(final IApplication application, final Action okActionListener, final Action cancelActionListener) {
		this.application = application;
		this.okActionListener = okActionListener;
		this.cancelActionListener = cancelActionListener;
		
		initialize();
		
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	private void initialize() {
		final Dimension buttonSize = new Dimension(90, 25);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		this.add(Box.createHorizontalGlue());
		
		buttonOk = new JButton();
		buttonOk.addActionListener(okActionListener);
		buttonOk.setPreferredSize(buttonSize);
		buttonOk.setMaximumSize(buttonSize);
		
		this.add(buttonOk);
		
		this.add (Box.createHorizontalStrut(5));
		
		buttonCancel = new JButton();
		buttonCancel.addActionListener(cancelActionListener);
		buttonCancel.setPreferredSize(buttonSize);
		buttonCancel.setMaximumSize(buttonSize);
		
		this.add(buttonCancel);
		
		this.add (Box.createHorizontalStrut(5));
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
	}
	
	public void updateLanguage(final ILocalization localization) {
		buttonOk.setText(localization.translateString(LocalizedStrings.BUTTON_OK_KEY));
		buttonCancel.setText(localization.translateString(LocalizedStrings.BUTTON_CANCEL_KEY));
	}
	
}
