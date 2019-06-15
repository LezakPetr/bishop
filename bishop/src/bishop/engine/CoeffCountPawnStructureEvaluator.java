package bishop.engine;

import bishop.base.Color;
import bishop.base.Position;

import java.util.function.Supplier;

public class CoeffCountPawnStructureEvaluator extends PawnStructureEvaluator {
	private final IPositionEvaluation openingCachedEvaluation;
	private final IPositionEvaluation middleGameCachedEvaluation;
	private final IPositionEvaluation endingCachedEvaluation;

	private final IPositionEvaluation openingPositionDependentEvaluation;
	private final IPositionEvaluation middleGamePositionDependentEvaluation;
	private final IPositionEvaluation endingPositionDependentEvaluation;


	public CoeffCountPawnStructureEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		super(evaluationFactory);

		this.openingCachedEvaluation = evaluationFactory.get();
		this.middleGameCachedEvaluation = evaluationFactory.get();
		this.endingCachedEvaluation = evaluationFactory.get();

		this.openingPositionDependentEvaluation = evaluationFactory.get();
		this.middleGamePositionDependentEvaluation = evaluationFactory.get();
		this.endingPositionDependentEvaluation = evaluationFactory.get();

	}

	@Override
	public IPositionEvaluation evaluate(final Position position, final int gameStage) {
		evaluatePawnStructure();

		final int openingCount = CombinedEvaluation.getComponentMultiplicator (gameStage, CombinedEvaluation.COMPONENT_OPENING);
		final int middleGameCount = CombinedEvaluation.getComponentMultiplicator (gameStage, CombinedEvaluation.COMPONENT_MIDDLE_GAME);
		final int endingCount = CombinedEvaluation.getComponentMultiplicator (gameStage, CombinedEvaluation.COMPONENT_ENDING);

		evaluation.addSubEvaluation(openingCachedEvaluation, openingCount);
		evaluation.addSubEvaluation(middleGameCachedEvaluation, middleGameCount);
		evaluation.addSubEvaluation(endingCachedEvaluation, endingCount);

		evaluation.addSubEvaluation(openingPositionDependentEvaluation, openingCount);
		evaluation.addSubEvaluation(middleGamePositionDependentEvaluation, middleGameCount);
		evaluation.addSubEvaluation(endingPositionDependentEvaluation, endingCount);

		evaluation.shiftRight(CombinedEvaluation.ALPHA_BITS);

		return evaluation;
	}

	@Override
	protected void addCachedEvaluation(final int endingCoeff, final int color) {
		endingCachedEvaluation.addCoeff(endingCoeff, color);
		middleGameCachedEvaluation.addCoeff(endingCoeff + MIDDLE_GAME_COEFF_DIFF, color);
		openingCachedEvaluation.addCoeff(endingCoeff + OPENING_COEFF_DIFF, color);
	}

	@Override
	protected void addPositionDependentEvaluation(final int endingCoeff, final int color, final int coeffCount) {
		endingPositionDependentEvaluation.addCoeff(endingCoeff, color, coeffCount);
		middleGamePositionDependentEvaluation.addCoeff(endingCoeff + MIDDLE_GAME_COEFF_DIFF, color, coeffCount);
		openingPositionDependentEvaluation.addCoeff(endingCoeff + OPENING_COEFF_DIFF, color, coeffCount);
	}

	@Override
	public void clear() {
		super.clear();

		openingCachedEvaluation.clear();
		middleGameCachedEvaluation.clear();
		endingCachedEvaluation.clear();

		openingPositionDependentEvaluation.clear();
		middleGamePositionDependentEvaluation.clear();
		endingPositionDependentEvaluation.clear();
	}

}
