package bishop.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class FormattedCellRenderer extends JLabel implements TableCellRenderer {
	
	public FormattedCellRenderer() {
		this.setOpaque(true);
	}

	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final FormattedCellData data = (FormattedCellData) value;
		
		this.setText(data.getText());
		this.setForeground(data.getForeground());
		this.setBackground(data.getBackground());
		
		return this;
	}

}
