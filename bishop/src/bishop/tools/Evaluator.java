package bishop.tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.function.Supplier;

import bishop.base.Fen;
import bishop.base.Position;
import bishop.engine.AttackCalculator;
import bishop.engine.IPositionEvaluation;
import bishop.engine.PositionEvaluatorSwitch;
import bishop.engine.PositionEvaluatorSwitchSettings;
import utils.IoUtils;

public class Evaluator {

	public static void main (final String[] args) throws IOException {
		try {
			final PushbackReader reader = new PushbackReader(new InputStreamReader(System.in));
			
			while (!IoUtils.isEndOfStream(reader)) {
				final String positionFen = IoUtils.readString(reader, ch -> ch != IoUtils.NEW_LINE.charAt(0));
				IoUtils.readChar(reader);   // Drop the new line
				
				evaluatePosition (positionFen);
			}
		}
		catch (Throwable th) {
			th.printStackTrace();
		}
	}

	private static void evaluatePosition(final String positionFen) throws IOException {
/*		final Fen fen = new Fen();
		fen.readFenFromString(positionFen);
		
		final Position position = fen.getPosition();
		final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
		final PositionEvaluationFeatures coeffs = new PositionEvaluationFeatures();
		final Supplier<IPositionEvaluation> evaluationFactory = () -> new FeatureCountPositionEvaluation(coeffs);
		final PositionEvaluatorSwitch evaluator = new PositionEvaluatorSwitch(settings, evaluationFactory.get());
		final AttackCalculator attackCalculator = new AttackCalculator();
		evaluator.evaluate(position, attackCalculator);
		
		System.out.println("Evaluation:");
		System.out.println(evaluator.getEvaluation());*/
	}
}
