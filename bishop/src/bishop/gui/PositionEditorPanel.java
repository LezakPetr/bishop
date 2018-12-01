package bishop.gui;

import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import bisGui.widgets.MouseButton;
import bisGuiSwing.graphics.AwtImage;
import bishop.base.CastlingRights;
import bishop.base.CastlingType;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.controller.GameEditor;
import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.RegimeType;

@SuppressWarnings("serial")
public class PositionEditorPanel extends JPanel implements IDeskListener, ILocalizedComponent {
	
	private final IApplicationView applicationView;
	private final IApplication application;
	private final IDesk desk;
	private JToggleButton[][] pieceButtonArray;
	private JPanel panelCastlingRights;
	private Checkbox[][] castlingRightCheckboxArray;
	private JLabel labelOnTurn;
	private ColorCombo comboOnTurn;
	private JLabel labelEpFile;
	private JComboBox<String> comboEpFile;
	private Piece selectedPiece;
	private Position position;
	private JPanel panelButtons;
	private JButton buttonClear;
	private JButton buttonInitialize;
	private JButton buttonOk;
	private JButton buttonCancel;
	
	private static final int ICON_WIDTH = 40;
	private static final int ICON_HEIGHT = 40;
	
	private static final Dimension PANEL_ON_TURN_SIZE = new Dimension(250, 25);
	private static final Dimension PANEL_EP_FILE_SIZE = new Dimension(250, 25);
	private static final Dimension PANEL_CASTLING_RIGHTS_SIZE = new Dimension(250, 80);
	
	
	private static final Piece DEFAULT_PIECE = Piece.withColorAndType(Color.WHITE, PieceType.PAWN);
	
	
	public PositionEditorPanel(final IApplicationView applicationView) {
		this.applicationView = applicationView;
		this.application = applicationView.getApplication();
		this.desk = applicationView.getDesk();
		
		final GameEditor gameEditor = application.getActualGameEditor();
		this.position = gameEditor.getActualPositionSource().getPosition().copy();
		
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
		
		showPosition();
	}
	
	private void initializeComponents() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		initializePieceButtons();
		initializeOnTurn();
		initializeEpFile();
		initializeCastlingRights();
		initializeClearButtons();
		initializeTerminationButtons();
		
