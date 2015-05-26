package bishop.gui;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public class NotationPanel extends JPanel implements ILocalizedComponent {
	
	private NotationTable table;
	private JScrollPane scrollPane;
	private IApplication application;
	private CommentaryViewPanel commentaryPanel;
		
	public NotationPanel(final IApplication application) {
		this.application = application;
		
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	private void initializeComponents() {
		this.setLayout(new GridBagLayout());
		
		table = new NotationTable(application);
		scrollPane = new JScrollPane(table);
		
		this.add (
			scrollPane,
			new GridBagConstraints(
				0, 0, 1, 1,
				1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
		
		commentaryPanel = new CommentaryViewPanel(application);
		
		this.add (
			commentaryPanel,
			new GridBagConstraints(
				0, 1, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
	}
	
	public void destroy() {
		table.destroy();
		commentaryPanel.destroy();
		application.getLocalizedComponentRegister().removeComponent(this);
	}	

	public void updateLanguage(final ILocalization localization) {
	}

}
