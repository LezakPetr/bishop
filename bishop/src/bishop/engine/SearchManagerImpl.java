package bishop.engine;


import utils.Logger;
import bishop.base.HandlerRegistrarImpl;
import bishop.base.IHandlerRegistrar;
import bishop.base.Move;
import bishop.base.MoveList;
import bishop.base.Position;

public final class SearchManagerImpl implements ISearchManager {
	
	private final static long SEARCH_INFO_TIMEOUT = 500;   // [ms] 
	
	// Settings
	private ISearchEngineFactory engineFactory;
	private int maxHorizon;
	private long maxTimeForMove;
	private HandlerRegistrarImpl<ISearchManagerHandler> handlerRegistrar;
	private int minHorizon = 3 * ISearchEngine.HORIZON_GRANULARITY;
	
	private ISearchEngine searchEngine;
	
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
	
	private final Position rootPosition;
	private int horizon;
	private SearchResult searchResult;
	private boolean searchFinished;
	private boolean isResultSent;
	private boolean isSearchRunning;
	private boolean searchInfoChanged;
	private long lastSearchInfoTime;
	
	private ISearchEngineHandler engineHandler = new ISearchEngineHandler() {
		@Override
		public void onResultUpdate(final SearchResult result) {
			synchronized (monitor) {
				searchResult = result;
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
		
		this.setMaxHorizon(256 * ISearchEngine.HORIZON_GRANULARITY);
		this.maxTimeForMove = TIME_FOR_MOVE_INFINITY;
		this.rootPosition = new Position();
		
		this.searchSettings = new SearchSettings();
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
			
			info.setElapsedTime(System.currentTimeMillis() - searchStartTime);
			info.setHorizon(searchResult.getHorizon());
			info.setNodeCount(totalNodeCount + searchResult.getNodeCount());
			info.setPrincipalVariation(searchResult.getPrincipalVariation());
			info.setEvaluation(searchResult.getNodeEvaluation().getEvaluation());
			
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
	 * @param expectedState expected manager state
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

			createSearchEngine();
			createSearchingThread();
			createCheckingThread();

			managerState = ManagerState.WAITING;
			monitor.notifyAll();
		}
	}

	private void createSearchEngine() {
		this.searchEngine = engineFactory.createEngine();
		this.searchEngine.setSearchSettings(searchSettings);
		this.searchEngine.getHandlerRegistrar().addHandler(engineHandler);
		this.searchEngine.setTablebaseEvaluator(tablebaseEvaluator);
		this.searchEngine.setHashTable(hashTable);
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
	
	private void doSearching() throws InterruptedException {
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
				System.out.println("SEARCH FINISHED");
				
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
					final long timeout = lastSearchInfoTime + SEARCH_INFO_TIMEOUT - System.currentTimeMillis();
					
					if (timeout > 0)
						monitor.wait(timeout);
					
					if (System.currentTimeMillis() > lastSearchInfoTime + SEARCH_INFO_TIMEOUT) {
						updateSearchInfo();
					}
					
					updateSearchFinished();
					
					if (searchFinished)
						searchEngine.stopSearching();
				}
				else
					monitor.wait();
			}
		}		
	}
	
	private void search() {
		boolean initialSearch = true;
		
		horizon = startHorizon;
		
		EvaluatedMoveList previousEvaluatedMoveList = null;
		
		while (true) {
			final SearchTask task = new SearchTask();
			task.getPosition().assign(rootPosition);
			task.setHorizon(horizon);
			task.setInitialSearch(initialSearch);
			task.setDepthAdvance(0);
			task.setRootMaterialEvaluation(rootPosition.getMaterialEvaluation());
			task.setRepeatedPositionRegister(new RepeatedPositionRegister());
			
			if (previousEvaluatedMoveList != null) {
				final EvaluatedMoveList taskMoveList = task.getRootMoveList();
				taskMoveList.assign(previousEvaluatedMoveList);
				taskMoveList.sortMoves(0, taskMoveList.getSize());
			}
			
			final SearchResult result = searchEngine.search(task);
			previousEvaluatedMoveList = result.getRootMoveList();
			
			synchronized (monitor) {
				updateSearchFinished();
				
				if (searchFinished || managerState != ManagerState.SEARCHING)
					return;
				
				horizon += ISearchEngine.HORIZON_GRANULARITY;
				initialSearch = false;
				
				if (!result.isSearchTerminated()) {   // TODO
					this.searchResult = result;
					this.searchInfoChanged = true;
				}
				
				totalNodeCount += searchResult.getNodeCount();
				this.searchResult.setNodeCount(0);
			}
		}
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
	 * Sets maximal time for move.
	 * @param time maximal time for search of one move
	 */
	public void setMaxTimeForMove (final long time) {
		synchronized (monitor) {
			checkManagerState (ManagerState.STOPPED, ManagerState.WAITING);
			this.maxTimeForMove = time;
		}
	}
	/*
	public Move initializeSearch (final Position position, final int startHorizon) {
		this.initialPosition.assign(position);
		this.horizon = startHorizon - ISearchEngine.HORIZON_GRANULARITY;
		
		this.searchResult.clear();
		
		rootNode = new SearchNode(position, null, null);
		rootNode.setMaxExtension(searchSettings.getMaxExtension());
		
		rootMaterialEvaluation = position.getMaterialEvaluation();
		
		if (singleMoveSearchEnabled) {
			final Move singleMove = singleMoveSearch();
			
			if (singleMove != null) {
				setBeforeSearchResult (singleMove);
				return singleMove;
			}
		}
		
		if (bookSearchEnabled) {
			final Move bookMove = bookSearch();
			
			if (bookMove != null) {
				setBeforeSearchResult (bookMove);
				return bookMove;
			}
		}
				
		return null;
	}*/
	
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
			
			searchStartTime = System.currentTimeMillis();
			totalNodeCount = 0;
			managerState = ManagerState.SEARCHING;
			
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
			
			if (searchEngine != null)
				searchEngine.stopSearching();
		
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
	
	private void sendMoveBeforeSearch (final Move move) {
		final MoveList principalVariation = searchResult.getPrincipalVariation();
		principalVariation.clear();
		principalVariation.add(move);
		
		searchFinished = true;
		monitor.notify();
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
