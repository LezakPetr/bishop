package bishop.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import bishop.base.Color;
import bishop.base.Game;
import bishop.base.GameHeader;
import bishop.base.IGameNode;
import bishop.base.ITreeIterator;
import bishop.base.PgnReader;
import bishop.base.PgnWriter;
import bishop.controller.GameEditor;
import bishop.controller.GameSettings;
import bishop.controller.GameType;
import bishop.controller.IApplication;
import bishop.controller.IApplicationListener;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.RegimeType;
import bishop.controller.SideType;
import bishop.controller.Utils;
import bishop.engine.ISearchManager;
import utils.Logger;


@SuppressWarnings("serial")
public class ApplicationViewImpl extends JPanel implements IApplicationView, ILocalizedComponent {
	
	private final IApplication application;
	private Frame mainFrame;

	private final JMenuBar menuBar;
	private JFileChooser pgnFileChooser;
	
	private JMenu gameMenu;
	private JMenuItem newGameMenuItem;
	private JMenuItem loadGameMenuItem;
	private JMenuItem saveGameMenuItem;
	private JMenuItem editFenMenuItem;
	private JMenuItem editPositionMenuItem;
	private JMenuItem endingTrainingMenuItem;
	private JMenuItem gameSettingMenuItem;
	
	private JMenu notationMenu;
	private JMenuItem editGameHeaderMenuItem;
	private JMenuItem previousMoveMenuItem;
	private JMenuItem nextMoveMenuItem;
	private JMenuItem deleteVariationMenuItem;
	private JMenuItem promoteVariationMenuItem;
	private JMenuItem editCommentaryMenuItem;
	
	private JMenu settingMenu;
	private JMenuItem applicationSettingMenuItem;
	private JCheckBoxMenuItem reverseDeskMenuItem;
	private JCheckBoxMenuItem showDeskCoordinatesMenuItem;
	
	private JMenu helpMenu;
	private JMenuItem showLogMenuItem;
	private JMenuItem aboutMenuItem;
	
	private DeskImpl2D desk;
	private IBoard board;
	private IPieceSet pieceSet;
	private SearchInfoPanel searchInfoPanel;
	
	private RegimePlayView regimePlayView;
	private RegimeEditPositionView regimeEditPositionView;
	private RegimeAnalysisView regimeAnalysisView;
	private RegimeEndingTrainingView regimeEndingTrainingView;
	
	private JPanel regimePanel;
	private JComponent regimeComponent;
	
	private static final Dimension REGIME_PANEL_SIZE = new Dimension(225, 300);
	
	
	public ApplicationViewImpl(final IApplication application) {
		this.application = application;
		this.menuBar = new JMenuBar();
	}
	
	public void loadGraphics() throws IOException {
		loadBoard();
		loadPieceSet();
	}
	
	public void initialize() {
		initializeComponents();
		createMenus();

		createPgnFileChooser();
		
		this.regimePlayView = new RegimePlayView(this);
		this.regimeAnalysisView = new RegimeAnalysisView(this);
		this.regimeEditPositionView = new RegimeEditPositionView(this);
		this.regimeEndingTrainingView = new RegimeEndingTrainingView(this);

		application.getLocalizedComponentRegister().addComponent(this);
		application.getApplicationListenerRegistrar().addHandler(applicationListener);
		
		application.setGameRegime();
		updateMenuItemsEnabled();
	}
	
	private void loadBoard() throws IOException {
		final URL url = new URL(application.getRootUrl(), "graphics/board.brd");
		final InputStream stream = url.openStream();
		
		try {
			final SvgBoard svgBoard = new SvgBoard();
			svgBoard.readBoard(stream);
			
			this.board = svgBoard;
		}
		finally {
			stream.close();
		}		
	}
	
	private void loadPieceSet() throws IOException {
		final URL url = new URL(application.getRootUrl(), "graphics/piece_set.pcs");
		final InputStream stream = url.openStream();
		
		try {
			final SvgPieceSet svgPieceSet = new SvgPieceSet();
			svgPieceSet.readPieceSet(stream);
			
			pieceSet = svgPieceSet;
		}
		finally {
			stream.close();
		}
	}
	
	private void showAboutDialog() {
		AboutDialog.showModalDialog(mainFrame, application.getLocalization());
	}
	
	private void createPgnFileChooser() {
		try {
			pgnFileChooser = new JFileChooser();
		}
		catch (Exception ex) {
			// We are in the sandbox
			Logger.logException(ex);
		}
	}
	
