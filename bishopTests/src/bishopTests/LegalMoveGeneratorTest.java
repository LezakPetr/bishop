package bishopTests;

import java.util.ArrayList;
import java.util.List;

import bishop.base.IMoveGenerator;
import bishop.base.LegalMoveGenerator;
import bishopTests.PseudoLegalMoveGeneratorTest.PositionWithMoves;


public class LegalMoveGeneratorTest extends MoveGeneratorTestBase {
	
	protected TestValue[] getTestValues() {
		final PseudoLegalMoveGeneratorTest.PositionWithMoves[] pseudoLegalTestValues = PseudoLegalMoveGeneratorTest.getPseudoLegalTestValues();
		final List<TestValue> result = new ArrayList<>();
		
		for (PositionWithMoves testCase: pseudoLegalTestValues) {
			result.add(new TestValue(testCase.getPositionFen(), testCase.getLegalMoves(), testCase.getLegalMoves()));
		}
		
		return result.toArray(new TestValue[result.size()]);
	}
	
	protected IMoveGenerator getMoveGenerator() {
		return new LegalMoveGenerator();
	}
}
