package bishop.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JTextField;

import bishop.base.IGameNode;
import bishop.base.ITreeIterator;
import bishop.controller.GameEditor;
import bishop.controller.IApplication;
import bishop.controller.IGameListener;

@SuppressWarnings("serial")
public class CommentaryViewPanel extends JPanel {
	
	private final GameEditor gameEditor;
	private JTextField fieldCommentary;
	
	public CommentaryViewPanel(final IApplication application) {
		this.gameEditor = application.getActualGameEditor();
		
		initializeComponents();
		gameEditor.getGameListenerRegistrar().addHandler(gameListener);
	}
	
	public void destroy() {
		gameEditor.getGameListenerRegistrar().removeHandler(gameListener);
	}
	
	private void initializeComponents() {
		this.setLayout(new GridBagLayout());
		
		fieldCommentary = new JTextField();
		fieldCommentary.setEditable(false);
		
		this.add (
			fieldCommentary,
			new GridBagConstraints(
				0, 0, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2),
				0, 0
			)
		);
	}
	
	private void updateData() {
		final ITreeIterator<IGameNode> iterator = gameEditor.getActualNodeIterator();
		final IGameNode node = iterator.getItem();
		
		fieldCommentary.setText(node.getCommentary());
	}
	
	private IGameListener gameListener = new IGameListener() {
		@Override
		public void onMove() {
			updateData();
		}
		
		@Override
		public void onGameChanged() {
			updateData();
		}
		
		@Override
		public void onActualPositionChanged() {
			updateData();
		}
	};

}
