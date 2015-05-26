package bishop.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import utils.Logger;
import bishop.base.GlobalSettings;
import bishop.base.HandlerRegistrarImpl;
import bishop.base.IHandlerRegistrar;
import bishop.base.Move;
import bishop.base.MoveList;
import bishop.base.Position;
import bishop.engine.ISearchEngine.EngineState;
import bishop.engine.SearchNode.EvaluationState;
import bishop.engine.SearchNode.TreeState;


public final class SearchManagerImpl implements ISearchManager, ISearchStrategyHandler, ISearchManagerAlgoHandler {
	
	static class EngineRecord {
		public ISearchEngine engine;
		public SearchNode node;
		public int index;
		public int taskAlpha;
		public int taskBeta;
		public boolean isSmallerWindow;
	}
	
	static class ResultRecord {
		public EngineRecord engineRecord;
		public SearchResult result;
	}
	
	
	private final static long SEARCH_INFO_TIMEOUT = 500;   // [ms] 
	
	// Settings
	private int threadCount;
	private ISearchEngineFactory engineFactory;
	private int maxHorizon;
	private long maxTimeForMove;
	private HandlerRegistrarImpl<ISearchManagerHandler> handlerRegistrar;
	private int minHorizon = 3 * ISearchEngine.HORIZON_GRANULARITY;
	
	// Data for the search
	private long searchStartTime;
	private long totalNodeCount;
	private int startHorizon;
	private final SearchManagerAlgo algo;
	
	// Synchronization
	private final List<EngineRecord> waitingEngineList;
	private final Map<ISearchEngine, EngineRecord> searchingEngineMap;
	
	private Thread managerThread;
	private final Queue<ResultRecord> resultRecordQueue;
	private ManagerState managerState;
	private final Object monitor;
	
	private boolean searchFinished;
	private boolean isResultSent;
	private boolean areEnginesTrimmed;
	private boolean isSearchRunning;
	private boolean searchInfoChanged;
	private long lastSearchInfoTime;
	
