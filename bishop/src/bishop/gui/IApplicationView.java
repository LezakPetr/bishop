package bishop.gui;

import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import bishop.controller.IApplication;


public interface IApplicationView {
	
	public IApplication getApplication();
	
	/**
	 * Returns piece set of the application.
	 * @return piece set
	 */
	public IPieceSet getPieceSet();
	
	/**
	 * Returns desk of the application.
	 * @return desk
	 */
	public IDesk getDesk();
	
	/**
	 * Returns main frame of the application.
	 * @return main frame
	 */
	public Frame getMainFrame();
	
	/**
	 * Returns application menu bar.
	 * @return menu bar
	 */
	public JMenuBar getApplicationMenuBar();
	
	/**
	 * Returns game menu.
	 * @return game menu
	 */
	public JMenu getGameMenu();
	
	/**
	 * Sets regime-dependent component.
	 * @param component regime-dependent component or null to unset
	 */
	public void setRegimeComponent (final JComponent component);
}
