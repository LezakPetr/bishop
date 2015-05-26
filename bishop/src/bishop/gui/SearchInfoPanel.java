package bishop.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import bishop.base.MoveList;
import bishop.controller.IApplication;
import bishop.controller.ILocalization;
import bishop.controller.ILocalizedComponent;
import bishop.controller.RegimeType;
import bishop.engine.Evaluation;
import bishop.engine.ISearchEngine;
import bishop.engine.ISearchManager;
import bishop.engine.ISearchManagerHandler;
import bishop.engine.SearchInfo;

@SuppressWarnings("serial")
public class SearchInfoPanel extends JPanel implements ISearchManagerHandler, ILocalizedComponent {
	
	private final IApplication application;
	
	private JLabel labelPrincipalVariation;
	private JTextField fieldPrincipalVariation;

	private JLabel labelEvaluation;
	private JTextField fieldEvaluation;

	private JLabel labelHorizon;
	private JTextField fieldHorizon;
	
	private JLabel labelNodeCount;
	private JTextField fieldNodeCount;

	private JLabel labelElapsedTime;
	private JTextField fieldElapsedTime;

	private JLabel labelNodesPerSecond;
	private JTextField fieldNodesPerSecond;
	
	public SearchInfoPanel(final IApplicationView applicationView) {
		this.application = applicationView.getApplication();
		
		initializeComponents();
		application.getLocalizedComponentRegister().addComponent(this);
	}
	
	public void destroy() {
		application.getLocalizedComponentRegister().removeComponent(this);
	}
	
	private void initializeComponents() {
		this.setLayout(new GridBagLayout());
		
		// Principal variation
		labelPrincipalVariation = new JLabel();
		
		this.add (
			labelPrincipalVariation,
			new GridBagConstraints(
				0, 0, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 5, 0, 5),
				0, 0
			)
		);

		fieldPrincipalVariation = new JTextField();
		fieldPrincipalVariation.setEditable(false);
		
		fieldPrincipalVariation.setPreferredSize(new Dimension(100, 25));
		
		this.add (
			fieldPrincipalVariation,
			new GridBagConstraints(
				1, 0, 5, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);

		// Evaluation
		labelEvaluation = new JLabel();
		
		this.add (
			labelEvaluation,
			new GridBagConstraints(
				0, 1, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 5, 0, 5),
				0, 0
			)
		);

		fieldEvaluation = new JTextField();
		fieldEvaluation.setEditable(false);
		
		fieldEvaluation.setPreferredSize(new Dimension(50, 25));
		
		this.add (
			fieldEvaluation,
			new GridBagConstraints(
				1, 1, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);

		// Horizon
		labelHorizon = new JLabel();
		
		this.add (
			labelHorizon,
			new GridBagConstraints(
				4, 1, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 5, 0, 5),
				0, 0
			)
		);

		fieldHorizon = new JTextField();
		fieldHorizon.setEditable(false);
		
		fieldHorizon.setPreferredSize(new Dimension(50, 25));
		
		this.add (
			fieldHorizon,
			new GridBagConstraints(
				5, 1, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);

		// Node count
		labelNodeCount = new JLabel();
		
		this.add (
			labelNodeCount,
			new GridBagConstraints(
				0, 2, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 5, 0, 5),
				0, 0
			)
		);

		fieldNodeCount = new JTextField();
		fieldNodeCount.setEditable(false);
		
		fieldNodeCount.setPreferredSize(new Dimension(50, 25));
		
		this.add (
			fieldNodeCount,
			new GridBagConstraints(
				1, 2, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);

		// Elapsed time
		labelElapsedTime = new JLabel();
		
		this.add (
			labelElapsedTime,
			new GridBagConstraints(
				2, 2, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 5, 0, 5),
				0, 0
			)
		);

		fieldElapsedTime = new JTextField();
		fieldElapsedTime.setEditable(false);
		
		fieldElapsedTime.setPreferredSize(new Dimension(50, 25));
		
		this.add (
			fieldElapsedTime,
			new GridBagConstraints(
				3, 2, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);

		// Elapsed time
		labelNodesPerSecond = new JLabel();
		
		this.add (
			labelNodesPerSecond,
			new GridBagConstraints(
				4, 2, 1, 1,
				0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 5, 0, 5),
				0, 0
			)
		);

		fieldNodesPerSecond = new JTextField();
		fieldNodesPerSecond.setEditable(false);
		
		fieldNodesPerSecond.setPreferredSize(new Dimension(50, 25));
		
		this.add (
			fieldNodesPerSecond,
			new GridBagConstraints(
				5, 2, 1, 1,
				1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0),
				0, 0
			)
		);
	}

	/**
	 * This method is called when search is complete.
	 * @param manager search manager
	 */
	public void onSearchComplete (final ISearchManager manager) {
	}
	
	private void updateData(final SearchInfo info) {
		final MoveList principalVariation = info.getPrincipalVariation();
		final String principalVariationStr;
		
		if (application.getRegimeType() == RegimeType.PLAY && principalVariation.getSize() > 0)
			principalVariationStr = principalVariation.get(0).toString();
		else
			principalVariationStr = principalVariation.toString();
		
		fieldPrincipalVariation.setText(principalVariationStr);
		
		fieldEvaluation.setText(Evaluation.toString(info.getEvaluation()));
		fieldHorizon.setText(Integer.toString(info.getHorizon() / ISearchEngine.HORIZON_GRANULARITY));
		
		final long elapsedTime = info.getElapsedTime();
		fieldElapsedTime.setText(Long.toString(elapsedTime / 1000));
		
		final long nodeCount = info.getNodeCount();
		fieldNodeCount.setText(Long.toString(nodeCount));
		
		final long nodesPerSecond = (elapsedTime > 0) ? (1000*nodeCount / elapsedTime) : 0;
		fieldNodesPerSecond.setText(Long.toString(nodesPerSecond));		
	}
	
	/**
	 * This method is called when search info is updated.
	 * @param info updates search info
	 */
	public void onSearchInfoUpdate (final SearchInfo info) {
		final Runnable runnable = new Runnable() {
			public void run() {
				updateData(info);
			}
		};
		
		SwingUtilities.invokeLater(runnable);
	}

	public void updateLanguage(final ILocalization localization) {
		labelPrincipalVariation.setText(localization.translateString("SearchInfoPanel.labelPrincipalVariation.text"));
		labelEvaluation.setText(localization.translateString("SearchInfoPanel.labelEvaluation.text"));
		labelHorizon.setText(localization.translateString("SearchInfoPanel.labelHorizon.text"));
		labelNodeCount.setText(localization.translateString("SearchInfoPanel.labelNodeCount.text"));
		labelElapsedTime.setText(localization.translateString("SearchInfoPanel.labelElapsedTime.text"));
		labelNodesPerSecond.setText(localization.translateString("SearchInfoPanel.labelNodesPerSecond.text"));
	}

}
