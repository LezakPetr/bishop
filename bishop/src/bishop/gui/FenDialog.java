package bishop.gui;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import utils.IoUtils;
import bishop.base.Fen;
import bishop.base.Position;
import bishop.controller.GameEditor;
import bishop.controller.ILocalization;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class FenDialog extends OkCancelDialog {
	
	private JLabel labelFen;
	private JTextField fieldFen;
	
	private static final int HORIZONTAL_SPACE = 5;
	private static final Dimension DIALOG_SIZE = new Dimension(500, 100);
	private static final Dimension CONTENT_PANEL_SIZE = new Dimension(10000, 25);
	

	public FenDialog (final IApplicationView applicationView) {
		super (applicationView);
		
		initialize();
		loadFen();
	}
	
	private void initialize() {
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
		
		super.destroy();
	}

	private void loadFen() {
		final GameEditor gameEditor = application.getActualGameEditor();
		final Position position = gameEditor.getActualPositionSource().getPosition();
		
		final Fen fen = new Fen();
		fen.setPosition(position);
		
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		
		fen.writeFen(printWriter);
		printWriter.flush();
		
		fieldFen.setText(stringWriter.toString());
		fieldFen.selectAll();
	}
	
	private boolean saveFen() {
		try {
			final GameEditor gameEditor = application.getActualGameEditor();
			final Position oldPosition = gameEditor.getActualPositionSource().getPosition();
			
			final Fen fen = new Fen();
			fen.readFen(IoUtils.getPushbackReader(fieldFen.getText()));
			
			final Position newPosition = fen.getPosition();
			
			if (!oldPosition.equals(newPosition)) {
				gameEditor.newGame(newPosition);
			}
			
			return true;
		}
		catch (Exception ex) {
			final ILocalization localization = application.getLocalization();
			final String message = localization.translateString("FenDialog.parseError");
			final String title = localization.translateString("Messages.error");
			
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	protected void initializeComponents() {
		this.setModal(true);
		this.setSize(DIALOG_SIZE);
		
		Utils.centerWindow(this);

		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		contentPanel.setPreferredSize(CONTENT_PANEL_SIZE);
		contentPanel.setMaximumSize(CONTENT_PANEL_SIZE);
		
		contentPanel.add(Box.createHorizontalStrut(HORIZONTAL_SPACE));
		
		labelFen = new JLabel();
		contentPanel.add(labelFen);
		
		contentPanel.add(Box.createHorizontalStrut(HORIZONTAL_SPACE));
		
		fieldFen = new JTextField();
		contentPanel.add(fieldFen);
		
		contentPanel.add(Box.createHorizontalStrut(HORIZONTAL_SPACE));
	}
	
	@Override
	protected void onOk() {
		try {
			if (saveFen()) {
				FenDialog.this.dispose();
			}
		}
		catch (Throwable th) {
			JOptionPane.showMessageDialog(FenDialog.this, th.getMessage());
		}		
	}

	@Override
	protected void onCancel() {
		FenDialog.this.dispose();	
	}
	
	@Override
	public void updateLanguage(final ILocalization localization) {
		this.setTitle(localization.translateString("FenDialog.title"));
		labelFen.setText(localization.translateString("FenDialog.labelFen"));
	}
	
	public static void showDialog(final IApplicationView applicationView) {
		final FenDialog dialog = new FenDialog(applicationView);
		
		dialog.setVisible(true);
		dialog.destroy();
	}

}
