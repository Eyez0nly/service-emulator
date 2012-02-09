/**
 * 
 */
package org.darkimport.qsle.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.qsle.constants.PropertiesConstants;
import org.darkimport.qsle.constants.ResourceConstants;
import org.darkimport.qsle.util.ConfigHelper;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * @author user
 * 
 */
public class OptionsWindow extends ChildWindow {
	private static final String		TXT_PRE_AUTH_REPLAY_FILES	= "txtPreAuthReplayFiles";
	/**
	 * 
	 */
	private static final long		serialVersionUID			= -33449310603092351L;
	private static final Log		log							= LogFactory.getLog(OptionsWindow.class);
	private static final String		TXT_AUTH_REPLAY_FILES		= "txtAuthReplayFiles";

	private final BuildResult		result;
	private Map<String, Properties>	config;

	public OptionsWindow(final JFrame mainWindow) {
		super(mainWindow);
		if (ConfigHelper.isInitialized()) {
			config = ConfigHelper.getPropertiesByGroup();
		} else {
			log.warn("The configuration is not initialized.");
		}
		result = SwingJavaBuilder.build(this);

		setInitialPosition(mainWindow);
	}

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// activate internationalization
				SwingJavaBuilder.getConfig().addResourceBundle("OptionsWindow");
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					new OptionsWindow(null).setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void close() {
		setVisible(false);
		dispose();
	}

	/**
	 * TODO Save only the settings for those services that have changed.
	 */
	public void saveConfig() {
		// Create a new config changed file for each service. The form is
		// tmpDir/groupName + timeStamp
		final Set<String> groups = config.keySet();
		List<String> failedGroups = null;
		for (final String groupName : groups) {
			OutputStream groupOut = null;
			File groupSettingsFile = null;
			try {
				groupSettingsFile = File.createTempFile("sle", null);
				groupOut = new FileOutputStream(groupSettingsFile);
				final Properties groupSettings = config.get(groupName);
				// TODO Find a less kludgey way of converting boolean (from
				// checkboxes) to string
				final Set<Object> keys = groupSettings.keySet();
				for (final Object key : keys) {
					final Object value = groupSettings.get(key);
					if (value != null && !(value instanceof String)) {
						groupSettings.put(key, value.toString());
					}
				}
				groupSettings.store(groupOut, null);
			} catch (final Exception e) {
				log.warn("An error occurred while writing the updated settings.", e);
				if (failedGroups == null) {
					failedGroups = new ArrayList<String>();
				}
				failedGroups.add(groupName);
				continue;
			} finally {
				IOUtils.closeQuietly(groupOut);
			}
			if (!groupSettingsFile.renameTo(new File(FilenameUtils.concat(System.getProperty("java.io.tmpdir"),
					groupName + new Date().getTime())))) {
				if (failedGroups == null) {
					failedGroups = new ArrayList<String>();
				}
				failedGroups.add(groupName);
			}
		}

		if (failedGroups != null && failedGroups.size() > 0) {
			final StringBuffer errorBuffer = new StringBuffer();
			for (final String failedGroup : failedGroups) {
				errorBuffer.append(MessageFormat.format(
						SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_SAVE_OPTION_GROUP),
						failedGroup));
			}
			JOptionPane.showMessageDialog(this, errorBuffer.toString(),
					SwingJavaBuilder.getConfig().getResource(ResourceConstants.ERROR_SAVE_OPTIONS_TITLE),
					JOptionPane.ERROR_MESSAGE);
		} else {
			// The config change
			// watcher for each service will pick up the new file and apply the
			// config to their respective services, triggering a stop/start
			// cycle.
			close();
		}
	}

	/**
	 * TODO Convert the txt box to a list. Add Add Edit Remove buttons.
	 */
	public void browsePreAuthReplays() {
		appendReplayPath(PropertiesConstants.GROUP_PRE_AUTH_EMULATOR, TXT_PRE_AUTH_REPLAY_FILES);
	}

	/**
	 * TODO Convert the txt box to a list. Add Add Edit Remove buttons.
	 */
	public void browseAuthReplays() {
		appendReplayPath(PropertiesConstants.GROUP_PRE_AUTH_EMULATOR, TXT_AUTH_REPLAY_FILES);
	}

	/**
	 * @param txtFieldName
	 * @param group
	 * 
	 */
	private void appendReplayPath(final String group, final String txtFieldName) {
		final String newReplayPath = replayBrowse();
		if (newReplayPath != null) {
			final Properties properties = config.get(PropertiesConstants.GROUP_PRE_AUTH_EMULATOR);
			String preAuthReplays = properties.getProperty(PropertiesConstants.CONVERSATION_PATHS);
			final StringBuffer replayFilesBuffer = new StringBuffer();
			if (preAuthReplays != null && preAuthReplays.length() > 0) {
				replayFilesBuffer.append(preAuthReplays).append(File.pathSeparator);
			}
			replayFilesBuffer.append(newReplayPath);
			preAuthReplays = replayFilesBuffer.toString();
			final JTextField textField = (JTextField) result.get(TXT_PRE_AUTH_REPLAY_FILES);
			textField.setText(preAuthReplays);
		}
	}

	/**
	 * @param groupName
	 */
	private String replayBrowse() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setApproveButtonToolTipText("PICKME!!!!");
		fileChooser.setDialogTitle("Choose a conversation file.");
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Server conversation files.";
			}

			@Override
			public boolean accept(final File f) {
				return f.getName().equalsIgnoreCase("conversation") || f.isDirectory();
			}
		});

		if (fileChooser.showDialog(this, "MEMEMEME") == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile().getParent();
		}

		return null;
	}

	public void browseMeshPath() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setApproveButtonToolTipText("PICKME!!!!");
		fileChooser.setDialogTitle("Choose a mesh path.");

		if (fileChooser.showDialog(this, "MEMEMEME") == JFileChooser.APPROVE_OPTION) {
			final Properties meshConfig = config.get(PropertiesConstants.GROUP_MESH_EMULATOR);
			meshConfig.setProperty(PropertiesConstants.LOCAL_MESH_RESOURCE_BASE, fileChooser.getSelectedFile()
					.getAbsolutePath());
		}
	}

	/**
	 * @return the config
	 */
	public Map<String, Properties> getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(final Map<String, Properties> config) {
		this.config = config;
	}
}
