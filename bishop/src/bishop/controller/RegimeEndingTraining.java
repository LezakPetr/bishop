package bishop.controller;

import javax.swing.JOptionPane;

import bishop.base.HandlerRegistrarImpl;
import bishop.base.IMoveWalker;
import bishop.base.LegalMoveGenerator;
import bishop.base.Move;
import bishop.base.MoveType;
import bishop.base.Position;
import bishop.engine.Evaluation;
import bishop.engine.TablebasePositionEvaluator;
import utils.Holder;

public class RegimeEndingTraining extends MoveRegime<IMoveRegimeListener> {
	
	public RegimeEndingTraining(final IApplication application) {
		super(application, new HandlerRegistrarImpl<IMoveRegimeListener>());
	}
	
	public void destroy() {
		super.destroy();
	}

	/**
	 * Activates this regime.
	 */
	public void activateRegime() {
		super.activateRegime();
		
		positionChanged();
		
		final GameEditor gameEditor = application.getActualGameEditor();
		gameEditor.getGameListenerRegistrar().addHandler(gameListener);
	}
	
	/**
	 * Deactivates this regime.
	 */
	public void deactivateRegime() {
		final GameEditor gameEditor = application.getActualGameEditor();
		gameEditor.getGameListenerRegistrar().removeHandler(gameListener);
		
		super.deactivateRegime();
	}
	
	private void positionChanged() {
		final GameEditor gameEditor = application.getActualGameEditor();
		final Position position = gameEditor.getActualPositionSource().getPosition();
		
		final int onTurn = position.getOnTurn();
		final GameSettings gameSettings = application.getSettings().getGameSettings();
		final SideSettings sideSettings = gameSettings.getSideSettings(onTurn);
		
		if (sideSettings.getSideType() == SideType.COMPUTER) {
			makeComputerMove();
		}
	}
	
	private int evaluateMove (final Position position, final Move move) {
		final SearchResources searchResources = application.getSearchResources();
		final TablebasePositionEvaluator tablebaseEvaluator = searchResources.getTablebasePositionEvaluator();

		position.makeMove (move);
		
		final int absoluteEvaluation = -tablebaseEvaluator.evaluatePosition(position, 0);
		final int relativeEvaluation = Evaluation.getRelative(absoluteEvaluation, position.getOnTurn());
		
		position.undoMove (move);
		
		return relativeEvaluation;
	}
	
	private int getBestMoveInPosition (final Position position, final Move bestMove) {
		final LegalMoveGenerator generator = new LegalMoveGenerator();
		generator.setPosition(position);

		final Holder<Integer> bestEvaluation = new Holder<Integer>();
		
		generator.setWalker(new IMoveWalker() {
			@Override
			public boolean processMove(final Move move) {
				final int evaluation = evaluateMove(position, move);
				
				if (bestEvaluation.getValue() == null || evaluation > bestEvaluation.getValue()) {
					bestEvaluation.setValue(evaluation);
					bestMove.assign(move);
				}
				
				return true;
			}
		});
		
		generator.generateMoves();
		
		if (bestEvaluation.getValue() != null)
			return bestEvaluation.getValue();
		else {
			bestMove.clear();
			return Evaluation.MIN;
		}
	}
	
	private void makeComputerMove() {
		try {
			final GameEditor gameEditor = application.getActualGameEditor();
			final Position position = gameEditor.getActualPositionSource().getPosition().copy();
			
			final Move bestMove = new Move();
			getBestMoveInPosition(position, bestMove);
	
			if (bestMove.getMoveType() != MoveType.INVALID)
				gameEditor.makeMove(bestMove);
		}
		catch (RuntimeException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage());
		}
	}

	private final IGameListener gameListener = new IGameListener() {
		public void onActualPositionChanged() {
			positionChanged();
		}
		
		public void onMove() {
			positionChanged();
		}

		public void onGameChanged() {
			positionChanged();
		}
	};

	/**
	 * Returns type of regime.
	 * @return regime type
	 */
	public RegimeType getRegimeType() {
		return RegimeType.ENDING_TRAINING;
	}

	public boolean validateMove(final Move move) {
		final GameEditor gameEditor = application.getActualGameEditor();
		final Position position = gameEditor.getActualPositionSource().getPosition().copy();
		
		final Move bestMove = new Move();
		final int bestEvaluation = getBestMoveInPosition(position, bestMove);
		final int moveEvaluation = evaluateMove(position, move);
		
		if (moveEvaluation > 0)
			return true;
		
		if (bestEvaluation == Evaluation.DRAW && moveEvaluation == Evaluation.DRAW)
			return true;
		
		if (bestEvaluation < 0)
			return true;
		
		return false;
	}

}
