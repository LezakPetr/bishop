package bishop.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import parallel.Parallel;

import bishop.base.PgnReader;
import bishop.engine.BookSource;
import bishop.engine.HashTableImpl;
import bishop.engine.ISearchManager;
import bishop.engine.PositionEvaluatorSwitchFactory;
import bishop.engine.PositionEvaluatorSwitchSettings;
import bishop.engine.SearchManagerImpl;
import bishop.engine.SerialSearchEngineFactory;
import bishop.engine.TablebasePositionEvaluator;

public class SearchResources {
	
	private static final int MAX_TOTAL_DEPTH = 256;
	public static final int MAX_THREADS = 3;
	
	private static final String BOOK_PATH = "book.pgn";

	private final IApplication application;
	private final SerialSearchEngineFactory searchEngineFactory;
	private final HashTableImpl hashTable;
	private final ISearchManager searchManager;
	private final TablebasePositionEvaluator tablebasePositionEvaluator;
	
	
	public SearchResources(final IApplication application) {
		this.application = application;
		
		searchEngineFactory = new SerialSearchEngineFactory();
		
		final int threadCount = Math.min(application.getSettings().getEngineSettings().getThreadCount(), SearchResources.MAX_THREADS);
		final Parallel parallel = new Parallel(threadCount);
		searchEngineFactory.setParallel(parallel);
		
		final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
		final PositionEvaluatorSwitchFactory evaluatorFactory = new PositionEvaluatorSwitchFactory(settings);
		
		searchEngineFactory.setPositionEvaluatorFactory(evaluatorFactory);
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
	
	private void setBookToManager() {
		try {
			final URL url = new URL(application.getRootUrl(), BOOK_PATH);
			final PgnReader pgn = new PgnReader();
			
			final InputStream stream = url.openStream();
			
			try {
				pgn.readPgnFromStream(stream);
				
				final BookSource book = new BookSource();
				book.addPgn(pgn);
				
				System.out.println ("Read book with " + book.getPositionCount() + " positions");
				
				searchManager.setBook(book);
			}
			finally {
				stream.close();
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
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

}
