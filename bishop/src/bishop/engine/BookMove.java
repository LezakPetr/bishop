package bishop.engine;

import bishop.base.Annotation;
import bishop.base.Move;

public final class BookMove {

	private Move move;
	private Annotation annotation;
	
	public BookMove() {
		move = null;
		setAnnotation(Annotation.NONE);
	}

	public Move getMove() {
		return move;
	}

	public void setMove(final Move move) {
		this.move = move;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(final Annotation annotation) {
		this.annotation = annotation;
	}

	
}
