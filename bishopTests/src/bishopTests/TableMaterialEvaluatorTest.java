package bishopTests;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.PieceType;
import bishop.base.Square;
import bishop.engine.TableMaterialEvaluator;


public class TableMaterialEvaluatorTest {
	
	@Test
	public void testReversible() {
		for (int index = 0; index < TableMaterialEvaluator.TABLE_SIZE; index++) {
			final MaterialHash materialHash = TableMaterialEvaluator.getMaterialHashForIndex(index);
			final int encodedIndex = TableMaterialEvaluator.getIndexForMaterialHash(materialHash);
			
			Assert.assertEquals(index, encodedIndex);
		}
	}
	
	@Test
	public void testExceeding() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
				final MaterialHash materialHash = new MaterialHash();
				materialHash.setOnTurn(color);
				
				for (int count = 0; count < Square.COUNT; count++) {
					final int index = TableMaterialEvaluator.getIndexForMaterialHash(materialHash);
					final boolean isStored = index >= 0;
					final boolean shouldBeStored = count < TableMaterialEvaluator.MAX_PIECE_COUNT;
					Assert.assertEquals(shouldBeStored, isStored);
					
					materialHash.addPiece(color, pieceType);
				}
			}
		}
	}
}
