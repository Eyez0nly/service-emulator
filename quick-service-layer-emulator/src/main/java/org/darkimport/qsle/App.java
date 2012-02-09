/**
 * 
 */
package org.darkimport.qsle;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.darkimport.qsle.ui.MainWindow;
import org.darkimport.qsle.util.ConfigHelper;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * Starts up the GUI used to control the emulated services.
 * 
 * @author user
 * 
 */
public class App {

	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception {
		System.setProperty("javax.net.ssl.keyStore", "mySrvKeystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		if (args.length > 0) {
			ConfigHelper.initialize(args[0]);
		} else {
			ConfigHelper.initialize();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// activate internationalization
				SwingJavaBuilder.getConfig().addResourceBundle("MainWindow");
				SwingJavaBuilder.getConfig().addResourceBundle("OptionsWindow");
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					new MainWindow().setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
