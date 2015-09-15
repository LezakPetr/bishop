package bishop.engine;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import utils.Logger;

import bishop.base.GlobalSettings;
import bishop.base.Move;
import bishop.base.MoveList;
import bishop.base.Position;
import bishop.engine.SearchNode.EvaluationState;
import bishop.engine.SearchNode.TreeState;

public class SearchManagerAlgo {

	private final ISearchManagerAlgoHandler handler;
	private final SearchNodeExpandor nodeExpandor;
	private TablebasePositionEvaluator tablebaseEvaluator;
	private SearchNode rootNode;
	private ISearchStrategy strategy;
	private SearchSettings searchSettings;
	private IHashTable hashTable;
	private IBook book;
	private boolean bookSearchEnabled;
	private boolean singleMoveSearchEnabled;
	private Position initialPosition;
	private final Random random;
	private final FinitePositionEvaluator finiteEvaluator;
	private final SearchExtensionCalculator extensionCalculator;
	private final MoveExtensionEvaluator moveExtensionEvaluator;
	private int horizon;
	private int rootMaterialEvaluation;
	private final SearchResult searchResult;


	/**
	 * Constructor.
	 * @param handler handler of the algorithm
	 */
	public SearchManagerAlgo(final ISearchManagerAlgoHandler handler) {
		this.handler = handler;
		this.initialPosition = new Position();
		this.rootNode = null;
		this.hashTable = new NullHashTable();
		this.random = new Random();
		
		this.extensionCalculator = new SearchExtensionCalculator();
		this.moveExtensionEvaluator = new MoveExtensionEvaluator();
		setSearchSettings(new SearchSettings());
		
		this.bookSearchEnabled = true;
		this.singleMoveSearchEnabled = true;

		this.nodeExpandor = new SearchNodeExpandor();
		
		this.finiteEvaluator = new FinitePositionEvaluator();
		this.searchResult = new SearchResult();
	}
	
	public boolean isIterationFinished() {
		return rootNode.getEvaluationState() == EvaluationState.EVALUATED;
	}

	public ISearchStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(ISearchStrategy strategy) {
		this.strategy = strategy;
	}

	public IBook getBook() {
		return book;
	}

	public void setBook(IBook book) {
		this.book = book;
	}
	
	public SearchNode getRootNode() {
		return rootNode;
	}
	
	/**
	 * Initializes new search.
	 * @param position initial position
	 * @param startHorizon initial horizon
	 * @return move if the best move can be determined without search, null if not
	 */
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
		
		openRootNode();