	private void setPgnChooserFileNameFilter(final ILocalization localization) {
		try {
			if (pgnFileChooser != null) {
				pgnFileChooser.resetChoosableFileFilters();
				
				final FileFilter filter = Utils.addPgnFileFilter(pgnFileChooser, localization);
				Utils.addProblemFileFilter(pgnFileChooser, localization);
				
				pgnFileChooser.setFileFilter(filter);
			}
		}
		catch (Throwable th) {
			Logger.logException(th);
		}
	}
	
	private void initializeComponents() {
		this.setLayout(new GridBagLayout());
		 
		desk = new DeskImpl2D(mainFrame);
		desk.setBoard(board);
		desk.setPieceSet(pieceSet);
		
		this.add(
			desk.getComponent(),
			new GridBagConstraints (
			    0, 0, 1, 1,
			    1.0, 1.0,
			    GridBagConstraints.CENTER,
			    GridBagConstraints.BOTH,
			    new Insets(0, 0, 0, 0),
			    0, 0
			)
		);
		
		searchInfoPanel = new SearchInfoPanel(this);
		
		this.add(
			searchInfoPanel,
			new GridBagConstraints (
			    0, 1, 2, 1,
			    0.0, 0.0,
			    GridBagConstraints.CENTER,
			    GridBagConstraints.BOTH,
			    new Insets(0, 0, 0, 0),
			    0, 0
			)
		);
		
		final ISearchManager searchManager = application.getSearchResources().getSearchManager();
		searchManager.getHandlerRegistrar().addHandler(searchInfoPanel);
		
		regimePanel = new JPanel();
		regimePanel.setLayout(new BorderLayout());
		
		regimePanel.setMinimumSize(REGIME_PANEL_SIZE);
		regimePanel.setPreferredSize(REGIME_PANEL_SIZE);
		
		this.add(
			regimePanel,
			new GridBagConstraints (
			    1, 0, 1, 1,
			    0.0, 0.0,
			    GridBagConstraints.CENTER,
			    GridBagConstraints.BOTH,
			    new Insets(0, 0, 0, 0),
			    0, 0
			)
		);
	}
	
	private void createMenus() {
		// Game menu
		gameMenu = new JMenu();
		
		newGameMenuItem = new JMenuItem(newGameAction);
		gameMenu.add(newGameMenuItem);

		loadGameMenuItem = new JMenuItem(loadGameAction);
		gameMenu.add(loadGameMenuItem);

		saveGameMenuItem = new JMenuItem(saveGameAction);
		gameMenu.add(saveGameMenuItem);

		editFenMenuItem = new JMenuItem(editFenAction);
		gameMenu.add(editFenMenuItem);

		editPositionMenuItem = new JMenuItem(editPositionAction);
		gameMenu.add(editPositionMenuItem);
		
		endingTrainingMenuItem = new JMenuItem(endingTrainingAction);
		gameMenu.add(endingTrainingMenuItem);
		
		gameSettingMenuItem = new JMenuItem(gameSettingAction);
		gameMenu.add(gameSettingMenuItem);

		menuBar.add(gameMenu);
		
		// Notation menu
		notationMenu = new JMenu();
		
		editGameHeaderMenuItem = new JMenuItem(editGameHeaderAction);
		notationMenu.add(editGameHeaderMenuItem);
		
		previousMoveMenuItem = new JMenuItem(previousMoveAction);
		notationMenu.add(previousMoveMenuItem);

		nextMoveMenuItem = new JMenuItem(nextMoveAction);
		notationMenu.add(nextMoveMenuItem);

		deleteVariationMenuItem = new JMenuItem(deleteVariationAction);
		notationMenu.add(deleteVariationMenuItem);

		promoteVariationMenuItem = new JMenuItem(promoteVariationAction);
		notationMenu.add(promoteVariationMenuItem);

		editCommentaryMenuItem = new JMenuItem(editCommentaryAction);
		notationMenu.add(editCommentaryMenuItem);

		getApplicationMenuBar().add(notationMenu);
		
		// Setting menu
		settingMenu = new JMenu();
		
		applicationSettingMenuItem = new JMenuItem(applicationSettingAction);
		settingMenu.add(applicationSettingAction);
		
		reverseDeskMenuItem = new JCheckBoxMenuItem(reverseDeskAction);
		settingMenu.add(reverseDeskMenuItem);

		showDeskCoordinatesMenuItem = new JCheckBoxMenuItem(showDeskCoordinatesAction);
		showDeskCoordinatesMenuItem.setSelected(desk.getShowCoordinates());
		settingMenu.add(showDeskCoordinatesMenuItem);

		getApplicationMenuBar().add(settingMenu);

		// Help menu
		helpMenu = new JMenu();
		menuBar.add(helpMenu);
		
		showLogMenuItem = new JMenuItem(showLogAction);
		helpMenu.add(showLogMenuItem);
		
		aboutMenuItem = new JMenuItem(aboutAction);
		helpMenu.add(aboutMenuItem);
	}
	