		this.add(Box.createVerticalGlue());
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
	}

	private void initializeOnTurn() {
		final JPanel panel = new JPanel();
		panel.setMinimumSize(PANEL_ON_TURN_SIZE);
		panel.setPreferredSize(PANEL_ON_TURN_SIZE);
		panel.setMaximumSize(PANEL_ON_TURN_SIZE);

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		this.add (panel);
		
		labelOnTurn = new JLabel();
		
		panel.add(labelOnTurn);		
		panel.add(Box.createHorizontalStrut(5));
		
		comboOnTurn = new ColorCombo();
		panel.add(comboOnTurn);
	}

	private void initializeEpFile() {
		final JPanel panel = new JPanel();
		panel.setMinimumSize(PANEL_EP_FILE_SIZE);
		panel.setPreferredSize(PANEL_EP_FILE_SIZE);
		panel.setMaximumSize(PANEL_EP_FILE_SIZE);

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		this.add (panel);
		
		labelEpFile = new JLabel();
		
		panel.add(labelEpFile);		
		panel.add(Box.createHorizontalStrut(5));
		
		comboEpFile = new JComboBox<String>();
		
		for (int file = File.FIRST; file < File.LAST; file++) {
			comboEpFile.addItem("" + File.toChar(file));
		}
		
		comboEpFile.addItem("-");		
		panel.add(comboEpFile);
	}

	private void initializeCastlingRights() {
		castlingRightCheckboxArray = new Checkbox[Color.LAST][CastlingType.LAST];
		
		panelCastlingRights = new JPanel();
		panelCastlingRights.setMinimumSize(PANEL_CASTLING_RIGHTS_SIZE);
		panelCastlingRights.setPreferredSize(PANEL_CASTLING_RIGHTS_SIZE);
		panelCastlingRights.setMaximumSize(PANEL_CASTLING_RIGHTS_SIZE);
		this.add (panelCastlingRights);
		
		panelCastlingRights.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		panelCastlingRights.setLayout(new GridBagLayout());
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {			
			for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
				final Checkbox checkbox = new Checkbox();
				checkbox.setLabel(getCastlingRightName (color, castlingType));
				
				panelCastlingRights.add(
					checkbox,
					new GridBagConstraints(
						color, castlingType, 1, 1,
						1.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0),
						0, 0
					)
				);
				
				castlingRightCheckboxArray[color][castlingType] = checkbox;
			}
		}
	}

	private String getCastlingRightName(final int color, final int castlingType) {
		final StringWriter stringWritter = new StringWriter();
		final PrintWriter printWritter = new PrintWriter(stringWritter);
		
		Color.write(printWritter, color);
		CastlingType.write(printWritter, castlingType);
		
		printWritter.flush();
		return stringWritter.toString();
	}

	private void initializeClearButtons() {
		final JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
		this.add(panelButtons);
		
		buttonClear = new JButton();
		buttonClear.addActionListener(buttonClear_ActionListener);
		panelButtons.add(buttonClear);
		
		buttonInitialize = new JButton();
		buttonInitialize.addActionListener(buttonInitialize_ActionListener);
		panelButtons.add(buttonInitialize);		
	}
	
	private void initializeTerminationButtons() {
		panelButtons = new JPanel();
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
		this.add(panelButtons);
		
		buttonOk = new JButton();
		buttonOk.addActionListener(buttonOk_ActionListener);
		panelButtons.add(buttonOk);
		
		buttonCancel = new JButton();
		buttonCancel.addActionListener(buttonCancel_ActionListener);
		panelButtons.add(buttonCancel);
	}

	private void initializePieceButtons() {
		pieceButtonArray = new JToggleButton[Color.LAST][PieceType.LAST];
		
		final IPieceSet pieceSet = applicationView.getPieceSet();
		final double scale = Utils.getIconScale(pieceSet.getPieceSize(), ICON_WIDTH, ICON_HEIGHT);
		final RasterPieceSet rasterPieceSet = pieceSet.renderScaledPieceSet(scale);
		
		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
			final JPanel horizontalPanel = new JPanel();
			horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.X_AXIS));
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				final AwtImage origImage = (AwtImage) rasterPieceSet.getPieceImage(color, pieceType);
				final Icon icon = new ImageIcon(origImage.getImage());
				
				final JToggleButton button = new JToggleButton();
				button.setIcon(icon);
				button.addActionListener(button_ActionListener);
				
				horizontalPanel.add(button);
				
				pieceButtonArray[color][pieceType] = button;
			}
			
			this.add(horizontalPanel);
		}
		
		selectedPiece = DEFAULT_PIECE;
		pieceButtonArray[DEFAULT_PIECE.getColor()][DEFAULT_PIECE.getPieceType()].setSelected(true);
	}
	
	private ActionListener button_ActionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent event) {
			final JToggleButton eventButton = (JToggleButton) event.getSource();
			
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
					JToggleButton currentButton = pieceButtonArray[color][pieceType];
					
					if (currentButton == eventButton) {
						currentButton.setSelected(true);
						
						selectedPiece = Piece.withColorAndType(color, pieceType);
					}
					else {
						currentButton.setSelected(false);
					}
				}
			}
		}
	};
	
	/**
	 * This method is called when user clicks on some square.
	 * @param square coordinate of square
	 * @param button clicked button
	 */
	public void onSquareClick (final int square, final MouseButton button) {
		boolean changed = false;
		Piece piece = null;
		
		switch (button) {
			case LEFT:
				piece = selectedPiece;
				changed = true;
				break;
				
			case RIGHT:
				piece = null;
				changed = true;
				break;
			
			default:
				return;
		}
		
		if (changed) {
			position.setSquareContent(square, piece);
			showPosition();
		}
	}
	
	/**
	 * This method is called when user starts dragging from some square.
	 * @param square source square
	 * @param button dragging button
	 */
	public void onDrag (final int square, final MouseButton button) {
		if (button == MouseButton.LEFT && position.getSquareContent(square) != null) {
			desk.startDragging(square);
		}
	}
	
	/**
	 * This method is called when user drops object to some square.
	 * @param beginSquare begin square of the dragging
	 * @param targetSquare target square of the dragging
	 */
	public void onDrop (final int beginSquare, final int targetSquare) {
		final Piece piece = position.getSquareContent(beginSquare);
		
		position.setSquareContent(beginSquare, null);
		position.setSquareContent(targetSquare, piece);
		
		showPosition();
		desk.stopDragging();
	}

	private void showPosition() {
		final IDesk desk = applicationView.getDesk();
		desk.changePosition(position);
		
		comboOnTurn.setSelectedColor(position.getOnTurn());

		final int epFile = position.getEpFile();
		comboEpFile.setSelectedIndex((epFile == File.NONE) ? File.LAST : epFile);
		
		// Castling rights
		final CastlingRights castlingRights = position.getCastlingRights();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {			
			for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
				final Checkbox checkbox = castlingRightCheckboxArray[color][castlingType];
				checkbox.setState(castlingRights.isRight(color, castlingType));
			}
		}
	}
	
	private boolean storePosition() {
		storeCastlingRights();
		storeOnTurn();
		storeEpFile();
		position.refreshCachedData();
		
		final MessagePositionValidator validator = new MessagePositionValidator();
		validator.setPosition(position);
		
		if (validator.checkPosition()) {
			final GameEditor gameEditor = application.getActualGameEditor();
			gameEditor.newGame(position);
			
			return true;
		}
		else {
			final ILocalization localization = application.getLocalization();
			final String message = validator.getMessage(localization);
			final String title = localization.translateString("PositionEditorPanel.invalidPosition.title");
			
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			
			return false;
		}
	}

	private void storeOnTurn() {
		final int color = comboOnTurn.getSelectedColor();
		
		if (!Color.isValid(color))
			throw new RuntimeException("Unknown color on turn selected");
		
		position.setOnTurn(color);
	}

	private void storeEpFile() {
		final int epFile = comboEpFile.getSelectedIndex() - File.FIRST;
		position.setEpFile ((File.isValid(epFile)) ? epFile : File.NONE);
	}

	private void storeCastlingRights() {
		final CastlingRights castlingRights = position.getCastlingRights();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int type = CastlingType.FIRST; type < CastlingType.LAST; type++) {
				final boolean right = castlingRightCheckboxArray[color][type].getState();
				castlingRights.setRight(color, type, right);
			}
		}
	}

	private ActionListener buttonOk_ActionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent event) {
			if (storePosition()) {
				application.popRegimeType();
			}
		}
	};
	
	private ActionListener buttonCancel_ActionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent event) {
			application.setRegimeType(RegimeType.PLAY);
		}
	};
	
	private ActionListener buttonClear_ActionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent event) {
			position.clearPosition();
			showPosition();
		}
	};

	private ActionListener buttonInitialize_ActionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent event) {
			position.setInitialPosition();
			showPosition();
		}
	};

	public void updateLanguage(final ILocalization localization) {
		labelOnTurn.setText(localization.translateString("PositionEditorPanel.labelOnTurn.text"));
		labelEpFile.setText(localization.translateString("PositionEditorPanel.labelEpFile.text"));
		buttonOk.setText(localization.translateString(LocalizedStrings.BUTTON_OK_KEY));
		buttonCancel.setText(localization.translateString(LocalizedStrings.BUTTON_CANCEL_KEY));
		buttonInitialize.setText(localization.translateString("PositionEditorPanel.buttonInitialize.text"));
		buttonClear.setText(localization.translateString("PositionEditorPanel.buttonClear.text"));
		
		final TitledBorder castlingRightsBorder = (TitledBorder) panelCastlingRights.getBorder();
		castlingRightsBorder.setTitle(localization.translateString("PositionEditorPanel.panelCastlingRights.title"));
		
		comboOnTurn.updateLanguage (localization);
	}
	
}
