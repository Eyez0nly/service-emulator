/**
 * 
 */
package org.darkimport.qsle;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author user
 * 
 */
public class JCheckBoxTest extends JFrame {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -201079888938555589L;
	private static final Log	log					= LogFactory.getLog(JCheckBoxTest.class);
	private final JCheckBox		checkBox			= new JCheckBox();
	private final JButton		button				= new JButton("Don't press this button again.");

	public JCheckBoxTest() {
		getContentPane().setLayout(new FlowLayout());
		checkBox.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				log.info(checkBox.isSelected());
			}
		});
		getContentPane().add(checkBox);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				log.info(checkBox.isSelected());
			}
		});
		getContentPane().add(button);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(500, 500);
	}

	public static void main(final String args[]) {
		final JCheckBoxTest jCheckBoxTest = new JCheckBoxTest();
		jCheckBoxTest.setVisible(true);
	}
}
