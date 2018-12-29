package bishop.engine;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import bishop.base.*;
import utils.Holder;
import utils.Logger;
import parallel.Parallel;

public final class SearchManagerImpl implements ISearchManager {
	
	// Settings
	private ISearchEngineFactory engineFactory;
	private int maxHorizon;
	private long maxTimeForMove;
	private HandlerRegistrarImpl<ISearchManagerHandler> handlerRegistrar;
	private int minHorizon = 3 * SerialSearchEngine.HORIZON_STEP_WITHOUT_EXTENSION;
	private int threadCount = 1;
	private CombinedPositionEvaluationTable combinedPositionEvaluationTable = CombinedPositionEvaluationTable.ZERO_TABLE;
	private PieceTypeEvaluations pieceTypeEvaluations;
	private long searchInfoTimeout = 500; // ms
	
	private final List<ISearchEngine> searchEngineList = new ArrayList<>();
	
	// Data for the search
	private SearchSettings searchSettings;
	private long searchStartTime;
	private long totalNodeCount;
	private int startHorizon;
	private boolean bookSearchEnabled;
	private boolean singleSearchEnabled;
	private IBook<?> book;
	private IHashTable hashTable;
	private TablebasePositionEvaluator tablebaseEvaluator;
	
	// Synchronization
	private Thread searchingThread;
	private Thread checkingThread;
	private ManagerState managerState;
	private final Object monitor;
	private Parallel parallel;
	
	private final Position rootPosition;
	private int horizon;
	private SearchResult searchResult;
	private boolean searchFinished;
	private boolean isResultSent;
	private boolean isSearchRunning;
	private boolean searchInfoChanged;
	private long lastSearchInfoTime;
	private final List<String> additionalInfo = new ArrayList<>();
	private Random random = new Random();
	
	private ISearchEngineHandler engineHandler = new ISearchEngineHandler() {
		@Override
		public void onResultUpdate(final SearchResult result) {
			synchronized (monitor) {
				searchResult = result;
				updateNodeCountInResult();
				
				searchInfoChanged = true;
			}
		}
	};
	
	
	/**
	 * Default constructor.
	 */
	public SearchManagerImpl() {
		this.managerState = ManagerState.STOPPED;
		this.monitor = new Object();
		this.handlerRegistrar = new HandlerRegistrarImpl<ISearchManagerHandler>();
		
		this.setMaxHorizon(256);
		this.maxTimeForMove = TIME_FOR_MOVE_INFINITY;
		this.rootPosition = new Position();
		
		this.searchSettings = new SearchSettings();
	}
	
	private void updateNodeCountInResult() {
		long nodeCount = 0;
		
		for (ISearchEngine engine: searchEngineList) {
			nodeCount += engine.getNodeCount();
		}
		
		this.searchResult.setNodeCount(nodeCount);
	}
	
	/**
	 * Sets search engine factory used to create engines controlled by this manager.
	 * Manager must be in STOPPED state.
	 * @param factory search engine factory
	 */
	public void setEngineFactory (final ISearchEngineFactory factory) {
		synchronized (monitor) {
			checkManagerState(ManagerState.STOPPED);
			this.engineFactory = factory;
		}
	}

