package bishop.engine;

import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

import java.util.function.Supplier;

public class AlgebraicPawnStructureEvaluator extends PawnStructureEvaluator {

	private long[] combinedEvaluations;
	private long cachedCombinedEvaluation;
	private long positionDependentCombinedEvaluation;
	private final PawnStructureCache structureCache;

	public AlgebraicPawnStructureEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		super(evaluationFactory);

		combinedEvaluations = new long[PositionEvaluationCoeffs.LAST];

		for (int coeff = ENDING_COEFFS.getFirstCoeff(); coeff < ENDING_COEFFS.getLastCoeff(); coeff++) {
			final IPositionEvaluation openingEvaluation = evaluationFactory.get();
			openingEvaluation.addCoeff(coeff + OPENING_COEFF_DIFF, Color.WHITE);

			final IPositionEvaluation middleGameEvaluation = evaluationFactory.get();
			middleGameEvaluation.addCoeff(coeff + MIDDLE_GAME_COEFF_DIFF, Color.WHITE);

			final IPositionEvaluation endingEvaluation = evaluationFactory.get();
			endingEvaluation.addCoeff(coeff, Color.WHITE);

			combinedEvaluations[coeff] = CombinedEvaluation.combine(
					openingEvaluation.getEvaluation(),
					middleGameEvaluation.getEvaluation(),
					endingEvaluation.getEvaluation()
			);
		}

		this.structureCache = new PawnStructureCache(
				(whitePawnMask, blackPawnMask) -> {
					assert whitePawnMask == structureData.getPawnMask(Color.WHITE);
					assert blackPawnMask == structureData.getPawnMask(Color.BLACK);

					evaluatePawnStructure();

					return cachedCombinedEvaluation;
				}
		);
	}

	@Override
	public IPositionEvaluation evaluate(final Position position, final int gameStage) {
		long combinedEvaluation = CombinedEvaluation.ACCUMULATOR_BASE;
		combinedEvaluation += structureCache.getCombinedEvaluation(
				position.getPiecesMask(Color.WHITE, PieceType.PAWN),
				position.getPiecesMask(Color.BLACK, PieceType.PAWN)
		);

		evaluatePositionDependent(position);
		combinedEvaluation += positionDependentCombinedEvaluation;

		evaluation.addEvaluation(
				CombinedEvaluation.getDecoderForGameStage(gameStage).decode(combinedEvaluation)
		);

		return evaluation;
	}

	@Override
	protected void addCachedEvaluation(final int endingCoeff, final int color) {
		cachedCombinedEvaluation += Color.colorNegate(color, combinedEvaluations[endingCoeff]);
	}

	@Override
	protected void addPositionDependentEvaluation(final int endingCoeff, final int color, final int coeffCount) {
		positionDependentCombinedEvaluation += Color.colorNegate(color, coeffCount * combinedEvaluations[endingCoeff]);
	}

	@Override
	public void clear() {
		super.clear();

		cachedCombinedEvaluation = 0;
		positionDependentCombinedEvaluation = 0;
	}
}