	/**
	 * Handler that processes results from search engines.
	 */
	private final ISearchEngineHandler engineHandler = new ISearchEngineHandler() {
		/**
		 * Method creates result record and pushes it into resultRecordQueue.
		 */
		public void onSearchComplete (final ISearchEngine engine, final SearchTask task, final SearchResult result) {
			synchronized (monitor) {
				final EngineRecord engineRecord = searchingEngineMap.get(engine);
				
				final ResultRecord resultRecord = new ResultRecord();
				resultRecord.engineRecord = engineRecord;
				resultRecord.result = result;
				
				resultRecordQueue.add(resultRecord);
				monitor.notifyAll();
				
				Logger.logMessage("Results from engine " + engineRecord.index);
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
		this.threadCount = 1;
		
		this.algo = new SearchManagerAlgo(this);
		this.setMaxHorizon(256 * ISearchEngine.HORIZON_GRANULARITY);
		this.maxTimeForMove = TIME_FOR_MOVE_INFINITY;
		
		this.waitingEngineList = new ArrayList<EngineRecord>();
		this.searchingEngineMap = new HashMap<ISearchEngine, EngineRecord>();
		this.resultRecordQueue = new LinkedList<ResultRecord>();
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
	 * Sets number of calculation threads.
	 * Manager must be in STOPPED state.
	 * @param threadCount number of threads
	 */
	public void setThreadCount (final int threadCount) {
		synchronized (monitor) {
			checkManagerState(ManagerState.STOPPED);
			this.threadCount = threadCount;
		}
	}
	
	/**
	 * Sets strategy of the search.
	 * Manager must be in STOPPED state.
	 * @param strategy search strategy
	 */
	public void setSearchStrategy (final ISearchStrategy strategy) {
		synchronized (monitor) {
			checkManagerState(ManagerState.STOPPED);
			algo.setStrategy(strategy);
		}
	}
	
	/**
	 * Sets book.
	 * Manager must be in STOPPED state.
	 * @param book new book
	 */
	public void setBook (final IBook book) {
		synchronized (monitor) {
			checkManagerState(ManagerState.STOPPED);
			algo.setBook (book);
		}
	}

	/**
	 * Checks if search should finish and if so sets searchFinished flag.
	 */
	@Override
	public void updateSearchFinished() {
		final int horizon = algo.getHorizon();
		
		if (horizon > maxHorizon)
			searchFinished = true;
		
		if (horizon > startHorizon && (System.currentTimeMillis() - searchStartTime) > maxTimeForMove) {
			searchFinished = true;
			Logger.logMessage("Updating " + horizon + ", " + startHorizon);
		}
	}
	
	/**
	 * Method checks content of resultRecordQueue and processes records from it.
	 */
	private void processResults() {
		synchronized (monitor) {
			while (managerState == ManagerState.SEARCHING || managerState == ManagerState.WAITING || managerState == ManagerState.TERMINATING) {
				try {
					final ResultRecord resultRecord = resultRecordQueue.poll();
					
					if (resultRecord != null) {
						stopEngine(resultRecord.engineRecord);
						
						totalNodeCount += resultRecord.result.getNodeCount();
						
						if (!searchFinished && managerState == ManagerState.SEARCHING) {
							onMoveCalculated(resultRecord);
						}
					}
					
					updateSearchFinished();
					
					if (searchingEngineMap.isEmpty()) {
						if (searchFinished) {
							sendResult();
						}
						
						isSearchRunning = false;
						monitor.notifyAll();
					}
					
					if (searchFinished || managerState == ManagerState.TERMINATING) {
						trimEngines();
					}
					
					final long currentTime = System.currentTimeMillis();
					final long timeoutInfo = lastSearchInfoTime + SEARCH_INFO_TIMEOUT - currentTime;
					
					if (searchInfoChanged && timeoutInfo < 0) {
						updateSearchInfo();
					}
					
					// Wait for event
					if (resultRecordQueue.isEmpty()) {
						if (managerState == ManagerState.SEARCHING && !searchFinished && (algo.getHorizon() > startHorizon || searchInfoChanged)) {
							final long timeoutSearch = searchStartTime + maxTimeForMove - currentTime;
							final long timeout = Math.min(timeoutInfo, timeoutSearch);
							
							if (timeout > 0) {
								monitor.wait(timeout);
							}
							else {
								monitor.wait(1);   // We must unlock monitor for a short time
							}
						}
						else {
							monitor.wait();
						}
					}
				}
				catch (Throwable ex) {
					Logger.logException(ex);
				}
			}
		}
	}

	// Clip boundaries to fasten engine stop
	private void trimEngines() {
		if (!areEnginesTrimmed) {
			for (EngineRecord record: searchingEngineMap.values()) {
				Logger.logMessage("Clipping out engine " + record.index);
				
				record.engine.terminateTask();
			}
			
			areEnginesTrimmed = true;
		}
	}
	
	private void onMoveCalculated(final ResultRecord resultRecord) {
		// Stop searching of the engine
		final EngineRecord engineRecord = resultRecord.engineRecord;
		Logger.logMessage("Engine " + engineRecord.index + " stopped, evaluation " + resultRecord.result.getNodeEvaluation() + ", PVAR " + resultRecord.result.getPrincipalVariation());

		if (resultRecord.result.isSearchTerminated())
			return;
		
		// Store evaluation
		final SearchNode calculatedNode = engineRecord.node;
		final NodeEvaluation evaluation = resultRecord.result.getNodeEvaluation();
		final boolean resultValid = !resultRecord.engineRecord.isSmallerWindow || evaluation.getEvaluation() >= resultRecord.engineRecord.taskBeta;
		final MoveList principalVariation = resultRecord.result.getPrincipalVariation();
		
		searchInfoChanged = true;
		algo.onMoveCalculated (calculatedNode, evaluation, principalVariation, resultValid);
	}

	private void stopEngine(final EngineRecord engineRecord) {
		Logger.logMessage("Stopping engine " + engineRecord.index);
		
		engineRecord.engine.stopSearching();
		searchingEngineMap.remove(engineRecord.engine);
		waitingEngineList.add(engineRecord);
	}
	
	/**
	 * Updates alpha-beta boundaries of engines that calculates direct children
	 * of given node as a result of node evaluation change.
	 * @param node node which evaluation was changed
	 */
	@Override
	public void updateEnginesTaskBoundaries(final SearchNode node) {
		for (EngineRecord engineRecord: searchingEngineMap.values()) {
			if (node == engineRecord.node.getParent() && engineRecord.engine.getEngineState() == EngineState.SEARCHING) {
				final NodeEvaluation nodeEvaluation = node.getEvaluation();
				engineRecord.engine.updateTaskBoundaries(-nodeEvaluation.getBeta(), -nodeEvaluation.getAlpha());
			}
		}
	}
	
	private boolean isDeepParent(final SearchNode parent, final SearchNode child) {
		SearchNode node = child;
		
		while (node != null && node.getDepth() > parent.getDepth()) {
			node = node.getParent();
		}
		
		return node == parent;
	}
	
	/**
	 * Stops engines that calculates direct or indirect children of given node.
	 * @param parentNode parent node
	 */
	@Override
	public void stopChildEngines (final SearchNode parentNode) {
		final Set<ISearchEngine> stoppedEngines = new HashSet<ISearchEngine>();
		
		// Stop engines that are searching child nodes
		for (Iterator<EngineRecord> it = searchingEngineMap.values().iterator(); it.hasNext(); ) {
			final EngineRecord record = it.next();
			
			if (isDeepParent (parentNode, record.node)) {
				record.engine.stopSearching();
				
				it.remove();
				waitingEngineList.add(record);
				stoppedEngines.add(record.engine);
			}
		}
		
		// Remove results from the queue
		for (Iterator<ResultRecord> it = resultRecordQueue.iterator(); it.hasNext(); ) {
			final ResultRecord record = it.next();
			
			if (stoppedEngines.contains(record.engineRecord.engine)) {
				it.remove();
			}
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
		final SearchResult searchResult = algo.getResult();
		final SearchInfo info = new SearchInfo();
		
		info.setElapsedTime(System.currentTimeMillis() - searchStartTime);
		info.setHorizon(searchResult.getHorizon());
		info.setNodeCount(totalNodeCount);
		info.setPrincipalVariation(searchResult.getPrincipalVariation());
		info.setEvaluation(searchResult.getNodeEvaluation().getEvaluation());
		
		for (ISearchManagerHandler handler: handlerRegistrar.getHandlers())
			handler.onSearchInfoUpdate(info);
		
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
			
			final ISearchStrategy strategy = algo.getStrategy();
			
			if (strategy == null)
				throw new RuntimeException("Search strategy is not set");

			if (engineFactory == null)
				throw new RuntimeException("Engine factory is not set");

			strategy.setHandler(this);
			handlerRegistrar.setChangesEnabled(false);
			
			managerThread = new Thread(new Runnable() {
				public void run() {
					processResults();
				}
			});
			
			managerThread.setName("SearchManagerImpl thread");
			managerThread.setDaemon(true);
			managerThread.start();
			
			for (int i = 0; i < threadCount; i++) {
				final ISearchEngine engine = engineFactory.createEngine();

				engine.getHandlerRegistrar().addHandler(engineHandler);
				engine.setHashTable(algo.getHashTable());
				engine.setTablebaseEvaluator(algo.getTablebaseEvaluator());
				engine.setSearchSettings(algo.getSearchSettings());
				engine.start();
				
				final EngineRecord record = new EngineRecord();
				record.engine = engine;
				record.node = null;
				record.index = i+1;
				
				waitingEngineList.add(record);
			}
			
			managerState = ManagerState.WAITING;
		}
	}
	
	private List<EngineRecord> getAllEngineList() {
		final List<EngineRecord> allEngineList = new ArrayList<EngineRecord>(waitingEngineList.size() + searchingEngineMap.size());
		
		allEngineList.addAll(waitingEngineList);
		allEngineList.addAll(searchingEngineMap.values());
		
		return allEngineList;
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
		
		final List<EngineRecord> allEngineList = getAllEngineList();
		
		for (EngineRecord engineRecord: allEngineList) {
			engineRecord.engine.stop();
		}

		while (true) {
			try {
				managerThread.join();
				break;
			}
			catch (InterruptedException ex) {
			}
		}
		
		synchronized (monitor) {
			for (EngineRecord engineRecord: allEngineList) {
				engineRecord.engine.getHandlerRegistrar().removeHandler(engineHandler);
			}
			
			waitingEngineList.clear();
			searchingEngineMap.clear();
			handlerRegistrar.setChangesEnabled(true);
			
			managerThread = null;
			resultRecordQueue.clear();
			
			algo.getStrategy().setHandler(null);
			
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
			this.areEnginesTrimmed = false;
			this.searchInfoChanged = false;
			this.lastSearchInfoTime = 0;
			this.isSearchRunning = true;
			
			searchStartTime = System.currentTimeMillis();
			totalNodeCount = 0;
			managerState = ManagerState.SEARCHING;

			final Move bestMove = algo.initializeSearch(position, startHorizon);
			
			if (bestMove != null) {
				//searchResult.getNodeEvaluation().setEvaluation(Evaluation.DRAW);
				sendMoveBeforeSearch(bestMove);
				this.isSearchRunning = false;
			}
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
			
			return algo.getResult();
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
	 * Returns number of waiting search engines.
	 * @return number of search engines that are waiting for some task
	 */
	public int getWaitingEngineCount() {
		synchronized (monitor) {
			return waitingEngineList.size();
		}
	}
	
	/**
	 * Returns total number of search engines.
	 * @return total number of search engines
	 */
	public int getTotalEngineCount() {
		synchronized (monitor) {
			return waitingEngineList.size() + searchingEngineMap.size();
		}
	}
	
	private void sendMoveBeforeSearch (final Move move) {
		final MoveList principalVariation = algo.getResult().getPrincipalVariation();
		principalVariation.clear();
		principalVariation.add(move);
		
		searchFinished = true;
		monitor.notify();
	}
	
	/**
	 * Adds task to some waiting search engine.
	 * Throws an exception if there is no waiting engine.
	 * @param node node to calculate
	 */
	public void calculateNode (final SearchNode node, final int alpha, final int beta, final boolean isSmallerWindow) {
		synchronized (monitor) {
			if (waitingEngineList.isEmpty())
				throw new RuntimeException("There is no waiting search engine");
			
			final SearchTask task = algo.openTaskForNode(node, alpha, beta, isSmallerWindow);
			
			node.setEvaluationState(EvaluationState.EVALUATING);
			
			final EngineRecord engineRecord = waitingEngineList.remove (waitingEngineList.size() - 1);
			searchingEngineMap.put(engineRecord.engine, engineRecord);

			engineRecord.node = node;
			engineRecord.taskAlpha = alpha;
			engineRecord.taskBeta = beta;
			engineRecord.isSmallerWindow = isSmallerWindow;
			engineRecord.engine.startSearching(task);
			
			if (GlobalSettings.isDebug()) {
				Logger.logMessage("Starting engine " + engineRecord.index + ", horizon " + algo.getHorizon() + ", move " + node.getMove() + "window <" + engineRecord.taskAlpha + ", " + engineRecord.taskBeta + ">");
			}
		}
	}
	
	/**
	 * Returns root node of the search.
	 * @return root node of the search
	 */
	public SearchNode getRootNode() {
		synchronized (monitor) {
			return algo.getRootNode();
		}
	}
	
	/**
	 * Opens given node.
	 * @param node  node to open
	 */
	public void openNode (final SearchNode node) {
		synchronized (monitor) {
			algo.openNode(node);
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

			algo.setHashTable(table);
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

			algo.setBookSearchEnabled(enabled);
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

			algo.setSingleMoveSearchEnabled(enabled);
		}
	}

	
	/**
	 * Gets search settings.
	 * @return search settings
	 */
	public SearchSettings getSearchSettings() {
		synchronized (monitor) {
			return algo.getSearchSettings();
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
			
			algo.setSearchSettings(searchSettings);
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
			
			algo.setTablebaseEvaluator(tablebaseEvaluator);
		}
	}
	
	/**
	 * Check if calculation is finished.
	 * @return true if searchFinished flag is set
	 */
	@Override
	public boolean isSearchFinished() {
		synchronized (monitor) {
			return searchFinished;
		}
	}
}
