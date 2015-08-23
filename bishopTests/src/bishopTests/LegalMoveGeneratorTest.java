package bishopTests;

import bishop.base.IMoveGenerator;
import bishop.base.LegalMoveGenerator;


public class LegalMoveGeneratorTest extends MoveGeneratorTestBase {
	
	protected TestValue[] getTestValues() {
		final TestValue[] pseudoLegalTestValues = PseudoLegalMoveGeneratorTest.getPseudoLegalTestValues();
		final TestValue[] legalTestValues = new TestValue[pseudoLegalTestValues.length];
		
		for (int i = 0; i < pseudoLegalTestValues.length; i++)
			legalTestValues[i] = new TestValue(pseudoLegalTestValues[i].positionFen, pseudoLegalTestValues[i].minExpectedMoves);
		
		return legalTestValues;
	}
	
	protected IMoveGenerator getMoveGenerator() {
		return new LegalMoveGenerator();
	}
}
