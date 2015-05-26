package bishop.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import bishop.base.Game;
import bishop.base.GameHeader;
import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.Utils;

@SuppressWarnings("serial")
public class GameSelectorDialog extends JDialog implements ILocalizedComponent {
	private final IApplication application;
	private final List<GameHeader> gameList;
	private JTable gameTable;
	private GameSelectorTableModel gameTableModel;
	private JScrollPane scrollPane;
	private OkCancelPanel panelButtons;
	private JPanel mainPanel;
	private int selectedIndex;

	
	public GameSelectorDialog (final IApplication application, final Frame owner, final List<GameHeader> gameList) {
		super (owner);
		
		this.application = application;
		this.gameList = gameList;
		this.selectedIndex = -1;
		
		initializeComponents();
		
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	private void initializeComponents() {
		this.setSize(300, 500);
		Utils.centerWindow(this);
		
		this.setModal(true);
		
		gameTableModel = new GameSelectorTableModel(application, gameList);
		gameTable = new JTable(gameTableModel);
		
		scrollPane = new JScrollPane(gameTable);
		panelButtons = new OkCancelPanel(application, buttonOk_ActionListener, buttonCancel_ActionListener);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(scrollPane);
		mainPanel.add(panelButtons);
		
		this.getContentPane().add(mainPanel);
	}
	
	public void destroy() {
		panelButtons.destroy();
		gameTableModel.destroy();
		
		application.getLocalizedComponentRegister().removeComponent(this);
	}
	
	private Action buttonOk_ActionListener = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			selectedIndex = gameTable.getSelectedRow();
			
			dispose();
		}		
	};
	
	private Action buttonCancel_ActionListener = new AbstractAction() {
		public void actionPerformed(final ActionEvent event) {
			dispose();
		}		
	};
	
	public void updateLanguage(final ILocalization localization) {
		this.setTitle(localization.translateString("GameSelectorDialog.title"));
	}
	
	public static int selectGameFromHeaderList (final IApplication application, final Frame owner, final List<GameHeader> gameHeaderList) {
		final GameSelectorDialog dialog = new GameSelectorDialog(application, owner, gameHeaderList);
		dialog.setVisible(true);
		dialog.destroy();
		
		return dialog.selectedIndex;
	}
	
	public static int selectGameFromGameList (final IApplication application, final Frame owner, final List<Game> gameList) {
		final List<GameHeader> headerList = new ArrayList<GameHeader>(gameList.size());
		
		for (Game game: gameList)
			headerList.add(game.getHeader());
		
		return selectGameFromHeaderList (application, owner, headerList);
	}

}
