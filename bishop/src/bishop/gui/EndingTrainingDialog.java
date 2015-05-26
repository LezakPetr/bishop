package bishop.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JSlider;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.Utils;
import bishop.engine.TablebasePositionEvaluator;

@SuppressWarnings("serial")
public class EndingTrainingDialog extends OkCancelDialog {

	private JLabel labelMaterial;
	private MaterialHashCombo comboMaterial;
	
	private JLabel labelSide;
	private ColorCombo comboSide;

	private JLabel labelDifficulty;
	private JSlider sliderDifficulty;

	private static final Dimension DIALOG_SIZE = new Dimension(300, 150);
	

	public EndingTrainingDialog (final IApplicationView applicationView) {
		super (applicationView);
		
		initialize();
	}
	
	private void initialize() {
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
		
		final TablebasePositionEvaluator tablebaseEvaluator = application.getSearchResources().getTablebasePositionEvaluator();
		comboMaterial.initialize(tablebaseEvaluator.getMaterialHashSet());
		
		comboSide.setSelectedColor(Color.WHITE);
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
		
		super.destroy();
	}
	
	protected void initializeComponents() {
		this.setModal(true);
		this.setSize(DIALOG_SIZE);
		
		Utils.centerWindow(this);

		contentPanel.setLayout(new GridBagLayout());
		
		labelMaterial = new JLabel();
		
		contentPanel.add(
			labelMaterial,
			new GridBagConstraints(
				0, 0, 1, 1,
				0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);

		comboMaterial = new MaterialHashCombo();
		
		contentPanel.add(
			comboMaterial,
			new GridBagConstraints(
				1, 0, 1, 1,
				1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);

		labelSide = new JLabel();
		
		contentPanel.add(
			labelSide,
			new GridBagConstraints(
				0, 1, 1, 1,
				0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
		
		comboSide = new ColorCombo();
		
		contentPanel.add(
			comboSide,
			new GridBagConstraints(
				1, 1, 1, 1,
				1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
		
		labelDifficulty = new JLabel();
		
		contentPanel.add(
			labelDifficulty,
			new GridBagConstraints(
				0, 2, 1, 1,
				0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
		
		final int difficulty = (IApplication.MIN_DIFFICULTY + IApplication.MAX_DIFFICULTY) / 2;
		sliderDifficulty = new JSlider(JSlider.HORIZONTAL, IApplication.MIN_DIFFICULTY, IApplication.MAX_DIFFICULTY, difficulty);
		
		contentPanel.add(
			sliderDifficulty,
			new GridBagConstraints(
				1, 2, 1, 1,
				1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
	}
	
	public static void showDialog(final IApplicationView applicationView) {
		final EndingTrainingDialog dialog = new EndingTrainingDialog(applicationView);
		
		dialog.setVisible(true);
		dialog.destroy();
	}
	
	@Override
	public void updateLanguage(final ILocalization localization) {
		this.setTitle(localization.translateString("EndingTrainingDialog.title"));
		labelMaterial.setText(localization.translateString("EndingTrainingDialog.labelMaterial"));
		labelSide.setText(localization.translateString("EndingTrainingDialog.labelSide"));
		labelDifficulty.setText(localization.translateString("EndingTrainingDialog.labelDifficulty"));
		
		comboSide.updateLanguage(localization);
	}

	@Override
	protected void onOk() {
		final MaterialHash materialHash = comboMaterial.getSelectedMaterialHash().copy();
		materialHash.setOnTurn(comboSide.getSelectedColor());
		
		final int difficulty = sliderDifficulty.getValue();
		application.endingTraining(materialHash, difficulty);
		
		EndingTrainingDialog.this.dispose();
	}

	@Override
	protected void onCancel() {
		EndingTrainingDialog.this.dispose();
	}

}