	/**
	 * Returns desk of the application.
	 * @return desk
	 */
	public IDesk getDesk() {
		return desk;
	}
	
	/**
	 * Returns piece set of the application.
	 * @return piece set
	 */
	public IPieceSet getPieceSet() {
		return pieceSet;
	}
	
	/**
	 * Sets regime-dependent component.
	 * @param newComponent regime-dependent component or null to unset
	 */
	public void setRegimeComponent (final JComponent newComponent) {
		if (regimeComponent != null) {
			regimePanel.remove(regimeComponent);
		}
		
		regimeComponent = newComponent;
		
		if (newComponent != null) {
			regimePanel.add(newComponent);
		}
		
		this.validate();
		this.repaint();
	}
	
	/**
	 * Returns main frame of the application.
	 * @return main frame
	 */
	public Frame getMainFrame() {
		return mainFrame;
	}
	
	public void setMainFrame(Frame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	/**
	 * Returns application menu bar.
	 * @return menu bar
	 */
	public JMenuBar getApplicationMenuBar() {
		return menuBar;
	}
	
	public void updateLanguage(final ILocalization localization) {
		gameMenu.setText(localization.translateString("Menu.game"));
		newGameAction.putValue(Action.NAME, localization.translateString("Menu.game.newGame"));
		loadGameAction.putValue(Action.NAME, localization.translateString("Menu.game.loadGame"));
		saveGameAction.putValue(Action.NAME, localization.translateString("Menu.game.saveGame"));
		editFenAction.putValue(Action.NAME, localization.translateString("Menu.game.editFen"));
		editPositionAction.putValue(Action.NAME, localization.translateString("Menu.game.editPosition"));
		endingTrainingAction.putValue(Action.NAME, localization.translateString("Menu.game.endingTraining"));
		gameSettingAction.putValue(Action.NAME, localization.translateString("Menu.game.gameSettings"));
		
		notationMenu.setText(localization.translateString("Menu.notation"));
		editGameHeaderAction.putValue(Action.NAME, localization.translateString("Menu.notation.editGameMenu"));
		previousMoveAction.putValue(Action.NAME, localization.translateString("Menu.notation.previousMove"));
		nextMoveAction.putValue(Action.NAME, localization.translateString("Menu.notation.nextMove"));
		deleteVariationAction.putValue(Action.NAME, localization.translateString("Menu.notation.deleteVariation"));
		promoteVariationAction.putValue(Action.NAME, localization.translateString("Menu.notation.promoteVariation"));
		editCommentaryAction.putValue(Action.NAME, localization.translateString("Menu.notation.editCommentary"));

		settingMenu.setText(localization.translateString("Menu.settings"));
		applicationSettingAction.putValue(Action.NAME, localization.translateString("Menu.settings.applicationSettings"));
		reverseDeskAction.putValue(Action.NAME, localization.translateString("Menu.settings.reverseDesk"));
		showDeskCoordinatesAction.putValue(Action.NAME, localization.translateString("Menu.settings.showDeskCoordinates"));
		
		helpMenu.setText(localization.translateString("Menu.help"));
		aboutAction.putValue(Action.NAME, localization.translateString("Menu.help.about"));
		showLogAction.putValue(Action.NAME, localization.translateString("Menu.help.showLog"));
		
		setPgnChooserFileNameFilter(localization);
	}
	
	private void updateMenuItemsEnabled () {
		final RegimeType type = application.getRegimeType();
		
		final boolean isRegimePlay = (type == RegimeType.PLAY);
		final boolean isRegimeAnalysis = (type == RegimeType.ANALYSIS);
		final boolean isRegimeEndingTraining = (type == RegimeType.ENDING_TRAINING);
		final boolean isGameRegime = (isRegimeAnalysis || isRegimePlay || isRegimeEndingTraining);
		
		applicationSettingMenuItem.setEnabled(isGameRegime);
		loadGameMenuItem.setEnabled(isGameRegime);
		saveGameMenuItem.setEnabled(isGameRegime);
		editPositionMenuItem.setEnabled(isGameRegime);
		editFenMenuItem.setEnabled(isGameRegime);
		previousMoveMenuItem.setEnabled(isGameRegime);
		nextMoveMenuItem.setEnabled(isGameRegime);
		deleteVariationMenuItem.setEnabled(isGameRegime);
		promoteVariationMenuItem.setEnabled(isGameRegime);
		editCommentaryMenuItem.setEnabled(isGameRegime);
	}
	
	public JMenu getGameMenu() {
		return gameMenu;
	}

	/**
	 * This method is called when application is closed.
	 */
	public void onClose() {
		searchInfoPanel.destroy();
		regimePlayView.destroy();
		regimeEditPositionView.destroy();
		regimeAnalysisView.destroy();
		regimeEndingTrainingView.destroy();

		application.getApplicationListenerRegistrar().removeHandler(applicationListener);
		application.getLocalizedComponentRegister().removeComponent(this);
		
		application.onClose();
	}
	
	/**
	 * Updates desk orientation in regime PLAY in case of game human x computer.
	 * Desk will be oriented according the human side. 
	 */
	void updateDeskOritntation() {
		final GameSettings gameSettings = application.getSettings().getGameSettings();
		
		if (gameSettings.getGameType() == GameType.PLAY) {
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final int oppositeColor = Color.getOppositeColor(color);
				final SideType ownType = gameSettings.getSideSettings(color).getSideType();
				final SideType oppositeType = gameSettings.getSideSettings(oppositeColor).getSideType();
	
				if (ownType == SideType.HUMAN && oppositeType == SideType.COMPUTER) {
					desk.setOrientation(color);
					return;
				}
			}
		}
	}

