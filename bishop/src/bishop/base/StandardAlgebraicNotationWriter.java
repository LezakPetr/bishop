package bishop.base;

import java.io.PrintWriter;

public class StandardAlgebraicNotationWriter extends StandardAlgebraicNotationBase implements INotationWriter {
	
	private final Move moveToWrite;
	private boolean moveFound;
	private int anotherMoveCount;
	private boolean onSameFile;
	private boolean onSameRank;


	private final PseudoLegalMoveGenerator generator;
	private final LegalMoveFinder legalMoveFinder;
	private final Position tempPosition;
	
	
	public StandardAlgebraicNotationWriter() {
		moveToWrite = new Move();
		tempPosition = new Position();
		legalMoveFinder = new LegalMoveFinder();
		
		generator = new PseudoLegalMoveGenerator();
		generator.setWalker(this::processMove);
	}

	private boolean processMove(final Move generatedMove) {
		if (generatedMove.getMovingPieceType() == moveToWrite.getMovingPieceType() && generatedMove.getTargetSquare() == moveToWrite.getTargetSquare()) {
			if (generatedMove.equals(moveToWrite))
				moveFound = true;
			else {
				anotherMoveCount ++;

				final int generatedBeginSquare = generatedMove.getBeginSquare();
				final int writtenBeginSquare = moveToWrite.getBeginSquare();

				if (Square.getFile(generatedBeginSquare) == Square.getFile(writtenBeginSquare))
					onSameFile = true;

				if (Square.getRank(generatedBeginSquare) == Square.getRank(writtenBeginSquare))
					onSameRank = true;
			}
		}

		return true;
	}

	/**
	 * Writes move into given writer.
	 * @param writer target writer
	 * @param position begin position of move
	 * @param move move to write
	 */
	public void writeMove (final PrintWriter writer, final Position position, final Move move) {
		moveToWrite.assign(move);
		tempPosition.assign(position);
		
		generator.setPosition(tempPosition);
		
		// Check if there is another move of same piece from same file or rank
		moveFound = false;
		anotherMoveCount = 0;
		onSameFile = false;
		onSameRank = false;
		
		generator.generateMoves();
		
		if (!moveFound) {
			final int compressedMove = move.getCompressedMove();
			final Move uncompressedMove = new Move();
			
			if (uncompressedMove.uncompressMove(compressedMove, position))
				throw new RuntimeException("Invalid move, but the move is uncompressable");
			else
				throw new RuntimeException("Invalid move");
		}
		
		final int moveType = move.getMoveType();
		
		switch (moveType) {
			case MoveType.NORMAL:
			case MoveType.EN_PASSANT:
				writeNormalMove(writer);
				break;
			
			case MoveType.PROMOTION:
				writePromotionMove(writer);
				break;

			case MoveType.CASTLING:
				writeCastlingMove(writer);
				break;
				
			default:
				throw new RuntimeException("Cannot write move with type " + moveType);
		}
		
		// Test check and mate
		tempPosition.makeMove(move);

		if (!legalMoveFinder.existsLegalMove(tempPosition))
			writer.write(MATE_MARK);
		else {
			if (tempPosition.isCheck())
				writer.write(CHECK_MARK);
		}
	}
	
	/**
	 * Writes normal move.
	 * @param writer target writer
	 */
	private void writeNormalMove(final PrintWriter writer) {
		final int movingPieceType = moveToWrite.getMovingPieceType();
		final int beginSquare = moveToWrite.getBeginSquare();
		final boolean isCapture = (moveToWrite.getCapturedPieceType() != PieceType.NONE);
		
		if (movingPieceType == PieceType.PAWN) {
			if (isCapture) {
				final int beginFile = Square.getFile(beginSquare);
				File.write(writer, beginFile);
			}
		}
		else {
			PieceType.write(writer, movingPieceType, true);
			
			if (anotherMoveCount > 0) {
				if (!onSameFile)
					File.write(writer, Square.getFile(beginSquare));
				else {
					if (!onSameRank)
						Rank.write(writer, Square.getRank(beginSquare));
					else {
						File.write(writer, Square.getFile(beginSquare));
						Rank.write(writer, Square.getRank(beginSquare));
					}
				}
			}
		}
		
		if (isCapture)
			writer.write(CAPTURE_MARK);
		
		final int targetSquare = moveToWrite.getTargetSquare();
		Square.write(writer, targetSquare);
	}
	
	/**
	 * Writes promotion move.
	 * @param writer target writer
	 */
	private void writePromotionMove(final PrintWriter writer) {
		writeNormalMove(writer);
		
		final int promotionPieceType = moveToWrite.getPromotionPieceType();
		
		writer.write(PROMOTION_MARK);
		PieceType.write(writer, promotionPieceType, true);
	}
	
	/**
	 * Writes castling move.
	 * @param writer target writer
	 */
	private void writeCastlingMove(final PrintWriter writer) {
		final int targetSquare = moveToWrite.getTargetSquare();
		
		if (Square.getFile(targetSquare) == File.FG)
			writer.write(SHORT_CASTLING_MARK);
		else
			writer.write(LONG_CASTLING_MARK);
	}
}
