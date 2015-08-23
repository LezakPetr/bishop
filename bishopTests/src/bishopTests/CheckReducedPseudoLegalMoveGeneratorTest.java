package bishopTests;

import bishop.base.IMoveGenerator;
import bishop.base.PseudoLegalMoveGenerator;

public class CheckReducedPseudoLegalMoveGeneratorTest extends MoveGeneratorTestBase {

	protected TestValue[] getTestValues() {
		return PseudoLegalMoveGeneratorTest.getPseudoLegalTestValues();
	}
	
	protected IMoveGenerator getMoveGenerator() {
		final PseudoLegalMoveGenerator generator = new PseudoLegalMoveGenerator();
		generator.setReduceMovesInCheck(true);
		
		return generator;
	}
}