	// Actions
	private final  AbstractAction applicationSettingAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			if (ApplicationSettingDialog.showDialog(ApplicationViewImpl.this, application.getSettings())) {
				try {
					application.updateSettings();
				}
				catch (Throwable ex) {
					JOptionPane.showMessageDialog(getMainFrame(), "Cannot update setting. Missing language?", "Error", JOptionPane.ERROR_MESSAGE);
					Logger.logException(ex);
				}
			}
		}
	};
	
	private final AbstractAction newGameAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			final ILocalization localization = application.getLocalization();
			final String message = localization.translateString("Application.newGameAction.confirm.message");
			final String title = localization.translateString("Application.newGameAction.confirm.title");
			final int answer = JOptionPane.showConfirmDialog(getMainFrame(), message, title, JOptionPane.OK_CANCEL_OPTION);
			
			if (answer == JOptionPane.OK_OPTION) {
				if (displayGameSettingsDialog()) {
					application.newGame();
					updateDeskOritntation();					
				}
			}
		}
	};
	
	private final AbstractAction loadGameAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			if (pgnFileChooser.showOpenDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
				final GameEditor gameEditor = application.getActualGameEditor();
				final PgnReader pgn = new PgnReader();
				
				try {
					pgn.readPgnFromFile(pgnFileChooser.getSelectedFile());
					
					final List<Game> gameList = pgn.getGameList();
					final int selectedIndex = GameSelectorDialog.selectGameFromGameList(application, getMainFrame(), gameList, GameSelectorMode.LOAD);
					
					if (selectedIndex >= 0) {
						final Game game = gameList.get(selectedIndex);
						gameEditor.setGame(game);
						
						application.setGameRegime();
					}
				}
				catch (Throwable ex) {
					JOptionPane.showMessageDialog(getMainFrame(), "Cannot read PGN", "Error", JOptionPane.ERROR_MESSAGE);
					Logger.logException(ex);
				}
			}
		}
	};
	
	private final AbstractAction saveGameAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			if (pgnFileChooser.showSaveDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
				try {
					final java.io.File file = pgnFileChooser.getSelectedFile();
					final PgnWriter pgnWriter = new PgnWriter();
					final List<Game> gameList = pgnWriter.getGameList();
					
					if (file.exists()) {
						final PgnReader pgnReader = new PgnReader();
						pgnReader.readPgnFromFile(file);
					
						gameList.addAll(pgnReader.getGameList());
					}
	
					final GameEditor gameEditor = application.getActualGameEditor();
					final Game game = gameEditor.getGame();
	
					if (!gameList.isEmpty()) {
						final int selectedIndex = GameSelectorDialog.selectGameFromGameList(application, getMainFrame(), gameList, GameSelectorMode.OVERWRITE);
						
						if (selectedIndex < 0)
							return;   // User cancel
						
						if (selectedIndex < gameList.size())
							gameList.set(selectedIndex, game);
						else
							gameList.add(game);	
					}
					else
						gameList.add(game);
					
					final FileOutputStream stream = new FileOutputStream(file);

					try {
						pgnWriter.writePgnToStream(stream);
					}
					finally {
						stream.close();
					}
				}
				catch (Throwable ex) {
					JOptionPane.showMessageDialog(getMainFrame(), "Cannot write PGN", "Error", JOptionPane.ERROR_MESSAGE);
					Logger.logException(ex);
				}
			}
		}
	};
	
	private final AbstractAction editFenAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			FenDialog.showDialog(ApplicationViewImpl.this);
		}
	};
	
	private final AbstractAction endingTrainingAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			EndingTrainingDialog.showDialog(ApplicationViewImpl.this);
		}
	};
	
	private final AbstractAction editPositionAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			application.setRegimeType(RegimeType.EDIT_POSITION);
		}
	};

	private final AbstractAction editGameHeaderAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent e) {
			final GameEditor editor = application.getActualGameEditor();
			final GameHeader header = editor.getGame().getHeader();
			
			GameHeaderDialog.showDialog(ApplicationViewImpl.this, header); 
		}
	};

	private final AbstractAction previousMoveAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent e) {
			final GameEditor editor = application.getActualGameEditor();
			final ITreeIterator<IGameNode> iterator = editor.getActualNodeIterator();
			
			if (iterator.hasParent()) {
				iterator.moveParent();
				editor.setActualNodeIterator(iterator);
			}
		}
	};
	
	private final AbstractAction nextMoveAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent e) {
			final GameEditor editor = application.getActualGameEditor();
			final ITreeIterator<IGameNode> iterator = editor.getActualNodeIterator();
			
			if (iterator.hasChild()) {
				iterator.moveFirstChild();
				editor.setActualNodeIterator(iterator);
			}
		}
	};

	private final AbstractAction deleteVariationAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent e) {
			final GameEditor editor = application.getActualGameEditor();
			final Game game = editor.getGame();
			final ITreeIterator<IGameNode> iterator = editor.getActualNodeIterator();
			
			if (!iterator.equals(game.getRootIterator())) {
				final ITreeIterator<IGameNode> parentIterator = iterator.copy();
				parentIterator.moveParent();
				
				editor.removeNode (iterator);
				editor.setActualNodeIterator(parentIterator);
			}
		}
	};

	private final AbstractAction promoteVariationAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent e) {
			final GameEditor editor = application.getActualGameEditor();
			final Game game = editor.getGame();
			final ITreeIterator<IGameNode> iterator = editor.getActualNodeIterator();
			
			if (!iterator.equals(game.getRootIterator())) {
				editor.promoteChild (iterator);
			}
		}
	};
	
	private final AbstractAction editCommentaryAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent e) {
			final GameEditor editor = application.getActualGameEditor();
			final ITreeIterator<IGameNode> iterator = editor.getActualNodeIterator();
			
			if (CommentaryDialog.showDialog(ApplicationViewImpl.this, iterator.getItem())) {
				editor.onNodeChanged(iterator);
			}
		}
	};
	
	private final AbstractAction aboutAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			showAboutDialog();
		}
	};

	private final AbstractAction showLogAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			Logger.showLog (getMainFrame());
		}
	};

	private final AbstractAction reverseDeskAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			if (reverseDeskMenuItem.isSelected())
				desk.setOrientation(Color.BLACK);
			else
				desk.setOrientation(Color.WHITE);
		}
	};

	private final AbstractAction showDeskCoordinatesAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			desk.setShowCoordinates (showDeskCoordinatesMenuItem.isSelected());
		}
	};

	private final IApplicationListener applicationListener = new IApplicationListener() {
		@Override
		public void onRegimeChanged() {
			updateMenuItemsEnabled();
		}
	};

	@Override
	public IApplication getApplication() {
		return application;
	}
	
	private boolean displayGameSettingsDialog() {
		final GameSettings gameSettings = application.getSettings().getGameSettings();
		
		return GameSettingDialog.showDialog(ApplicationViewImpl.this, gameSettings);
	}

	private final AbstractAction gameSettingAction = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			if (displayGameSettingsDialog()) {
				application.setGameRegime();
				updateMenuItemsEnabled();
			}
		}
	};
	
}
