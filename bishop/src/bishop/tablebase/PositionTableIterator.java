package bishop.tablebase;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Square;

public class PositionTableIterator implements ITableIterator {
	
	private final ITable table;
	private final TableDefinition tableDefinition;
	private final VariationIterator iterator;
	private final Position position;
	private final int epFile;
	
	// Square boundaries; shared by more iterator instances - arrays cannot be modified 
	private int[] firstSquares;
	private int[] lastSquares;
	
	private boolean valid;
	private long tableIndex;
	
	
	public PositionTableIterator(final ITable table, final int epFile) {
		this.table = table;
		this.tableDefinition = table.getDefinition();
		this.epFile = epFile;
		this.position = new Position();
		this.valid = true;
		
		fillSquareBoundaries();
		
		iterator = new VariationIterator(firstSquares, lastSquares);
		updateCachedData();
	}
	
	private void fillSquareBoundaries() {
		final int pieceCount = tableDefinition.getPieceCount();
		final int onTurn = tableDefinition.getOnTurn();
		
		firstSquares = new int[pieceCount];
		lastSquares = new int[pieceCount];
		
		int pieceIndex = 0;
		
		// Kings
		for (int i = 0; i < 2; i++) {
			firstSquares[pieceIndex] = Square.FIRST;
			lastSquares[pieceIndex] = Square.LAST;
			pieceIndex++;
		}
		
		// Pieces
		for (int combinationIndex = 0; combinationIndex < tableDefinition.getCombinationDefinitionCount(); combinationIndex++) {
			final CombinationDefinition combinationDefinition = tableDefinition.getCombinationDefinitionAt(combinationIndex);
			final Piece piece = combinationDefinition.getPiece();
			
			if (piece.getPieceType() == PieceType.PAWN) {
				int i = 0;
				
				if (epFile != File.NONE) {
					final int notOnTurn = Color.getOppositeColor(onTurn);
					final int epSquare = BoardConstants.getEpSquare(notOnTurn, epFile);
					
					if (piece.getColor() == onTurn) {
						final int epRank = BoardConstants.getEpRank(notOnTurn);
						
						firstSquares[pieceIndex] = Math.max(epSquare - 1, Square.onFileRank(File.FA, epRank));
						lastSquares[pieceIndex] = Math.min(epSquare + 1, Square.onFileRank(File.FH, epRank)) + 1;
					}
					else {
						firstSquares[pieceIndex] = epSquare;
						lastSquares[pieceIndex] = epSquare + 1;
					}
					
					pieceIndex++;
					i++;
				}
				
				while (i < combinationDefinition.getCount()) {
					firstSquares[pieceIndex] = Square.FIRST_PAWN_SQUARE;
					lastSquares[pieceIndex] = Square.LAST_PAWN_SQUARE;
					
					pieceIndex++;
					i++;
				}
			}
			else {
				for (int i = 0; i < combinationDefinition.getCount(); i++) {
					firstSquares[pieceIndex] = Square.FIRST;
					lastSquares[pieceIndex] = Square.LAST;
					pieceIndex++;
				}
			}
		}
	}

	public PositionTableIterator(final PositionTableIterator orig) {
		this.table = orig.table;
		this.tableDefinition = orig.tableDefinition;
		this.epFile = orig.epFile;
		this.position = orig.position.copy();
		this.valid = orig.valid;
		this.tableIndex = orig.tableIndex;
		this.firstSquares = orig.firstSquares;
		this.lastSquares = orig.lastSquares;
		
		this.iterator = orig.iterator.copy();
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void next() {
		moveForward (1);
	}

	@Override
	public void moveForward(final long count) {
		for (long i = 0; i < count; i++) {
			valid = iterator.next();
		}
		
		updateCachedData();
	}

	@Override
	public ITableIterator copy() {
		return new PositionTableIterator(this);
	}
	
	@Override
	public void fillPosition(final Position position) {
		position.assign(this.position);
	}

	@Override
	public int getResult() {
		return table.getResult (tableIndex);
	}

	@Override
	public void setResult(final int result) {
		throw new RuntimeException("PositionTableIterator.setResult not implemented"); 
	}

	@Override
	public long getTableIndex() {
		return tableIndex;
	}
	
	private void updateCachedData() {
		if (isValid()) {
			position.clearPosition();
			position.setOnTurn(tableDefinition.getOnTurn());
			position.setEpFile(epFile);
			
			Utils.setSquareArrayToPosition (position, iterator, tableDefinition);
			position.refreshCachedData();
			
			final int pieceCount = BitBoard.getSquareCount(position.getOccupancy());
			
			if (pieceCount == tableDefinition.getPieceCount())
				tableIndex = tableDefinition.calculateTableIndex(position);
			else
				tableIndex = -1;
		}
		else {
			tableIndex = -1;
		}
	}

	@Override
	public int getChunkIndex() {
		return tableDefinition.getChunkAtTableIndex(tableIndex);
	}

	@Override
	public long getPositionCount() {
		long count = 1;
		
		for (int i = 0; i < firstSquares.length; i++) {
			count *= (lastSquares[i] - firstSquares[i]);
		}
		
		return count;
	}

}
