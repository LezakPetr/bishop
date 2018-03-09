package bishop.tablebaseGenerator;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.MaterialHash;
import bishop.base.Piece;
import bishop.base.Position;
import bishop.base.Rank;
import bishop.base.Square;
import bishop.tablebase.FileNameCalculator;
import bishop.tablebase.FilePositionResultSource;
import bishop.tablebase.TableBlockCache;
import bishop.tablebase.TableResult;
import bishop.tablebase.VariationIterator;


public class QueryProcessor {
	private static final int CACHE_BITS = 16;
	
	private static class FixedPiece {
		private final Piece piece;
		private final int square;
		
		public FixedPiece (final Piece piece, final int square) {
			this.piece = piece;
			this.square = square;
		}
	}
	
	private static class SquareStatistics {
		private final long[] winCount = new long[Square.LAST];
		private final long[] drawCount = new long[Square.LAST];
		private final long[] loseCount = new long[Square.LAST];
		
		public void print (final PrintWriter writer) {
			writer.print("       ");
			
			for (int file = bishop.base.File.FIRST; file < bishop.base.File.LAST; file++) {
				writer.print(bishop.base.File.toChar(file));
				writer.print("         ");
			}
			
			writer.println();
			
			for (int rank = Rank.LAST - 1; rank >= Rank.FIRST; rank--) {
				writer.print(Rank.toChar(rank) + " | ");
				
				for (int file = bishop.base.File.FIRST; file < bishop.base.File.LAST; file++) {
					final int square = Square.onFileRank(file, rank);
					
					final long count = winCount[square] + loseCount[square] + drawCount[square];
					
					if (count > 0) {
						final int win = (int) Math.round(100.0 * winCount[square] / count);
						final int lose = (int) Math.round(100.0 * loseCount[square] / count);
						
						writer.format("%3d/%3d | ", win, lose);
					}
					else
						writer.write("        | ");
				}
				
				writer.println();
			}
			
			writer.println();
		}
		
		public void addResult (final int square, final int result) {
			if (TableResult.isWin(result))
				winCount[square]++;
			
			if (TableResult.isLose(result))
				loseCount[square]++;

			if (result == TableResult.DRAW)
				drawCount[square]++;
		}
	}

	private final Position position = new Position();
	private final MaterialHash materialHash = new MaterialHash();
	private final List<FixedPiece> fixedPieceList = new ArrayList<>();
	private final List<Piece> variablePieceList = new ArrayList<>();
	private final List<SquareStatistics> squareStatisticList = new ArrayList<>();
	private int onTurn;
	private String directory;
	private FilePositionResultSource resultSource;

	public void addFixedPiece (final Piece piece, final int square) {
		fixedPieceList.add(new FixedPiece(piece, square));
		materialHash.addPiece(piece.getColor(), piece.getPieceType());
	}
	
	public void addVariablePiece (final Piece piece) {
		variablePieceList.add(piece);
		materialHash.addPiece(piece.getColor(), piece.getPieceType());
	}
	
	public void setOnTurn (final int onTurn) {
		this.onTurn = onTurn;
		materialHash.setOnTurn(onTurn);
	}

	public void setDirectory(final String directory) {
		this.directory = directory;
	}

	public void run() {
		final TableBlockCache blockCache = new TableBlockCache(CACHE_BITS);
		final File file = new File(FileNameCalculator.getAbsolutePath(directory, materialHash));
		resultSource = new FilePositionResultSource(file, blockCache);
		
		position.clearPosition();
		position.setOnTurn(onTurn);
		
		initializeStatistics();
		placeFixedPieces();
		printResults();
		printStatistics();
	}
	
	private void initializeStatistics() {
		squareStatisticList.clear();
		
		for (int i = 0; i < variablePieceList.size(); i++)
			squareStatisticList.add(new SquareStatistics());
	}

	private void placeFixedPieces() {
		for (FixedPiece fixedPiece: fixedPieceList) {
			position.setSquareContent(fixedPiece.square, fixedPiece.piece);
		}
	}
	
	private void printResults() {
		final int variableSize = variablePieceList.size() - 1;
		
		if (variableSize > 0) {
			final int[] first = new int[variableSize];
			Arrays.fill(first, Square.FIRST);
	
			final int[] last = new int[variableSize];
			Arrays.fill(last, Square.LAST);
	
			final VariationIterator it = new VariationIterator(first, last);
			
			do {
				long toClear = BitBoard.EMPTY;
				
				for (int i = 0; i < variableSize; i++) {
					final int square = it.getItemAt(i);
					final long mask = BitBoard.getSquareMask(square);
					
					if ((position.getOccupancy() & mask) == 0) {
						position.setSquareContent(square, variablePieceList.get(i));
						toClear |= mask;
					}
					else
						break;
				}
				
				if (BitBoard.getSquareCount(toClear) == variableSize) {
					final PrintWriter writer = new PrintWriter(System.out);
					
					for (int i = 0; i < variableSize; i++) {
						variablePieceList.get(i).write(writer);
						
						final int square = it.getItemAt(i);
						Square.write(writer, square);
						
						writer.println();
					}
					
					writer.flush();

					position.refreshCachedData();
					printBoard(it);
					
					System.out.println();
				}
				
				for (BitLoop loop = new BitLoop(toClear); loop.hasNextSquare(); ) {
					final int square = loop.getNextSquare();
					position.setSquareContent(square, null);
				}
			} while (it.next());
		}
		else {
			printBoard(null);
		}
	}

	private void printBoard(final VariationIterator it) {
		final Piece piece = variablePieceList.get(variablePieceList.size() - 1);
		
		System.out.print(' ');
		
		for (int file = bishop.base.File.FIRST; file < bishop.base.File.LAST; file++)
			System.out.print(bishop.base.File.toChar(file));
		
		System.out.println();
		
		for (int rank = Rank.LAST - 1; rank >= Rank.FIRST; rank--) {
			System.out.print(Rank.toChar(rank));
			
			for (int file = bishop.base.File.FIRST; file < bishop.base.File.LAST; file++) {
				// Get result
				final int square = Square.onFileRank(file, rank);
				int result = TableResult.ILLEGAL;
				
				if ((position.getOccupancy() & BitBoard.getSquareMask(square)) == 0) {
					position.setSquareContent(square, piece);
					position.refreshCachedData();
					
					result = resultSource.getPositionResult(position);
					position.setSquareContent(square, null);
				}
				
				// Print result
				final char ch = getResultChar (result);
				System.out.print(ch);
				
				// Update statistics
				if (it != null) {
					for (int i = 0; i < it.getItemCount(); i++)
						squareStatisticList.get(i).addResult(it.getItemAt(i), result);
				}
				
				squareStatisticList.get(it.getItemCount()).addResult(square, result);
			}
			
			System.out.println();
		}
	}

	private char getResultChar(final int result) {
		if (TableResult.isWin(result))
			return '+';
		
		if (TableResult.isLose(result))
			return '-';

		if (result == TableResult.ILLEGAL)
			return ' ';

		if (result == TableResult.DRAW)
			return '=';

		throw new RuntimeException("Unknown result " + result);
	}
	
	private void printStatistics() {
		final PrintWriter writer = new PrintWriter(System.out);
		
		for (int i = 0; i < variablePieceList.size(); i++) {
			writer.write("Statistics of ");
			variablePieceList.get(i).write(writer);
			writer.println();
			
			squareStatisticList.get(i).print(writer);
		}
		
		writer.flush();
	}

}
