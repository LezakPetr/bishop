package bishopTests;

import org.junit.Assert;
import org.junit.Test;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.IPosition;
import bishop.base.Position;
import bishop.base.PositionValidator;
import bishop.tablebase.ITable;
import bishop.tablebase.ITableIterator;
import bishop.tablebase.PositionTableIterator;
import bishop.tablebase.TableDefinition;

public class TableDefinitionTest {
	
	private static final String[] COVERAGE_TESTED_MATERIALS = {
		"00000-00000", "00001-00000", "00000-00001", "00001-00001"
	};
	
	private static final int VERSION = 2;
	
	private static class DummyTable implements ITable {
		
		private final TableDefinition definition;
		
		public DummyTable(final TableDefinition definition) {
			this.definition = definition;
		}

		@Override
		public TableDefinition getDefinition() {
			return definition;
		}

		@Override
		public int getResult(long index) {
			throw new RuntimeException("Method not supported");
		}

		@Override
		public int getPositionResult(IPosition position) {
			throw new RuntimeException("Method not supported");
		}

		@Override
		public ITableIterator getIterator() {
			throw new RuntimeException("Method not supported");
		}

		@Override
		public void setResult(long tableIndex, int result) {
			throw new RuntimeException("Method not supported");
		}
	}
	
	private void testTable (final ITable table, final int epFile) {
		final PositionTableIterator iterator = new PositionTableIterator(table, epFile);
		final Position position = new Position();
		
		final PositionValidator validator  = new PositionValidator();
		validator.setPosition(position);
		
		final TableDefinition defintion = table.getDefinition();
		
		while (iterator.isValid()) {
			iterator.fillPosition(position);
			
			if (defintion.hasSameCountOfPieces(position) && validator.checkPosition()) {
				Assert.assertTrue(defintion.calculateTableIndex(position) >= 0);
			}
			
			iterator.next();
		}
	}
	
	/**
	 * Verifies that all legal positions are really in the table.
	 */
	@Test
	public void testTableCoverage() {
		for (String testedMaterial: COVERAGE_TESTED_MATERIALS) {
			for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
				final MaterialHash materialHash = new MaterialHash(testedMaterial, onTurn);
				final boolean isWhitePawn = materialHash.getPieceCount(Color.WHITE, PieceType.PAWN) > 0;
				final boolean isBlackPawn = materialHash.getPieceCount(Color.BLACK, PieceType.PAWN) > 0;
				final boolean epPossible = isWhitePawn && isBlackPawn;
				
				final TableDefinition definition = new TableDefinition(VERSION, materialHash);
				final ITable table = new DummyTable(definition);
				
				testTable (table, File.NONE);
				
				if (epPossible) {
					for (int epFile = File.FIRST; epFile < File.LAST; epFile++)
						testTable (table, epFile);
				}
			}
		}
	}
}
