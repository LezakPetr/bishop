package bishop.gui;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import bishop.controller.IApplication;

@SuppressWarnings("serial")
public class NotationTable extends JTable {
	
	private final NotationTableModel model;
	private final FormattedCellRenderer renderer;
	
	
	public NotationTable(final IApplication application) {
		model = new NotationTableModel(application);
		renderer = new FormattedCellRenderer();
		
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.getSelectionModel().addListSelectionListener(selectionListener);
		
		this.setModel(model);
		model.setTable (this);
	}
	
	public void destroy() {
		model.destroy();
	}
	
	public TableCellRenderer getCellRenderer (final int row, final int column) {
		return renderer;
	}
	
	private final ListSelectionListener selectionListener = new ListSelectionListener() {
		public void valueChanged(final ListSelectionEvent event) {
			final int selectedRow = getSelectedRow();
			
			if (selectedRow >= 0) {
				model.onSelectionChanged(selectedRow, getSelectedColumn());
				
				final ListSelectionModel selectionModel = getSelectionModel();
				selectionModel.removeSelectionInterval(0, getRowCount());
			}
		}
	};

}
