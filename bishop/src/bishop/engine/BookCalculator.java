package bishop.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bishop.base.IMoveWalker;
import bishop.base.LegalMoveGenerator;
import bishop.base.Move;
import bishop.base.Position;
import utils.Holder;

public class BookCalculator implements IBook<EvaluatedBookRecord> {

	private final ISearchManager searchManager = new SearchManagerImpl();
	private final Map<Position, EvaluatedBookRecord> recordMap = new HashMap<>();
	private final List<Position> positionOrder = new LinkedList<>();
	
	public BookCalculator() {
		
	}

	@Override
	public EvaluatedBookRecord getRecord(final Position position) {
		return recordMap.get(position);
	}


	@Override
	public Collection<EvaluatedBookRecord> getAllRecords() {
		return recordMap.values();
	}
	
	public void calculate(final IBook<?> book) {
		try {
			fillRecordMap(book);
			calculateOrder();
			evaluateRecords();
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot calculate book");
		}
	}

	private void fillRecordMap(final IBook<?> book) {
		recordMap.clear();
		
		for (BookRecord record: book.getAllRecords()) {
			final EvaluatedBookRecord evaluatedRecord = new EvaluatedBookRecord();
			evaluatedRecord.assign (record);
			
			recordMap.put(evaluatedRecord.getPosition(), evaluatedRecord);
		}
	}

	private void calculateOrder() {
		positionOrder.clear();
		
		final List<Position> openQueue = new LinkedList<>();
		final Set<Position> walkedSet = new HashSet<>();
		
		final Position initialPosition = new Position();
		initialPosition.setInitialPosition();
		
		openQueue.add(initialPosition);
		walkedSet.add(initialPosition);
		
		final LegalMoveGenerator moveGenerator = new LegalMoveGenerator();
		final Position currentPosition = new Position();
		moveGenerator.setPosition(currentPosition);
		
		moveGenerator.setWalker(new IMoveWalker() {
			@Override
			public boolean processMove(final Move move) {
				currentPosition.makeMove(move);
				
				if (recordMap.containsKey(currentPosition) && !walkedSet.contains(currentPosition)) {
					final EvaluatedBookRecord record = recordMap.get(currentPosition);
					walkedSet.add(record.getPosition());
					positionOrder.add(0, record.getPosition());
				}
				
				currentPosition.undoMove(move);
				return true;
			}
		});
		
		while (!openQueue.isEmpty()) {
			currentPosition.assign(openQueue.remove(0));
			moveGenerator.generateMoves();
		}
		
		if (walkedSet.size() != recordMap.size())
			throw new RuntimeException("There are unreachable positions in book"); 
	}
	
	private void evaluateRecords() throws InterruptedException {
		final Holder<Boolean> finished = new Holder<>();
		
		final ISearchManagerHandler handler = new ISearchManagerHandler() {
			@Override
			public void onSearchInfoUpdate(final SearchInfo info) {
			}
			
			@Override
			public void onSearchComplete(ISearchManager manager) {
				synchronized (finished) {
					finished.setValue(Boolean.TRUE);
					finished.notify();
				}
			}
		};
		
		searchManager.start();
		
		for (Position position: positionOrder) {
			final EvaluatedBookRecord record = recordMap.get(position);

			synchronized (finished) {
				finished.setValue(Boolean.FALSE);
			}

			searchManager.getHandlerRegistrar().addHandler(handler);
			searchManager.startSearching(position);
			
			synchronized (finished) {
				while (!finished.getValue())
					finished.wait();
			}
			
			searchManager.stopSearching();
			
			final SearchResult result = searchManager.getResult();
			record.setEvaluation(result.getEvaluation());
			
			final Move bestMove = result.getPrincipalVariation().get(0);
			record.removeAllMoves();
			
			final BookMove bookMove = new BookMove();
			bookMove.setMove(bestMove);
			record.addMove(bookMove);
			
			final HashRecord hashRecord = new HashRecord();
			hashRecord.setHorizon(100);
			hashRecord.setCompressedBestMove(bestMove.getCompressedMove());
			hashRecord.setEvaluation(result.getEvaluation());
			hashRecord.setType(HashRecordType.VALUE);
			
			searchManager.getHashTable().updateRecord(position, hashRecord);
		}
		
		searchManager.stop();
	}

}
