package bishop.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bishop.base.GameHeader;
import bishop.base.GameResult;
import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;

@SuppressWarnings("serial")
public class GameHeaderPanel extends JPanel implements ILocalizedComponent {
	
	private IApplication application;
	
	private JLabel labelEvent;
	private JTextField fieldEvent;
	private JLabel labelSite;
	private JTextField fieldSite;
	private JLabel labelDate;
	private JTextField fieldDate;
	private JLabel labelRound;
	private JTextField fieldRound;
	private JLabel labelWhite;
	private JTextField fieldWhite;
	private JLabel labelBlack;
	private JTextField fieldBlack;
	private JLabel labelResult;
	private JComboBox<String> comboResult;
	
	
	private static class ResultItem {
		private GameResult result;
		private String key;
		
		public ResultItem (final GameResult result, final String key) {
			this.result = result;
			this.key = key;
		}
		
		public GameResult getResult() {
			return result;
		}
		
		public String getKey() {
			return key;
		}
	}
	
	private static final ResultItem[] RESULT_ITEM_LIST = {
		new ResultItem(GameResult.GAME_IN_PROGRESS, "GameResult.gameInProgress"),
		new ResultItem(GameResult.WHITE_WINS, "GameResult.whiteWins"),
		new ResultItem(GameResult.BLACK_WINS, "GameResult.blackWins"),
		new ResultItem(GameResult.DRAW, "GameResult.draw")
	};
	
	public GameHeaderPanel(final IApplication application) {
		this.application = application;
		
		initializeComponents();
		
		application.getLocalizedComponentRegister().addComponent(this);
	}

	private JLabel createLabel (final int row) {
		final JLabel label = new JLabel();
		
		this.add(
			label,
			new GridBagConstraints(
				0, row,
				1, 1,
				0.0, 1.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 5, 5, 5),
				0, 0
			)
		);
		
		return label;
	}

	private JTextField createTextField (final int row) {
		final JTextField field = new JTextField();
		
		this.add(
			field,
			new GridBagConstraints(
				1, row,
				1, 1,
				0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 5),
				0, 0
			)
		);
		
		return field;
	}

	private JComboBox<String> createResultComboBox (final int row) {
		final JComboBox<String> combo = new JComboBox<String>();
		
		this.add(
			combo,
			new GridBagConstraints(
				1, row,
				1, 1,
				1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 5),
				0, 0
			)
		);
		
		return combo;
	}

	private void initializeComponents() {
		this.setLayout(new GridBagLayout());
		
		labelEvent = createLabel(0);
		fieldEvent = createTextField(0);
		
		labelSite = createLabel(1);
		fieldSite = createTextField(1);

		labelDate = createLabel(2);
		fieldDate = createTextField(2);

		labelRound = createLabel(3);
		fieldRound = createTextField(3);

		labelWhite = createLabel(4);
		fieldWhite = createTextField(4);

		labelBlack = createLabel(5);
		fieldBlack = createTextField(5);

		labelResult = createLabel(6);
		comboResult = createResultComboBox (6);
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
	}
	
	public void loadHeader (final GameHeader header) {
		fieldEvent.setText(header.getEvent());
		fieldSite.setText(header.getSite());
		fieldDate.setText(header.getDate());
		fieldRound.setText(header.getRound());
		fieldWhite.setText(header.getWhite());
		fieldBlack.setText(header.getBlack());
		
		final GameResult result = header.getResult();
		
		for (int i = 0; i < RESULT_ITEM_LIST.length; i++) {
			if (RESULT_ITEM_LIST[i].getResult() == result) {
				comboResult.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public void saveHeader (final GameHeader header) {
		header.setEvent(fieldEvent.getText());
		header.setSite(fieldSite.getText());
		header.setDate(fieldDate.getText());
		header.setRound(fieldRound.getText());
		header.setWhite(fieldWhite.getText());
		header.setBlack(fieldBlack.getText());
		
		final int resultIndex = comboResult.getSelectedIndex();
		header.setResult(RESULT_ITEM_LIST[resultIndex].getResult());
	}
	
	private void updateComboResult(final ILocalization localization) {
		final int selectedIndex = comboResult.getSelectedIndex();
		
		comboResult.removeAllItems();
		
		for (ResultItem item: RESULT_ITEM_LIST) {
			final String text = localization.translateString(item.getKey());
			comboResult.addItem(text);
		}
		
		comboResult.setSelectedIndex(selectedIndex);
	}

	public void updateLanguage(final ILocalization localization) {
		labelEvent.setText(localization.translateString("GameHeaderPanel.event"));
		labelSite.setText(localization.translateString("GameHeaderPanel.site"));
		labelDate.setText(localization.translateString("GameHeaderPanel.date"));
		labelRound.setText(localization.translateString("GameHeaderPanel.round"));
		labelWhite.setText(localization.translateString("GameHeaderPanel.white"));
		labelBlack.setText(localization.translateString("GameHeaderPanel.black"));
		labelResult.setText(localization.translateString("GameHeaderPanel.result"));
		
		updateComboResult(localization);
	}
}
