package bishop.controller;

import bishop.base.CombinedPositionEvaluationTable;
import bishop.base.PieceTypeEvaluations;
import bishop.engine.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;

public class SearchResources {
	
	private static final int MAX_TOTAL_DEPTH = 256;
	private static final int DEFAULT_HASH_TABLE_EXPONENT = 9;
	
	private static final String BOOK_PATH = "book.dat";
	private static final String EVALUATION_COEFFS_PATH = "coeffs.tbl";

	private final IApplication application;
	private final SerialSearchEngineFactory searchEngineFactory;
	private final EvaluationHashTableImpl evaluationHashTable;
	private final BestMoveHashTableImpl bestMoveHashTable;
	private final ISearchManager searchManager;
	private final TablebasePositionEvaluator tablebasePositionEvaluator;
	
	private final Supplier<IPositionEvaluation> evaluationFactory;
	
	public SearchResources(final IApplication application) {
		this.application = application;
		
		searchEngineFactory = new SerialSearchEngineFactory();
		
		final int threadCount = application.getSettings().getEngineSettings().getThreadCount();

		final PositionEvaluationCoeffs evaluationCoeffs = createEvaluationCoeffs(application.getRootUrl());
		evaluationFactory = createEvaluationFactory(evaluationCoeffs);

		final PieceTypeEvaluations pieceTypeEvaluations = evaluationCoeffs.getPieceTypeEvaluations();
		final PositionEvaluatorSwitchFactory evaluatorFactory = new PositionEvaluatorSwitchFactory(getEvaluationFactory());

		searchEngineFactory.setPositionEvaluatorFactory(evaluatorFactory);
		searchEngineFactory.setMaximalDepth(MAX_TOTAL_DEPTH);
		searchEngineFactory.setPieceTypeEvaluations(pieceTypeEvaluations);
		
		evaluationHashTable = new EvaluationHashTableImpl(DEFAULT_HASH_TABLE_EXPONENT);
		bestMoveHashTable = new BestMoveHashTableImpl(DEFAULT_HASH_TABLE_EXPONENT);
		
		final EngineSettings engineSettings = application.getSettings().getEngineSettings();
		final java.io.File tbbsDir = new java.io.File (engineSettings.getTablebaseDirectory());
		tablebasePositionEvaluator = new TablebasePositionEvaluator(tbbsDir);
		
		searchManager = new SearchManagerImpl();
		searchManager.setEngineFactory(searchEngineFactory);
		searchManager.setHashTable(evaluationHashTable, bestMoveHashTable);
		searchManager.setTablebaseEvaluator (tablebasePositionEvaluator);
		searchManager.setThreadCount(threadCount);
		searchManager.setPieceTypeEvaluations (pieceTypeEvaluations);
		searchManager.setCombinedPositionEvaluationTable(new CombinedPositionEvaluationTable(evaluationCoeffs));
		
		setBookToManager();
		updateSettings();
	}

	public static Supplier<IPositionEvaluation> createEvaluationFactory(PositionEvaluationCoeffs evaluationCoeffs) {
		return () -> new AlgebraicPositionEvaluation(evaluationCoeffs);
	}
	
	private void setBookToManager() {
		try {
			final URL url = new URL(application.getRootUrl(), BOOK_PATH);
			final BookReader book = new BookReader(url);
				
			searchManager.setBook(book);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static PositionEvaluationCoeffs createEvaluationCoeffs(final URL rootUrl) {
		try {
			final URL url = new URL(rootUrl, EVALUATION_COEFFS_PATH);
			
			try (final InputStream stream = url.openStream()) {
				final PositionEvaluationCoeffs evaluationCoeffs = new PositionEvaluationCoeffs();
				evaluationCoeffs.read(stream);

				return evaluationCoeffs;
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public SerialSearchEngineFactory getSearchEngineFactory() {
		return searchEngineFactory;
	}

	public ISearchManager getSearchManager() {
		return searchManager;
	}
	
	public TablebasePositionEvaluator getTablebasePositionEvaluator() {
		return tablebasePositionEvaluator;
	}
	
	public void updateSettings() {
		final ApplicationSettings applicationSettings = application.getSettings();
		final EngineSettings engineSettings = applicationSettings.getEngineSettings();
		
		evaluationHashTable.resize (engineSettings.getHashTableExponent());
		bestMoveHashTable.resize (engineSettings.getHashTableExponent());
		searchManager.setThreadCount(engineSettings.getThreadCount());
	}
	
	public Supplier<IPositionEvaluation> getEvaluationFactory() {
		return evaluationFactory;
	}
}
