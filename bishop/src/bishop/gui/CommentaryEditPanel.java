package bishop.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bishop.base.Annotation;
import bishop.base.IGameNode;
import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public class CommentaryEditPanel extends JPanel implements ILocalizedComponent {

	private final IApplication application;
	private final IGameNode node;
	
	private JLabel labelAnnotation;
	private JComboBox<Annotation> comboAnnotation;
	
	private JLabel labelCommentary;
	private JTextField fieldCommentary;
	
	public CommentaryEditPanel(final IApplication application, final IGameNode node) {
		this.application = application;
		this.node = node;
		
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
	}
	
	private void initializeComponents() {
		this.setLayout(new GridBagLayout());
		
		// Annotation
		labelAnnotation = new JLabel();
		
		this.add(
			labelAnnotation,
			new GridBagConstraints(
				0, 0, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(2, 2, 5, 5),
				0, 0
			)
		);
		
		comboAnnotation = new JComboBox<Annotation>();
		
		for (Annotation item: Annotation.values())
			comboAnnotation.addItem(item);
		
		this.add (
			comboAnnotation,
			new GridBagConstraints(
				1, 0, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(2, 0, 5, 2),
				0, 0
			)
		);
		
		// Commentary
		labelCommentary = new JLabel();
		
		this.add (
			labelCommentary,
			new GridBagConstraints(
				0, 1, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 2, 2, 5),
				0, 0
			)
		);

		fieldCommentary = new JTextField();
		
		this.add (
			fieldCommentary,
			new GridBagConstraints(
				1, 1, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 2, 2),
				0, 0
			)
		);
	}
	
	@Override
	public void updateLanguage(final ILocalization localization) {
		labelAnnotation.setText(localization.translateString("CommentaryPanel.labelAnnotation"));
		labelCommentary.setText(localization.translateString("CommentaryPanel.labelCommentary"));
	}

	public void loadData() {
		comboAnnotation.setSelectedItem(node.getAnnotation());
		fieldCommentary.setText(node.getCommentary());
	}
	
	public void saveData() {
		final int selectedIndex = comboAnnotation.getSelectedIndex();
		final Annotation annotation = comboAnnotation.getItemAt(selectedIndex);
		
		node.setAnnotation(annotation);
		node.setCommentary(fieldCommentary.getText());
	}
}
