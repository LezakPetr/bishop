package bishop.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public class GameSelectorTableModel extends AbstractTableModel implements ILocalizedComponent {
	
	private static final int COLUMN_GAME = 0;
	private static final int COLUMN_LAST = 1;
	
	private final IApplication application;
	private final List<?> valueList;
	private final String[] columnNames;
	
	
	public GameSelectorTableModel(final IApplication application, final List<?> valueList) {
		this.application = application;
		this.valueList = valueList;
		this.columnNames = new String[COLUMN_LAST];
		
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
	}

	public int getColumnCount() {
		return COLUMN_LAST;
	}

	public int getRowCount() {
		return valueList.size();
	}

	public Object getValueAt(final int rowIndex, final int columnIndex) {
		final Object value = valueList.get(rowIndex);
		
		return value.toString();
	}
	
	public String getColumnName (final int columnIndex) {
		return columnNames[columnIndex];
	}
	
	public void updateLanguage(final ILocalization localization) {
		columnNames[COLUMN_GAME] = localization.translateString("GameSelectorTableModel.columnGame.name");
		
		fireTableStructureChanged();
	}

}
