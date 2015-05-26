package bishop.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import bishop.controller.IApplication;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public abstract class OkCancelDialog extends JDialog implements ILocalizedComponent {
	
	protected final IApplicationView applicationView;
	protected final IApplication application;
	protected JPanel contentPanel;
	private JPanel rootPanel;
	private OkCancelPanel panelButtons;

	
	public OkCancelDialog (final IApplicationView applicationView) {
		super (applicationView.getMainFrame());
		
		this.applicationView = applicationView;
		this.application = applicationView.getApplication();
		
		initializeComponents();
	}
	
	protected abstract void onOk();
	protected abstract void onCancel();

	
	private void initializeComponents() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
		
		contentPanel = new JPanel();

		
		rootPanel.add(contentPanel);
		
		rootPanel.add(Box.createVerticalGlue());

		// Buttons
		panelButtons = new OkCancelPanel(application, buttonOk_ActionListener, buttonCancel_ActionListener);
		
		rootPanel.add(panelButtons);
		this.add(rootPanel);
		
		setKey (KeyEvent.VK_ENTER, "enter", buttonOk_ActionListener);
		setKey (KeyEvent.VK_ESCAPE, "esc", buttonCancel_ActionListener);
	}
	
	public void destroy() {
		panelButtons.destroy();
	}
	

	private void setKey (final int key, final String actionKey, final Action action) {
		final InputMap inputMap = rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final KeyStroke stroke = KeyStroke.getKeyStroke(key, 0);
		inputMap.put(stroke, actionKey);
		
		final ActionMap actionMap = rootPanel.getActionMap();
		actionMap.put(actionKey, action);
	}
	
	private Action buttonOk_ActionListener = new AbstractAction() {
		@Override
		public void actionPerformed(final ActionEvent event) {
			onOk();
		}
	};
	
	private Action buttonCancel_ActionListener = new AbstractAction() {
		@Override
		public void actionPerformed(final ActionEvent event) {
			onCancel();
		}
	};

}