		if (isIterationFinished()) {
			final MoveList principalVariation = rootNode.getPrincipalVariation();
			
			if (principalVariation.getSize() > 0)
				return principalVariation.get(0);
		}
		else {
			hashTable.clear();
			
			strategy.onNewSearch();
			startNewIteration();
		}
		
		
		return null;
	}
	
	private void setBeforeSearchResult(final Move move) {
		searchResult.clear();
		
		final MoveList principalVariation = searchResult.getPrincipalVariation();
		principalVariation.clear();
		principalVariation.add(move);
		
		searchResult.getNodeEvaluation().setEvaluation(Evaluation.DRAW);
	}

	/**
	 * Opens given node.
	 * @param node node to open
	 */
	public void openNode (final SearchNode node) {
		switch (node.getTreeState()) {
			case CLOSED:
				sortMoves(node);
				updateExpandedNode (node);		
				break;
				
			case LEAF:
				nodeExpandor.expandNode(node);
				updateExpandedNode (node);
				break;
				
			case OPENED:
				break;
		}
		
		finiteEvaluator.setRepeatedPositionRegister(getRepeatedPositionRegister (node));
		
		// We must open root node in all cases
		if (node.getDepth() > 0 && finiteEvaluator.evaluate(node.getPosition(), node.getDepth(), node.getRequiredHorizon(), node.getEvaluation().getAlpha(), node.getEvaluation().getBeta())) {
			setLeafNode(node, finiteEvaluator.getEvaluation());
		}
		else {
			if (node.getChildren().isEmpty())
				node.setTreeState(TreeState.LEAF);
			else
				node.setTreeState(TreeState.OPENED);
		}
	}
	
	public static RepeatedPositionRegister getRepeatedPositionRegister(final SearchNode node) {
		final int size = node.getDepth() + 1;
		final RepeatedPositionRegister stack = new RepeatedPositionRegister();
		stack.clearAnsReserve(size);
		
		final SearchNode[] nodeList = new SearchNode[size];
		
		for (SearchNode currentNode = node; currentNode != null; currentNode = currentNode.getParent()) {
			final int depth = currentNode.getDepth();
			nodeList[depth] = currentNode;
		}
		
		for (SearchNode currentNode: nodeList) {
			stack.pushPosition(currentNode.getPosition(), currentNode.getMove());
		}
		
		return stack;
	}

	public SearchSettings getSearchSettings() {
		return searchSettings;
	}

	public void setSearchSettings(SearchSettings searchSettings) {
		this.searchSettings = searchSettings;
		extensionCalculator.setSearchSettings(searchSettings);
		moveExtensionEvaluator.setSettings(searchSettings);
	}

	public IHashTable getHashTable() {
		return hashTable;
	}

	public void setHashTable(IHashTable hashTable) {
		this.hashTable = hashTable;
	}

	public boolean isBookSearchEnabled() {
		return bookSearchEnabled;
	}

	public void setBookSearchEnabled(boolean bookSearchEnabled) {
		this.bookSearchEnabled = bookSearchEnabled;
	}

	public boolean isSingleMoveSearchEnabled() {
		return singleMoveSearchEnabled;
	}

	public void setSingleMoveSearchEnabled(boolean singleMoveSearchEnabled) {
		this.singleMoveSearchEnabled = singleMoveSearchEnabled;
	}

	public TablebasePositionEvaluator getTablebaseEvaluator() {
		return tablebaseEvaluator;
	}

	public void setTablebaseEvaluator(TablebasePositionEvaluator tablebaseEvaluator) {
		this.tablebaseEvaluator = tablebaseEvaluator;
		this.finiteEvaluator.setTablebaseEvaluator(tablebaseEvaluator);
	}
	
	/**
	 * Starts new iteration of the search.
	 */
	private void startNewIteration() {
		horizon += ISearchEngine.HORIZON_GRANULARITY;
		
		if (!handler.isSearchFinished()) {
			Logger.logMessage("-----===== Iteration with horizon " + horizon + " =====-----");
			
			rootNode.clear();
			rootNode.setRequiredHorizon(horizon);
			
			strategy.onNewIteration(horizon);
		}
	}

	public int getHorizon() {
		return horizon;
	}

	public void onMoveCalculated(final SearchNode calculatedNode, final NodeEvaluation evaluation, final MoveList principalVariation, final boolean resultValid) {
		calculatedNode.getEvaluation().assign(evaluation);
		
		if (resultValid) {
			calculatedNode.setEvaluationState(EvaluationState.EVALUATED);
			calculatedNode.getPrincipalVariation().assign(principalVariation);
			
			updateParentNodes(calculatedNode);
			updateResult();
			
			// Stop the search if it was finished
			handler.updateSearchFinished();
		}
		
		if (!handler.isSearchFinished()) {
			if (isIterationFinished()) {
				startNewIteration();
			}
			else {
				strategy.onNodeCalculated(calculatedNode);
			}
		}

	}
	
	public SearchTask openTaskForNode(final SearchNode node, final int alpha, final int beta, final boolean isSmallerWindow) {
		if (beta < alpha)
			throw new RuntimeException("Beta lower than alpha");
					
		final int depthAdvance = node.getDepth();
		final SearchTask task = new SearchTask();
		
		task.setHorizon(node.getRequiredHorizon());
		task.setDepthAdvance(depthAdvance);
		task.setInitialSearch(true);
		task.getPosition().assign(node.getPosition());			
		task.setAlpha(alpha);
		task.setBeta(beta);
		task.setRepeatedPositionRegister(SearchManagerAlgo.getRepeatedPositionRegister (node));
		task.setRootMaterialEvaluation(rootMaterialEvaluation);
		task.setMaxExtension(node.getMaxExtension());
		
		final MoveList taskPrincipalVariation = task.getPrincipalVariation();
		final MoveList resultPrincipalVariation = searchResult.getPrincipalVariation();
		
		taskPrincipalVariation.clear();
		
		if (depthAdvance < resultPrincipalVariation.getSize())
			taskPrincipalVariation.addRange(resultPrincipalVariation, depthAdvance, resultPrincipalVariation.getSize() - depthAdvance);

		return task;
	}

	public SearchResult getResult() {
		return searchResult.copy();
	}
	
	/**
	 * Updates calculation result.
	 */
	private void updateResult() {
		final List<SearchNode> children = rootNode.getChildren();
		
		if (children.isEmpty() || horizon <= ISearchEngine.HORIZON_GRANULARITY || children.get(0).getEvaluationState() == EvaluationState.EVALUATED) {
			if (!searchResult.getPrincipalVariation().equals(rootNode.getPrincipalVariation()) || searchResult.getHorizon() != horizon) {
				searchResult.getPrincipalVariation().assign(rootNode.getPrincipalVariation());
				
				Logger.logMessage("New principal variation [" + horizon + "] "+ rootNode.getPrincipalVariation() + ", evaluation " + rootNode.getEvaluation());
			}
			
			searchResult.setNodeCount(0);
			searchResult.getNodeEvaluation().assign(rootNode.getEvaluation());
			searchResult.setHorizon(horizon);
		}
		else {
			Logger.logMessage("Delaying result");
		}
	}
	
	private void setLeafNode(final SearchNode node, final int evaluation) {
		node.setTreeState(TreeState.LEAF);
		node.setEvaluationState(EvaluationState.EVALUATED);
		
		final NodeEvaluation nodeEvaluation = node.getEvaluation();
		nodeEvaluation.clear();
		nodeEvaluation.setEvaluation(evaluation);
		
		updateParentNodes(node);
		updateResult();
	}
	
	private void updateHashRecord (final SearchNode node) {
		final HashRecord record = new HashRecord();
		final NodeEvaluation nodeEvaluation = node.getEvaluation();
		
		record.setEvaluationAndType(nodeEvaluation, node.getDepth());
		record.setHorizon(node.getRequiredHorizon());
		
		if (node.getPrincipalVariation().getSize() > 0) {
			record.setCompressedBestMove(node.getPrincipalVariation().get(0).getCompressedMove());
		}
		else {
			record.setCompressedBestMove(Move.NONE_COMPRESSED_MOVE);
		}
		
		hashTable.updateRecord(node.getPosition(), record);
	}

	private void updateParentNodes (final SearchNode node) {
		SearchNode actualNode = node;
		
		while (actualNode != null) {
			updateHashRecord(actualNode);
			
			final SearchNode parentNode = actualNode.getParent();
			
			if (parentNode == null) {
				// Root node
				break;   
			}

			final int evaluatedChildrenCount = parentNode.getEvaluatedChildrenCount() + 1;
			parentNode.setEvaluatedChildrenCount(evaluatedChildrenCount);
			
			final NodeEvaluation parentEvaluation = actualNode.getEvaluation().getParent();
			
			if (parentNode.getEvaluation().update(parentEvaluation)) {
				final MoveList parentPrincipalVariation = parentNode.getPrincipalVariation();
				
				parentPrincipalVariation.clear();
				parentPrincipalVariation.add(actualNode.getMove());
				parentPrincipalVariation.addAll(actualNode.getPrincipalVariation());
				
				handler.updateEnginesTaskBoundaries(parentNode);
			}
			
			if (evaluatedChildrenCount < parentNode.getChildren().size() && !parentNode.getEvaluation().isBetaCutoff())
				break;
			
			parentNode.setEvaluationState(EvaluationState.EVALUATED);
			actualNode = parentNode;
		}
		
		handler.stopChildEngines (actualNode);
	}
	
	private void openRootNode() {
		openNode(rootNode);
		
		final List<SearchNode> children = rootNode.getChildren();
		
		for (SearchNode child: children) {
			final Position position = child.getPosition();
			final int horizon = child.getRequiredHorizon();
			final int currentDepth = child.getDepth();
			
			if (currentDepth > 0 && finiteEvaluator.evaluate(position, currentDepth, horizon, -Evaluation.MAX, Evaluation.MAX)) {
				final int evaluation = finiteEvaluator.getEvaluation();
				setLeafNode(child, evaluation);
				
				Logger.logMessage("TBBS: " + child.getMove()+ " " + Evaluation.toString(evaluation));
			}
		}
	}

	private Move singleMoveSearch() {
		openNode(rootNode);
		
		final List<SearchNode> children = rootNode.getChildren();
		final int moveCount = children.size();
		
		if (moveCount == 1)
			return children.get(0).getMove();
		else
			return null;
	}
	
	private Move bookSearch() {
		if (book == null)
			return null;
		
		final BookRecord record = book.getRecord(rootNode.getPosition());
		
		if (record == null)
			return null;
		
		final BookMove bookMove = record.getRandomMove(random);
		
		if (bookMove == null)
			return null;
		
		return bookMove.getMove();
	}
	
	private void updateExpandedNode(final SearchNode node) {
		clearNodeEvaluation(node);
		
		if (!sinkPrincipalVariation(node))
			promoteHashBestMove(node);
		
		node.getPrincipalVariation().clear();
	}
	
	private void sortMoves(final SearchNode node) {
		Collections.sort(node.getChildren(), SearchNodeComparator.INSTANCE);
	}
	
	private boolean sinkPrincipalVariation(final SearchNode node) {
		final MoveList parentPrincipalVariation = node.getPrincipalVariation();
		
		if (parentPrincipalVariation.getSize() == 0)
			return false;
		
		final List<SearchNode> children = node.getChildren();
		final Move principalMove = parentPrincipalVariation.get(0);
		int childIndex = -1;
			
		for (int i = 0; i < children.size(); i++) {
			final SearchNode child = children.get(i);
			
			if (child.getMove().equals(principalMove)) {
				childIndex = i;
				break;
			}
		}
			
		if (childIndex < 0)
			throw new RuntimeException("Move from principal variation not found");
			
		final SearchNode child = children.remove(childIndex);
		children.add(0, child);
		
		final MoveList childPrincipalVariation = child.getPrincipalVariation();
		
		childPrincipalVariation.clear();
		childPrincipalVariation.addRange(parentPrincipalVariation, 1, parentPrincipalVariation.getSize() - 1);
		
		return true;
	}
	
	private void clearNodeEvaluation(final SearchNode node) {
		final List<SearchNode> children = node.getChildren();
		node.getEvaluation().clear();
		
		final int parentRequiredHorizon = node.getRequiredHorizon();
		final Position position = node.getPosition();
		final boolean isCheck = position.isCheck();
		
		final HashRecord hashRecord = new HashRecord();
		hashTable.getRecord(position, hashRecord);
		
		final AttackCalculator attackCalculator = new AttackCalculator();
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		final int positionExtension = extensionCalculator.getExtension(position, isCheck, hashRecord, horizon, attackCalculator);
		final int beginMaterialEvaluation = node.getPosition().getMaterialEvaluation();
		final int maxExtension = node.getMaxExtension();
		
		for (SearchNode currentNode: children) {
			final int moveExtension = moveExtensionEvaluator.getExtension(currentNode.getPosition(), currentNode.getMove(), rootMaterialEvaluation, beginMaterialEvaluation);
			final int totalExtension = Math.min (Math.min(moveExtension + positionExtension, ISearchEngine.HORIZON_GRANULARITY), maxExtension);

			currentNode.setEvaluationState(EvaluationState.NOT_EVALUATED);
			currentNode.getPrincipalVariation().clear();
			currentNode.setMaxExtension(maxExtension - totalExtension);
						
			currentNode.setRequiredHorizon(parentRequiredHorizon + totalExtension - ISearchEngine.HORIZON_GRANULARITY);
		}
	}
	
	private void promoteHashBestMove(final SearchNode node) {
		final HashRecord hashRecord = new HashRecord();
		
		if (!hashTable.getRecord(node.getPosition(), hashRecord))
			return;
		
		final int hashBestCompressedMove = hashRecord.getCompressedBestMove();
		
		if (hashBestCompressedMove == Move.NONE_COMPRESSED_MOVE)
			return;
		
		final List<SearchNode> children = node.getChildren();
		
		for (int i = 0; i < children.size(); i++) {
			final SearchNode currentNode = children.get(i);
			
			if (currentNode.getMove().getCompressedMove() == hashBestCompressedMove) {
				children.remove(i);
				children.add(0, currentNode);
				break;
			}
		}
	}

}
