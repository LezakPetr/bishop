package bishop.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import bisGuiSwing.graphics.AwtImage;
import bishop.base.PieceType;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class PromotionDialog extends JDialog {
	
	private static class ButtonRecord {
		public int pieceType;
		public JButton button;
		
		public ButtonRecord (final int pieceType, final JButton button) {
			this.pieceType = pieceType;
			this.button = button;
		}
	}
	
	private List<ButtonRecord> buttonList;
	private JPanel panel;
	private int color;
	private IPieceSet pieceSet;
	private int selectedPieceType;
	private int ICON_WIDTH = 60;
	private int ICON_HEIGHT = 60;

	
	private PromotionDialog (final Frame owner, final int color, final IPieceSet pieceSet) {
		super (owner);
		
		this.color = color;
		this.pieceSet = pieceSet;
		this.selectedPieceType = PieceType.NONE;
				
		initializeComponents();
	}
	
	private void initializeComponents() {
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setSize(400, 100);
		this.setTitle("Select promotion piece");
		
		Utils.centerWindow(this);

		panel = new JPanel();
		this.add (panel);
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		buttonList = new LinkedList<ButtonRecord>();
		
		final double scale = bishop.gui.Utils.getIconScale(pieceSet.getPieceSize(), ICON_WIDTH, ICON_HEIGHT);
		final RasterPieceSet rasterPieceSet = pieceSet.renderScaledPieceSet(scale);
		
		for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
			final AwtImage origImage = (AwtImage) rasterPieceSet.getPieceImage(color, pieceType);
			final Icon icon = new ImageIcon(origImage.getImage());
			
			final JButton button = new JButton();
			button.setIcon(icon);
			button.addActionListener(button_ActionListener);
			
			panel.add(button);
			
			// Add record to the button list
			buttonList.add(new ButtonRecord(pieceType, button));
		}
	}
	
	private ActionListener button_ActionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent event) {
			for (Iterator<ButtonRecord> it = buttonList.iterator(); it.hasNext(); ) {
				final ButtonRecord record = it.next();
				
				if (record.button == event.getSource()) {
					selectedPieceType = record.pieceType;
					break;
				}
			}
			
			PromotionDialog.this.dispose();
		}
	};
	
	public static int selectPromotionPieceType (final Frame owner, final int color, final IPieceSet pieceSet) {
		final PromotionDialog dialog = new PromotionDialog(owner, color, pieceSet);
		dialog.setVisible(true);
		
		return dialog.selectedPieceType;
	}
}
