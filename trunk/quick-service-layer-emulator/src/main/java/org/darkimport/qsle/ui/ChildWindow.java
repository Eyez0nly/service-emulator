/**
 * 
 */
package org.darkimport.qsle.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * @author user
 * 
 */
public abstract class ChildWindow extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1634677144592306116L;

	public ChildWindow(JFrame mainWindow) {
		super(mainWindow);
	}

	/**
	 * @param mainWindow
	 */
	public void setInitialPosition(JFrame mainWindow) {
		// Set the location of the options window such that it's not off
		// screen!
		Point p = mainWindow.getLocation();
		Rectangle r = getBounds();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Point finalP = new Point();
		if (p.x + r.width > d.width) {
			finalP.x = d.width - r.width;
		} else {
			finalP.x = p.x;
		}

		if (p.y + r.height > d.height) {
			finalP.y = d.height - r.height;
		} else {
			finalP.y = p.y;
		}
		setLocation(finalP);
	}

}
