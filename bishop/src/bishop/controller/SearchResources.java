package bishop.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;

import parallel.Parallel;
import bishop.base.DefaultAdditiveMaterialEvaluator;
import bishop.base.IMaterialEvaluator;
import bishop.base.PgnReader;
import bishop.engine.AlgebraicPositionEvaluation;
import bishop.engine.BookSource;
import bishop.engine.HashTableImpl;
import bishop.engine.IPositionEvaluation;
import bishop.engine.ISearchManager;
import bishop.engine.PositionEvaluationCoeffs;
import bishop.engine.PositionEvaluatorSwitchFactory;
import bishop.engine.PositionEvaluatorSwitchSettings;
import bishop.engine.SearchManagerImpl;
import bishop.engine.SerialSearchEngineFactory;
import bishop.engine.TableMaterialEvaluator;
import bishop.engine.TablebasePositionEvaluator;

public class SearchResources {
	
	private static final int MAX_TOTAL_DEPTH = 256;
	public static final int MAX_THREADS = 3;
	
	private static final String BOOK_PATH = "book.pgn";
	private static final String MATERIAL_PATH = "material.tbl";
	private static final String EVALUATION_COEFFS_PATH = "coeffs.tbl";

	private final IApplication application;
	private final SerialSearchEngineFactory searchEngineFactory;
	private final HashTableImpl hashTable;
	private final ISearchManager searchManager;
	private final TablebasePositionEvaluator tablebasePositionEvaluator;
	
	private final Supplier<IPositionEvaluation> evaluationFactory;
	
	public SearchResources(final IApplication application) {
		this.application = application;
		
		searchEngineFactory = new SerialSearchEngineFactory();
		
		final int threadCount = Math.min(application.getSettings().getEngineSettings().getThreadCount(), SearchResources.MAX_THREADS);
		final Parallel parallel = new Parallel(threadCount);
		searchEngineFactory.setParallel(parallel);

		evaluationFactory = createEvaluationFactory(application.getRootUrl());
		
		final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
		final IMaterialEvaluator materialEvaluator = createMaterialEvaluator();
		final PositionEvaluatorSwitchFactory evaluatorFactory = new PositionEvaluatorSwitchFactory(settings, materialEvaluator, getEvaluationFactory());
		
		searchEngineFactory.setPositionEvaluatorFactory(evaluatorFactory);
		searchEngineFactory.setEvaluationFactory(evaluationFactory);
		searchEngineFactory.setMaximalDepth(MAX_TOTAL_DEPTH);
		
		hashTable = new HashTableImpl(10);
		
		final EngineSettings engineSettings = application.getSettings().getEngineSettings();
		final java.io.File tbbsDir = new java.io.File (engineSettings.getTablebaseDirectory());
		tablebasePositionEvaluator = new TablebasePositionEvaluator(tbbsDir);
		
		searchManager = new SearchManagerImpl();
		searchManager.setEngineFactory(searchEngineFactory);
		searchManager.setHashTable(hashTable);
		searchManager.setTablebaseEvaluator (tablebasePositionEvaluator);
		
		setBookToManager();
		updateSettings();
	}

	public static Supplier<IPositionEvaluation> createEvaluationFactory(final URL rootUrl) {
		final PositionEvaluationCoeffs evaluationCoeffs = createEvaluationCoeffs(rootUrl);
		
		return () -> new AlgebraicPositionEvaluation(evaluationCoeffs);
	}
	
	private void setBookToManager() {
		try {
			final URL url = new URL(application.getRootUrl(), BOOK_PATH);
			
			try (final InputStream stream = url.openStream()) {
				final PgnReader pgn = new PgnReader();
				pgn.readPgnFromStream(stream);
				
				final BookSource book = new BookSource();
				book.addPgn(pgn);
				
				System.out.println ("Read book with " + book.getPositionCount() + " positions");
				
				searchManager.setBook(book);
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static IMaterialEvaluator createMaterialEvaluator(final URL rootUrl) {
		try {
			final URL url = new URL(rootUrl, MATERIAL_PATH);
			
			try (final InputStream stream = url.openStream()) {
				final IMaterialEvaluator baseEvaluator = DefaultAdditiveMaterialEvaluator.getInstance();
				final TableMaterialEvaluator tableEvaluator = new TableMaterialEvaluator(baseEvaluator);
				tableEvaluator.read(stream);

				return tableEvaluator;
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private IMaterialEvaluator createMaterialEvaluator() {
		return createMaterialEvaluator(application.getRootUrl());
	}

	private static PositionEvaluationCoeffs createEvaluationCoeffs(final URL rootUrl) {
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

	public HashTableImpl getHashTable() {
		return hashTable;
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
		
		hashTable.resize (engineSettings.getHashTableExponent());
	}
	
	public Supplier<IPositionEvaluation> getEvaluationFactory() {
		return evaluationFactory;
	}
}