	@Override
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table) {
		synchronized (monitor) {
			checkManagerState(ManagerState.STOPPED);
			this.combinedPositionEvaluationTable = table;
		}
	}
	
	/**
	 * Sets book.
	 * Manager must be in STOPPED state.
	 * @param book new book
	 */
	@Override
	public void setBook (final IBook<?> book) {
		synchronized (monitor) {
			checkManagerState(ManagerState.STOPPED);
			this.book = book;
		}
	}

	/**
	 * Checks if search should finish and if so sets searchFinished flag.
	 */
	public void updateSearchFinished() {
		if (horizon > maxHorizon)
			searchFinished = true;
		
		if (horizon > startHorizon && (System.currentTimeMillis() - searchStartTime) > maxTimeForMove) {
			searchFinished = true;
			Logger.logMessage("Updating " + horizon + ", " + startHorizon);
		}
	}
	
	private void sendResult() {
		if (!isResultSent) {
			updateSearchInfo();
			
			for (ISearchManagerHandler handler: handlerRegistrar.getHandlers()) {
				handler.onSearchComplete(SearchManagerImpl.this);
			}
			
			isResultSent = true;
		}
	}

	/**
	 * Sends updated search info to registered handlers.
	 */
	private void updateSearchInfo() {
		if (searchResult != null && searchInfoChanged) {
			final SearchInfo info = new SearchInfo();
			
			info.setPosition (rootPosition);
			info.setElapsedTime(System.currentTimeMillis() - searchStartTime);
			info.setHorizon(searchResult.getHorizon());
			info.setNodeCount(totalNodeCount + searchResult.getNodeCount());
			info.setPrincipalVariation(searchResult.getPrincipalVariation());
			info.setEvaluation(searchResult.getNodeEvaluation().getEvaluation());
			info.getAdditionalInfo().addAll(additionalInfo);
			
			for (ISearchManagerHandler handler: handlerRegistrar.getHandlers())
				handler.onSearchInfoUpdate(info);
		}
		
		searchInfoChanged = false;
		lastSearchInfoTime = System.currentTimeMillis();
	}

	/**
	 * Checks if manager is in one of given expected states.
	 * If not exception is thrown.
	 * Expects that calling thread owns the monitor.
	 * @param expectedStates expected manager states
	 */
	private void checkManagerState (final ManagerState... expectedStates) {
		for (ManagerState state: expectedStates) {
			if (state == managerState)
				return;
		}
		
		throw new RuntimeException("Engine is not in expected state, but in state " + managerState.name());
	}
	
	/**
	 * Starts the manager.
	 * Changes state from STOPPED to WAITING.
	 */
	public void start() {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED);
			
			if (engineFactory == null)
				throw new RuntimeException("Engine factory is not set");

			handlerRegistrar.setChangesEnabled(false);

			parallel = new Parallel(threadCount);
			createSearchEngines();
			createSearchingThread();
			createCheckingThread();

			managerState = ManagerState.WAITING;
			monitor.notifyAll();
		}
	}

	private void createSearchEngines() {
		searchEngineList.clear();
		
		for (int i = 0; i < threadCount; i++) {
			final ISearchEngine engine = engineFactory.createEngine();
			engine.setSearchSettings(searchSettings);
			engine.getHandlerRegistrar().addHandler(engineHandler);
			engine.setTablebaseEvaluator(tablebaseEvaluator);
			engine.setHashTable(hashTable);
			engine.setCombinedPositionEvaluationTable(combinedPositionEvaluationTable);
			
			searchEngineList.add(engine);
		}
	}

	private void createCheckingThread() {
		checkingThread = new Thread(new Runnable() {
			public void run() {
				try {
					doChecking();
				}
				catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		});
		
		checkingThread.setName("SearchManagerImpl checkingThread");
		checkingThread.setDaemon(true);
		checkingThread.start();
	}

	private void createSearchingThread() {
		searchingThread = new Thread(new Runnable() {
			public void run() {
				try {
					doSearching();
				}
				catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		});
			
		searchingThread.setName("SearchManagerImpl searchingThread");
		searchingThread.setDaemon(true);
		searchingThread.start();
	}
	
	private void doSearching() throws InterruptedException, ExecutionException {
		while (true) {
			boolean shouldSearch = false;
			
			synchronized (monitor) {
				switch (managerState) {
					case STOPPING:
					case STOPPED:
						return;
						
					case WAITING:
						monitor.wait();
						break;
						
					case TERMINATING:
						isSearchRunning = false;
						monitor.notifyAll();
						break;
						
					case SEARCHING:
						shouldSearch = true;
						searchFinished = false;
						break;
				}
			}
			
			if (shouldSearch) {
				search();
				
				synchronized (monitor) {
					sendResult();
					
					while (managerState == ManagerState.SEARCHING)
						monitor.wait();
				}
			}
		}
	}
	
	private void doChecking() throws InterruptedException {
		while (true) {
			synchronized (monitor) {
				if (managerState == ManagerState.STOPPING)
					break;
				
				if (managerState == ManagerState.SEARCHING && isSearchRunning)
				{
					final long timeout = lastSearchInfoTime + searchInfoTimeout - System.currentTimeMillis();
					
					if (timeout > 0)
						monitor.wait(timeout);
					
					if (System.currentTimeMillis() > lastSearchInfoTime + searchInfoTimeout) {
						updateSearchInfo();
					}
					
					updateSearchFinished();
					
					if (searchFinished) {
						for (ISearchEngine engine: searchEngineList)
							engine.stopSearching();
					}
				}
				else
					monitor.wait();
			}
		}		
	}
	
	private void search() throws InterruptedException, ExecutionException {
		final Move initialMove = initializeSearch();
		
		if (initialMove != null) {
			searchResult = new SearchResult();
			searchResult.getPrincipalVariation().add(initialMove);
			
			this.searchInfoChanged = true;
			
			return;
		}
		
		boolean initialSearch = true;
		
		horizon = startHorizon;
		
		EvaluatedMoveList previousEvaluatedMoveList = null;
		
		while (true) {
			final SearchTask task = new SearchTask();
			task.getPosition().assign(rootPosition);
			task.setHorizon(horizon);
			task.setInitialSearch(initialSearch);
			
			final int materialEvaluation = rootPosition.getMaterialEvaluation();
			task.setRootMaterialEvaluation(materialEvaluation);
			
			final RepeatedPositionRegister repeatedPositionRegister = new RepeatedPositionRegister();
			repeatedPositionRegister.clearAnsReserve(1);
			repeatedPositionRegister.pushPosition(rootPosition, null);
			
			task.setRepeatedPositionRegister(repeatedPositionRegister);
			
			if (previousEvaluatedMoveList != null) {
				final EvaluatedMoveList taskMoveList = task.getRootMoveList();
				taskMoveList.assign(previousEvaluatedMoveList);
				taskMoveList.sortMoves(0, taskMoveList.getSize());
			}
			
			final List<Callable<SearchResult>> callableList = new ArrayList<>(threadCount);
			
			for (int i = 0; i < threadCount; i++) {
				final ISearchEngine engine = searchEngineList.get(i);
				
				callableList.add(() -> engine.search(task));
			}
			
			final List<Future<SearchResult>> futureList = parallel.getExecutor().invokeAll(callableList);
			SearchResult bestResult = null;
			
			for (Future<SearchResult> future: futureList) {
				final SearchResult result = future.get();
				
				if (bestResult == null || result.getNodeEvaluation().getEvaluation() > bestResult.getNodeEvaluation().getEvaluation())
					bestResult = result;
			}
			
			previousEvaluatedMoveList = bestResult.getRootMoveList();
			
			synchronized (monitor) {
				updateSearchFinished();
				
				if (searchFinished || managerState != ManagerState.SEARCHING)
					return;
				
				horizon += SerialSearchEngine.HORIZON_STEP_WITHOUT_EXTENSION;
				initialSearch = false;
				
				this.searchResult = bestResult;
				updateNodeCountInResult();
				this.searchInfoChanged = true;
				
				totalNodeCount += searchResult.getNodeCount();
				this.searchResult.setNodeCount(0);
			}
		}
	}
	
	private Move initializeSearch() {
		this.horizon = startHorizon;
		
		this.searchResult = null;
		additionalInfo.clear();
		
		if (singleSearchEnabled) {
			final Move singleMove = singleMoveSearch();
			
			if (singleMove != null)
				return singleMove;
		}
		
		final Move bookMove = bookSearch();
		
		if (bookMove != null && bookSearchEnabled)
			return bookMove;

		hashTable.clear();

		for (ISearchEngine engine: searchEngineList)
			engine.clear();
		
		return null;
	}

	private Move bookSearch() {
		if (book == null)
			return null;
		
		final BookRecord record = book.getRecord(rootPosition);
		
		if (record == null)
			return null;
		
		record.logRecord(additionalInfo);
		searchInfoChanged = true;
		
		final BookMove bookMove = record.getRandomMove(random);
		
		if (bookMove == null)
			return null;
		
		return bookMove.getMove();
	}

	private Move singleMoveSearch() {
		final LegalMoveGenerator generator = new LegalMoveGenerator();
		generator.setPosition(rootPosition);
		
		final Move lastMove = new Move();
		final Holder<Integer> moveCount = new Holder<>(0);
		
		generator.setWalker(new IMoveWalker() {
			@Override
			public boolean processMove(final Move move) {
				lastMove.assign(move);
				
				final int newCount = moveCount.getValue() + 1;
				moveCount.setValue(newCount);
				
				return newCount > 1;
			}
		});
		
		if ((int) moveCount.getValue() == 1)
			return lastMove;
		else
			return null;
	}

	/**
	 * Stops the manager.
	 * Changes state from WAITING or SEARCHING to STOPPING and later to STOPPED.
	 */
	public void stop() {
		synchronized (monitor) {
			checkManagerState (ManagerState.WAITING, ManagerState.SEARCHING);
			managerState = ManagerState.STOPPING;
			monitor.notifyAll();
		}

		Utils.joinThread(checkingThread);
		Utils.joinThread(searchingThread);
		parallel.shutdown();
		
		searchEngineList.clear();
		
		synchronized (monitor) {
			handlerRegistrar.setChangesEnabled(true);
			
			searchingThread = null;
			checkingThread = null;
			
			managerState = ManagerState.STOPPED;
		}
	}

	/**
	 * Sets maximal horizon of the search.
	 * Manager must be in STOPPED or WAITING state.
	 * @param maxHorizon maximal horizon
	 */
	public void setMaxHorizon (final int maxHorizon) {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED, ManagerState.WAITING);
			this.maxHorizon = maxHorizon;
		}
	}

	/**
	 * Sets timeout for updating search info.
	 * Manager must be in STOPPED or WAITING state.
	 * @param timeout timeout [ms]
	 */
	@Override
	public void setSearchInfoTimeout (final long timeout) {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED, ManagerState.WAITING);
			this.searchInfoTimeout = timeout;
		}
	}
	
	/**
	 * Sets maximal time for move.
	 * @param time maximal time for search of one move
	 */
	public void setMaxTimeForMove (final long time) {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED, ManagerState.WAITING);
			this.maxTimeForMove = time;
		}
	}
	
	/**
	 * Starts searching of given position.
	 * Changes state from WAITING to SEARCHING.
	 * @param position position to search
	 */
	public void startSearching (final Position position) {
		synchronized (monitor) {
			checkManagerState (ManagerState.WAITING);
			
			Logger.logMessage("Starting search");
			
			this.startHorizon = Math.min(minHorizon, maxHorizon);
			this.searchFinished = false;
			this.isResultSent = false;
			this.searchInfoChanged = false;
			this.lastSearchInfoTime = 0;
			this.isSearchRunning = true;
			this.rootPosition.assign(position);
			this.rootPosition.refreshCachedData();
			
			searchStartTime = System.currentTimeMillis();
			totalNodeCount = 0;
			managerState = ManagerState.SEARCHING;
			
			hashTable.clear();
			
			monitor.notifyAll();
		}
	}
	
	/**
	 * Stops searching.
	 * Changes state from SEARCHING to TERMINATING and later to WAITING.
	 */
	public void stopSearching() {
		Logger.logMessage("SearchManager.stopSearching");
		
		synchronized (monitor) {
			checkManagerState (ManagerState.SEARCHING);
			
			managerState = ManagerState.TERMINATING;
			monitor.notifyAll();
			
			for (ISearchEngine engine: searchEngineList)
				engine.stopSearching();
					
			while (isSearchRunning) {
				try {
					monitor.wait();					
				}
				catch (InterruptedException ex) {
				}
			}
			
			managerState = ManagerState.WAITING;
		}
		
		Logger.logMessage("Searching stopped");
	}
	
	/**
	 * Returns handler registrar.
	 * Modification of registrar is allowed just in STOPPED state.
	 * @return registrar
	 */
	public IHandlerRegistrar<ISearchManagerHandler> getHandlerRegistrar() {
		return handlerRegistrar;
	}

	/**
	 * Returns result of the search.
	 * Manager must be in WAITING state.
	 * @return result of the search
	 */
	public SearchResult getResult() {
		synchronized (monitor) {
			checkManagerState (ManagerState.WAITING);
			
			return searchResult;
		}
	}
	
	/**
	 * Returns current state of the manager.
	 * @return manager state
	 */
	public ManagerState getManagerState() {
		synchronized (monitor) {
			return managerState;
		}		
	}
		
	/**
	 * Returns hash table.
	 * @returns hash table
	 */
	public IHashTable getHashTable() {
		synchronized (monitor) {
			return hashTable;
		}
	}

	/**
	 * Sets hash table for the manager.
	 * Manager must be in STOPPED state.
	 * @param table hash table
	 */
	public void setHashTable (final IHashTable table) {
		if (table == null)
			throw new RuntimeException("Hash table cannot be null");
		
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED);

			this.hashTable = table;
		}
	}


	/**
	 * Sets piece type evaluations.
	 * Manager must be in STOPPED state.
	 */
	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations) {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED);

			this.pieceTypeEvaluations = pieceTypeEvaluations;
			rootPosition.setPieceTypeEvaluations(pieceTypeEvaluations);
		}
	}
	
	/**
	 * Enables or disables book.
	 * Manager must be in STOPPED or WAITING state.
	 * @param enabled if book search is enabled
	 */
	public void setBookSearchEnabled(final boolean enabled) {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED, ManagerState.WAITING);

			this.bookSearchEnabled = enabled;
		}
	}
	
	/**
	 * Enables or disables single move search.
	 * Manager must be in STOPPED or WAITING state.
	 * @param enabled if single move search is enabled
	 */
	public void setSingleMoveSearchEnabled(final boolean enabled) {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED, ManagerState.WAITING);

			this.singleSearchEnabled = enabled;
		}
	}
	
	/**
	 * Gets search settings.
	 * @return search settings
	 */
	public SearchSettings getSearchSettings() {
		synchronized (monitor) {
			return searchSettings;
		}
	}

	/**
	 * Sets search settings.
	 * Manager must be in STOPPED state.
	 * @param searchSettings search settings
	 */
	public void setSearchSettings(final SearchSettings searchSettings) {
		synchronized (monitor) {
			checkManagerState(ManagerState.STOPPED);
			
			this.searchSettings = searchSettings;
		}
	}

	/**
	 * Gets thread count.
	 * @return thread count
	 */
	public int getThreadCount() {
		synchronized (monitor) {
			return threadCount;
		}
	}

	/**
	 * Sets thread counts.
	 * Manager must be in STOPPED state.
	 * @param threadCount thread count
	 */
	public void setThreadCount(final int threadCount) {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED, ManagerState.WAITING);
			this.threadCount = threadCount;
		}
	}

	/**
	 * Sets tablebase position evaluator.
	 * Manager must be in STOPPED state.
	 * @param tablebaseEvaluator evaluator
	 */
	public void setTablebaseEvaluator(final TablebasePositionEvaluator tablebaseEvaluator) {
		synchronized (monitor) {
			checkManagerState(ManagerState.STOPPED);
			
			this.tablebaseEvaluator = tablebaseEvaluator;
		}
	}
	
	/**
	 * Check if calculation is finished.
	 * @return true if searchFinished flag is set
	 */
	public boolean isSearchFinished() {
		synchronized (monitor) {
			return searchFinished;
		}
	}
	
}
