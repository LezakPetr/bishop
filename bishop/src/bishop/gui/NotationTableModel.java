package bishop.gui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import bishop.base.Annotation;
import bishop.base.Color;
import bishop.base.Game;
import bishop.base.IGameNode;
import bishop.base.INotationWriter;
import bishop.base.ITreeIterator;
import bishop.base.Move;
import bishop.base.Position;
import bishop.base.StandardAlgebraicNotationWriter;
import bishop.controller.GameEditor;
import bishop.controller.IApplication;
import bishop.controller.IGameListener;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public class NotationTableModel extends AbstractTableModel implements ILocalizedComponent {
	
	@SuppressWarnings("unused")
	private static final int COLUMN_FIRST = 0;
	
	private static final int COLUMN_MOVE_NUMBER = 0;
	private static final int COLUMN_WHITE = 1;
	private static final int COLUMN_BLACK = 2;
	
	private static final int COLUMN_LAST = 3;
	
	private final String[] columnNames;
	
	
	private static class RowRecord {
		public int variantDepth;
		public ITreeIterator<IGameNode> whiteIterator;
		public ITreeIterator<IGameNode> blackIterator;		
	}
	
	private final List<RowRecord> rowRecordList;
	private final INotationWriter notation;
	private IApplication application;
	private JTable table;
	
	
	public NotationTableModel(final IApplication application) {
		this.application = application;
		this.rowRecordList = new ArrayList<NotationTableModel.RowRecord>();
		this.notation = new StandardAlgebraicNotationWriter();
		this.columnNames = new String[COLUMN_LAST];
		
		application.getActualGameEditor().getGameListenerRegistrar().addHandler(gameListener);
		updateRowRecordList();
		
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	public void destroy() {
		application.getActualGameEditor().getGameListenerRegistrar().removeHandler(gameListener);
		application.getLocalizedComponentRegister().removeComponent(this);
	}
	
	public void setTable (final JTable table) {
		this.table = table;
	}

	public int getColumnCount() {
		return COLUMN_LAST;
	}

	public int getRowCount() {
		return rowRecordList.size();
	}
	
	public String getColumnName (final int columnIndex) {
		return columnNames[columnIndex];
	}
	
	private String writeMove (final ITreeIterator<IGameNode> iterator) {
		if (iterator != null) {
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			
			final ITreeIterator<IGameNode> parentIterator = iterator.copy();
			parentIterator.moveParent();
			
			final Position beginPosition = parentIterator.getItem().getTargetPosition();
			final IGameNode node = iterator.getItem();
			final Move move = node.getMove();
			
			notation.writeMove(printWriter, beginPosition, move);
			
			final Annotation annotation = node.getAnnotation();
			
			if (annotation != Annotation.NONE) {
				printWriter.print(' ');
				printWriter.print(annotation.toString());
			}
			
			printWriter.flush();
			
			return stringWriter.toString();
		}
		else
			return "";
	}

	public Object getValueAt(final int rowIndex, final int columnIndex) {
		final RowRecord rowRecord = rowRecordList.get(rowIndex);
		final String cellString;
		ITreeIterator<IGameNode> iterator = null;
		
		switch (columnIndex) {
			case COLUMN_MOVE_NUMBER:
				if (rowRecord.whiteIterator != null)
					cellString = Integer.toString(rowRecord.whiteIterator.getItem().getMoveNumber());
				else
					cellString = Integer.toString(rowRecord.blackIterator.getItem().getMoveNumber());
				
				break;
				
			case COLUMN_WHITE:
				cellString = writeMove (rowRecord.whiteIterator);
				iterator = rowRecord.whiteIterator;
				break;
				
			case COLUMN_BLACK:
				cellString = writeMove (rowRecord.blackIterator);
				iterator = rowRecord.blackIterator;
				break;
				
			default:
				throw new RuntimeException("Unknown column");
		}
		
		final java.awt.Color background;
		
		if (iterator != null && iterator.equals(application.getActualGameEditor().getActualNodeIterator())) {
			background = table.getSelectionBackground();
		}
		else {
			final float gray = Math.max(1.0f - 0.1f * rowRecord.variantDepth, 0.5f);
			background = new java.awt.Color (gray, gray, gray, 1);
		}
		
		return new FormattedCellData(cellString, java.awt.Color.BLACK, background);
	}
	
	private RowRecord addNonEmptyRowRecord (final RowRecord record) {
		if (record.whiteIterator != null || record.blackIterator != null) {
			rowRecordList.add(record);
			
			return new RowRecord();
		}
		else
			return record;
	}
	
	private void createMoveRecordList(final ITreeIterator<IGameNode> firstMoveIterator) {
		class StackRecord {
			public final ITreeIterator<IGameNode> iterator;
			public final int variantDepth;
			
			public StackRecord (final ITreeIterator<IGameNode> iterator, final int variantDepth) {
				this.iterator = iterator;
				this.variantDepth = variantDepth;
			}
		}
		
		final Stack<StackRecord> stack = new Stack<StackRecord>();
		stack.push(new StackRecord(firstMoveIterator, 0));
		
		RowRecord rowRecord = new RowRecord();
		
		while (!stack.isEmpty()) {
			final StackRecord stackRecord = stack.pop();
			final ITreeIterator<IGameNode> iterator = stackRecord.iterator;
			final int onTurn = Color.getOppositeColor(iterator.getItem().getTargetPosition().getOnTurn());
			
			rowRecord.variantDepth = stackRecord.variantDepth;
			
			if (onTurn == Color.WHITE)
				rowRecord.whiteIterator = iterator;		
			else {
				rowRecord.blackIterator = iterator;
				rowRecord = addNonEmptyRowRecord(rowRecord);
			}
			
			if (iterator.hasChild()) {
				final ITreeIterator<IGameNode> childIterator = iterator.copy();
				childIterator.moveFirstChild();
				
				stack.push(new StackRecord(childIterator, stackRecord.variantDepth));
			}
			else
				rowRecord = addNonEmptyRowRecord(rowRecord);
			
			if (iterator.hasNextSibling() && !iterator.hasPreviousSibling()) {
				final ITreeIterator<IGameNode> siblingIterator = iterator.copy();
				
				while (siblingIterator.hasNextSibling())
					siblingIterator.moveNextSibling();
				
				while (!siblingIterator.equals(iterator)) {
					stack.push(new StackRecord(siblingIterator.copy(), stackRecord.variantDepth+1));
					siblingIterator.movePreviousSibling();
				}
				
				rowRecord = addNonEmptyRowRecord(rowRecord);
			}
		}
	}
	
	private void updateRowRecordList() {
		rowRecordList.clear();
		
		final Game game = application.getActualGameEditor().getGame();

		ITreeIterator<IGameNode> iterator = game.getRootIterator();
		
		if (iterator.hasChild()) {
			iterator.moveFirstChild();
			
			createMoveRecordList(iterator);
		}
		
		this.fireTableDataChanged();
	}

	private final IGameListener gameListener = new IGameListener() {
		public void onActualPositionChanged() {
			fireTableDataChanged();
		}
	
		public void onGameChanged() {
			updateRowRecordList();
		}
		
		public void onMove() {
			updateRowRecordList();
		}
	};
	
	public void onSelectionChanged(final int row, final int column) {
		if (row >= 0 && row < rowRecordList.size()) {
			final RowRecord record = rowRecordList.get(row);
			final ITreeIterator<IGameNode> iterator;
			
			switch (column) {
				case COLUMN_WHITE:
					iterator = record.whiteIterator;
					break;
	
				case COLUMN_BLACK:
					iterator = record.blackIterator;
					break;
	
				default:
					return;
			}
			
			if (iterator != null) {
				final GameEditor editor = application.getActualGameEditor();
				editor.setActualNodeIterator(iterator);
			}
		}
	}

	public void updateLanguage(final ILocalization localization) {
		columnNames[COLUMN_MOVE_NUMBER] = localization.translateString("NotationTableModel.column.moveNumber");
		columnNames[COLUMN_WHITE] = LocalizedStrings.translateColor(localization, Color.WHITE);
		columnNames[COLUMN_BLACK] = LocalizedStrings.translateColor(localization, Color.BLACK);
		
		fireTableStructureChanged();
	}

}
