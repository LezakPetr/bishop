package bishop.gui;

import java.awt.Dimension;

import javax.swing.OverlayLayout;

import bishop.base.IGameNode;
import bishop.controller.ILocalization;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class CommentaryDialog extends OkCancelDialog {
	
	private final IGameNode node;
	
	private CommentaryEditPanel commentaryPanel;
	private boolean confirmed;
	
	private static final Dimension DIALOG_SIZE = new Dimension(300, 125);
	

	public CommentaryDialog (final IApplicationView applicationView, final IGameNode node) {
		super (applicationView);
		
		this.node = node;
		this.confirmed = false;
		
		initializeComponents();
		commentaryPanel.loadData();
		
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	@Override
	public void destroy() {
		commentaryPanel.destroy();
		application.getLocalizedComponentRegister().removeComponent(this);
		
		super.destroy();
	}
	
	protected void initializeComponents() {
		this.setModal(true);
		this.setSize(DIALOG_SIZE);
		
		Utils.centerWindow(this);

		contentPanel.setLayout(new OverlayLayout(contentPanel));
		commentaryPanel = new CommentaryEditPanel(application, node);
		contentPanel.add(commentaryPanel);
	}
	
	@Override
	protected void onOk() {
		commentaryPanel.saveData();
		confirmed = true;
		
		CommentaryDialog.this.dispose();		
	}

	@Override
	protected void onCancel() {
		CommentaryDialog.this.dispose();
	}

	
	public static boolean showDialog(final IApplicationView applicationView, final IGameNode node) {
		final CommentaryDialog dialog = new CommentaryDialog(applicationView, node);
		
		dialog.setVisible(true);
		dialog.destroy();
		
		return dialog.confirmed;
	}

	@Override
	public void updateLanguage(ILocalization localization) {
		this.setTitle(localization.translateString("CommentaryDialog.title"));
	}
	
}
