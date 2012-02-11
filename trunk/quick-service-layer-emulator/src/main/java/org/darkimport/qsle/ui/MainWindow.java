/**
 * 
 */
package org.darkimport.qsle.ui;

import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.qsle.constants.PropertiesConstants;
import org.darkimport.qsle.constants.ResourceConstants;
import org.darkimport.qsle.services.PassThroughEmulator;
import org.darkimport.qsle.services.RepeaterEmulator;
import org.darkimport.qsle.services.StartStoppable;
import org.darkimport.qsle.services.StartStoppableConfigWatcher;
import org.darkimport.qsle.util.ConfigHelper;
import org.javabuilders.BuildResult;
import org.javabuilders.annotations.DoInBackground;
import org.javabuilders.event.BackgroundEvent;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * @author user
 * 
 */
public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 3232281984252128182L;
	private static final Log		log					= LogFactory.getLog(MainWindow.class);

	@SuppressWarnings("unused")
	private final BuildResult		result;

	private final StartStoppable	authEmulator		= new StartStoppableConfigWatcher(new RepeaterEmulator(),
																PropertiesConstants.GROUP_AUTH_EMULATOR);
	private final StartStoppable	preAuthEmulator		= new StartStoppableConfigWatcher(new RepeaterEmulator(),
																PropertiesConstants.GROUP_PRE_AUTH_EMULATOR);
	private final StartStoppable	meshEmulator		= new StartStoppableConfigWatcher(new PassThroughEmulator(),
																PropertiesConstants.GROUP_MESH_EMULATOR);

	public MainWindow() {
		result = SwingJavaBuilder.build(this);
		addWindowListener(new WindowAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent
			 * )
			 */
			@Override
			public void windowClosed(final WindowEvent e) {
				exit();
			}
		});

		addComponentListener(new ComponentAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ComponentAdapter#componentMoved(java.awt.event
			 * .ComponentEvent)
			 */
			@Override
			public void componentMoved(final ComponentEvent e) {
				final Properties p = ConfigHelper.getGroup(PropertiesConstants.GROUP_GENERAL);
				p.setProperty(PropertiesConstants.WINDOW_X_POSITION, String.valueOf(getLocation().x));
				p.setProperty(PropertiesConstants.WINDOW_Y_POSITION, String.valueOf(getLocation().y));
				ConfigHelper.updateGroup(PropertiesConstants.GROUP_GENERAL, p);
				ConfigHelper.saveConfiguration();
			}

		});

		final Point p = getLocation();
		Properties properties = null;
		if (ConfigHelper.isInitialized()) {
			properties = ConfigHelper.getGroup(PropertiesConstants.GROUP_GENERAL);
			p.x = new Integer(properties.getProperty(PropertiesConstants.WINDOW_X_POSITION, String.valueOf(p.x)));
			p.y = new Integer(properties.getProperty(PropertiesConstants.WINDOW_Y_POSITION, String.valueOf(p.y)));
			setLocation(p);
		}
	}

	public void onFileOptions() {
		try {
			final JDialog jDialog = new OptionsWindow(this);

			jDialog.setVisible(true);
		} catch (final Exception e) {
			log.warn("An error occurred while opening the Options dialog.", e);
			if (JOptionPane.showConfirmDialog(this,
					SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_OPTIONS_STARTUP_MESSAGE),
					SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_OPTIONS_STARTUP_TITLE),
					JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				exit();
			}
		}
	}

	public void exit() {
		if (authEmulator.isStarted()) {
			authEmulator.stop();
		}
		if (preAuthEmulator.isStarted()) {
			preAuthEmulator.stop();
		}
		if (meshEmulator.isStarted()) {
			meshEmulator.stop();
		}
		System.exit(0);
	}

	public void startPreAuth(final JButton button) {
		try {
			button.setText(toggleService(preAuthEmulator, button.getText(),
					ConfigHelper.getGroup(PropertiesConstants.GROUP_PRE_AUTH_EMULATOR)));
		} catch (final Exception e) {
			log.warn("Unable to start Pre Auth Service.", e);
			JOptionPane.showMessageDialog(this, MessageFormat.format(
					SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_SERVICE_STARTUP_PREAUTH),
					e.getLocalizedMessage()),
					SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_SERVICE_STARTUP_TITLE),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void startAuth(final JButton button) {
		try {
			button.setText(toggleService(authEmulator, button.getText(),
					ConfigHelper.getGroup(PropertiesConstants.GROUP_AUTH_EMULATOR)));
		} catch (final Exception e) {
			log.warn("Unable to start Auth Service.", e);
			JOptionPane.showMessageDialog(
					this,
					MessageFormat.format(
							SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_SERVICE_STARTUP_AUTH),
							e.getLocalizedMessage()),
					SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_SERVICE_STARTUP_TITLE),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void startMesh(final JButton button) {
		try {
			button.setText(toggleService(meshEmulator, button.getText(),
					ConfigHelper.getGroup(PropertiesConstants.GROUP_MESH_EMULATOR)));
		} catch (final Exception e) {
			log.warn("Unable to start Mesh Service.", e);
			JOptionPane.showMessageDialog(
					this,
					MessageFormat.format(
							SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_SERVICE_STARTUP_MESH),
							e.getLocalizedMessage()),
					SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_SERVICE_STARTUP_TITLE),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @param button
	 * @param port
	 * @param conversationPaths
	 * @throws Exception
	 */
	private String toggleService(final StartStoppable serviceEmulator, final String buttonState,
			final Properties properties) throws Exception {
		final String serviceStartString = SwingJavaBuilder.getConfig().getResource(
				ResourceConstants.BUTTON_SERVICESTART);
		final String serviceStopString = SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_SERVICESTOP);
		if (buttonState.equals(serviceStartString)) {
			serviceEmulator.start(properties);
			return serviceStopString;
		}
		serviceEmulator.stop();
		return serviceStartString;
	}

	public void generateKeyStore() {
		// TODO
	}

	public void generateTrustStore() {
		// TODO
	}

	@DoInBackground(blocking = false)
	public void downloadMeshes(final BackgroundEvent backgroundEvent) {
		// TODO
		for (long i = 0; i < 1000000000000L; i++) {
			if (i % 1000000 == 0) {
				log.info("Ping!");
			}
		}

	}

	public void fixRoutes() {

	}

	/**
	 * We're going to use the route command instead.
	 */
	@Deprecated
	public void fixHosts() {
		/*
		 * // Parse the hosts file. HostsEditor hostsEditor; try { hostsEditor =
		 * new HostsEditor(); } catch (IOException e) {
		 * log.warn("Unable to open the hosts file.", e); JOptionPane
		 * .showMessageDialog( this, MessageFormat .format(SwingJavaBuilder
		 * .getConfig() .getResource(
		 * ResourceConstants.ERROR_HOSTS_EDITOR_OPEN_MESSAGE),
		 * e.getLocalizedMessage()), SwingJavaBuilder .getConfig() .getResource(
		 * ResourceConstants.ERROR_HOSTS_EDITOR_OPEN_TITLE),
		 * JOptionPane.ERROR_MESSAGE); throw new RuntimeException(e); } //
		 * Remove previously added host entries that we added (they will be //
		 * marked) String[] marks = new String[] { PREAUTH_MARK, AUTH_MARK,
		 * MESH_MARK }; for (String mark : marks) {
		 * hostsEditor.removeMarkedHosts(mark); } // Overwrite existing hosts
		 * file. try { hostsEditor.updateHosts(); } catch (IOException e) {
		 * log.warn("Unable to write to the hosts file.", e); JOptionPane
		 * .showMessageDialog( this, MessageFormat .format(SwingJavaBuilder
		 * .getConfig() .getResource(
		 * ResourceConstants.ERROR_HOSTS_EDITOR_WRITE_MESSAGE),
		 * e.getLocalizedMessage()), SwingJavaBuilder .getConfig() .getResource(
		 * ResourceConstants.ERROR_HOSTS_EDITOR_WRITE_TITLE),
		 * JOptionPane.ERROR_MESSAGE); throw new RuntimeException(e); }
		 */
	}
}
